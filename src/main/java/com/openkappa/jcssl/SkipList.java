package com.openkappa.jcssl;

import java.util.Arrays;

import static com.openkappa.jcssl.Constants.TOP_LANE_BLOCK;
import static com.openkappa.jcssl.DataNode.newNode;
import static com.openkappa.jcssl.ProxyNode.newProxyNode;
import static com.openkappa.jcssl.Util.toUnsigned;

public class SkipList {


  /**
   * Creates a skip list
   * @param maxLevel
   * @param skip
   */
  public static SkipList createSkipList(byte maxLevel, byte skip) {
    return new SkipList(maxLevel, skip);
  }


  private final byte maxLevel;
  private final byte skip;
  private int numElements;
  private int[] itemsPerLevel;
  private int[] fastLaneItems;
  private int[] startsOfFastLanes;
  private int[] fastLanes;
  private ProxyNode[] fastLaneValues;
  private DataNode head;
  private DataNode tail;

  private SkipList(byte maxLevel, byte skip) {
    this.maxLevel = maxLevel;
    this.skip = skip > 1 ? skip : 2;
    this.head = newNode(0);
    this.tail = head;
    itemsPerLevel = new int[toUnsigned(maxLevel)];
    startsOfFastLanes = new int[toUnsigned(maxLevel)];
    fastLaneItems = new int[toUnsigned(maxLevel)];
    buildFastLanes();
  }

  /**
   * Adds the key to the skip list
   * @param key the value to insert
   */
  public void insert(int key) {
    DataNode newNode = newNode(key);
    boolean nodeInserted = true;
    boolean flaneInserted = false;

    // add new node at the end of the data list
    tail.setNext(newNode);
    tail = newNode;

    // add key to fast lanes
    for (int level = 0; level < toUnsigned(maxLevel); ++level) {
      // TODO - probably a better way of writing this
      if (numElements % (int) Math.pow(toUnsigned(skip), (level + 1)) == 0 && nodeInserted)
        // TODO - should this be != Integer.MAX_VALUE?
        nodeInserted = insertItemIntoFastLane((byte)level, newNode) != 0;
      else
        break;
      flaneInserted = true;
    }

    if (!flaneInserted)
      findAndInsertIntoProxyNode(newNode);

    ++numElements;

    // resize fast lanes if more space is needed
    if (numElements % (TOP_LANE_BLOCK*((int) Math.pow(toUnsigned(skip), maxLevel))) == 0)
      resizeFastLanes();
  }

  /**
   * Checks if the skip list contains the key
   * @param key the value to check
   * @return true if the key is in the skip list
   */
  public boolean contains(int key) {
    int curPos = 0;
    int first = 0;
    int last = itemsPerLevel[maxLevel - 1] - 1;
    int middle = 0;
    // scan highest fast lane with binary search
    while (first < last) {
      middle = (first + last) >>> 1;
      if (fastLanes[middle] < key) {
        first = middle + 1;
      } else if (fastLanes[middle] == key) {
        curPos = middle;
        break;
      } else {
        last = middle;
      }
    }
    if (first > last)
      curPos = last;
    int level;
    // traverse over fast lanes
    for (level = maxLevel - 1; level >= 0; --level) {
      int rPos = curPos - startsOfFastLanes[level];
      while (rPos < itemsPerLevel[level] &&
              key >= fastLanes[++curPos])
        rPos++;
      if (level == 0)
        break;
      curPos  = startsOfFastLanes[level - 1] + rPos * toUnsigned(skip);
    }
    if (key == fastLanes[--curPos])
      return true;

    ProxyNode proxy = fastLaneValues[curPos - startsOfFastLanes[0]];
    for (int i = 1; i < toUnsigned(skip); i++) {
      if (proxy.getKey(i) == key)
        return true;
    }
    return false;
  }


