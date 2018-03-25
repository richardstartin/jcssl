package com.openkappa.jcssl;

import java.util.Arrays;

class ProxyNode {

  public static ProxyNode newProxyNode(SkipList skipList, DataNode node) {
    return new ProxyNode(skipList, node);
  }
  private int[] keys;
  private DataNode[] values;

  public ProxyNode(SkipList skipList, DataNode dataNode) {

    this.keys = new int[skipList.getSkip()];
    this.values = new DataNode[skipList.getSkip()];
    Arrays.fill(keys, Integer.MAX_VALUE);
    keys[0] = dataNode.getKey();
    values[0] = dataNode;

  }

  void insert(DataNode node) {
    for (int i = 1; i < keys.length; ++i) {
      if (keys[i] == Integer.MAX_VALUE) {
        keys[i] = node.getKey();
        values[i] = node;
        return;
      }
    }
  }

  int getKey(int index) {
    return keys[index];
  }

  DataNode getValue(int index) {
    return values[index];
  }
}
