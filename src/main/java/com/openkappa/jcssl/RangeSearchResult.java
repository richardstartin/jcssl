package com.openkappa.jcssl;

public class RangeSearchResult {

  public static RangeSearchResult of(DataNode start, DataNode end, int count) {
    return new RangeSearchResult(start, end, count);
  }

  private final DataNode start;
  private final DataNode end;
  private final int count;

  public RangeSearchResult(DataNode start, DataNode end, int count) {
    this.start = start;
    this.end = end;
    this.count = count;
  }

  /**
   * The inclusive first element of the range
   * @return
   */
  public DataNode getStart() {
    return start;
  }

  /**
   * The exclusive last element of the range
   * @return
   */
  public DataNode getEnd() {
    return end;
  }

  /**
   * The number of values in the range (exclusing the end)
   * @return the number of elements
   */
  public int getCount() {
    return count;
  }


}