  public RangeSearchResult searchRange(int startKey, int endKey) {
    // use the cache to determine the section of the first fast lane that
    // should be used as starting position for search
    int level;
    int curPos = 0;
    int rPos = 0;
    int first = 0;
    int last = itemsPerLevel[maxLevel - 1] - 1;
    int middle = 0;
    // scan highest fast lane with binary search
    while (first < last) {
      middle = (first + last) / 2;
      if (fastLanes[middle] < startKey) {
        first = middle + 1;
      } else if (fastLanes[middle] == startKey) {
        curPos = middle;
        break;
      } else {
        last = middle;
      }
    }
    if (first > last)
      curPos = last;

    for (level = maxLevel - 1; level >= 0; level--) {
      rPos = curPos - startsOfFastLanes[level];
      while (rPos < itemsPerLevel[level] && startKey >= fastLanes[++curPos]) {
        rPos++;
      }
      if (level == 0) break;
      curPos  = startsOfFastLanes[level - 1] + rPos * toUnsigned(skip);
    }
    int start_of_flane = startsOfFastLanes[0];
    while (startKey < fastLanes[curPos] && curPos > start_of_flane) {
      curPos--;
    }

    ProxyNode proxy = fastLaneValues[curPos - startsOfFastLanes[0]];
    DataNode start = proxy.getValue(toUnsigned(skip) - 1).getNext();
    for (int i = 0; i < toUnsigned(skip); i++) {
      if (startKey <= proxy.getKey(i)) {
        start = proxy.getValue(i);
        break;
      }
    }

    int count = 0;
    // search for the range's last matching node
    // TODO: it's highly likely that there are porting errors here since this section relies on AVX intrinsics in the original code
    int itemsInFastLane = itemsPerLevel[0];
    rPos = curPos - start_of_flane;
    while (rPos++ < itemsInFastLane && fastLanes[curPos++] < endKey) {
      count += toUnsigned(skip);
    }

    while (endKey >= fastLanes[++curPos] && rPos < itemsInFastLane) {
      rPos++;
    }

    proxy = fastLaneValues[rPos];
    DataNode end = proxy.getValue(toUnsigned(skip) - 1);
    for (int i = 1; i < toUnsigned(skip); i++) {
      if (endKey < proxy.getKey(i)) {
        end = proxy.getValue(i - 1);
        break;
      }
    }

    return RangeSearchResult.of(start, end, count);
  }

  /**
   * Gets the size of the skip
   * @return the skip
   */
  public int getSkip() {
    return toUnsigned(skip);
  }

  private int insertItemIntoFastLane(byte level, DataNode node) {
    int curPos = startsOfFastLanes[level] + fastLaneItems[level];
    int levelLimit = curPos + itemsPerLevel[level];

    if (curPos > levelLimit)
      curPos = levelLimit;

    while (node.getKey() > fastLanes[curPos] && curPos < levelLimit)
      curPos++;

    if (fastLanes[curPos] == Integer.MAX_VALUE) {
      fastLanes[curPos] = node.getKey();
      if (level == 0)
        fastLaneValues[curPos - startsOfFastLanes[0]] = newProxyNode(this, node);
      fastLaneItems[level]++;
    } else { // TODO: MAX_VALUE seems to indicate failure to insert
      return Integer.MAX_VALUE;
    }

    return curPos;
  }


  private void buildFastLanes() {
    int fastLaneSize = TOP_LANE_BLOCK;

    itemsPerLevel[maxLevel - 1]  = fastLaneSize;
    startsOfFastLanes[maxLevel - 1] = 0;

    // calculate level sizes level by level
    for (int level = maxLevel - 2; level >= 0; level--) {
      itemsPerLevel[level]  = itemsPerLevel[level + 1] * toUnsigned(skip);
      startsOfFastLanes[level] = startsOfFastLanes[level + 1 ] +
              itemsPerLevel[level + 1];
      fastLaneSize += itemsPerLevel[level];
    }

    fastLanes = new int[fastLaneSize];
    Arrays.fill(fastLanes, Integer.MAX_VALUE);
    fastLaneValues = new ProxyNode[itemsPerLevel[0]];
  }

  private void resizeFastLanes() {
    // TODO - literal port, can be simplified greatly
    int newSize = itemsPerLevel[maxLevel - 1] + TOP_LANE_BLOCK;
    int[] levelItems = new int[maxLevel];
    int[] levelStarts = new int[maxLevel];

    levelItems[maxLevel - 1]  = newSize;
    levelStarts[maxLevel - 1] = 0;

    for (int level = maxLevel - 2; level >= 0; level--) {
      levelItems[level] = levelItems[level + 1] * toUnsigned(skip);
      levelStarts[level] = levelStarts[level + 1] + levelItems[level + 1];
      newSize += levelItems[level];
    }

    fastLanes = Arrays.copyOf(fastLanes, newSize);
    Arrays.fill(fastLanes, fastLaneItems[maxLevel - 1], newSize, Integer.MAX_VALUE);
    fastLaneValues = Arrays.copyOf(fastLaneValues, levelItems[0]);

    itemsPerLevel = levelItems;
    startsOfFastLanes = levelStarts;
  }

  // Adds a new element to the corresponding proxy lane in the given skip list
  void findAndInsertIntoProxyNode(DataNode node) {
    ProxyNode proxy = fastLaneValues[fastLaneItems[0] - 1];
    proxy.insert(node);
  }
}
