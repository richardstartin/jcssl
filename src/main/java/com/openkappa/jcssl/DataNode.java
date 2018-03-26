package com.openkappa.jcssl;

class DataNode {

  public static DataNode newNode(int key) {
    return new DataNode(key);
  }

  private final int key;
  private DataNode next;

  public DataNode(int key) {
    this.key = key;
  }

  public int getKey() {
    return key;
  }

  public void setNext(DataNode next) {
    this.next = next;
  }

  public DataNode getNext() {
    return next;
  }

  @Override
  public String toString() {
    return String.valueOf(key);
  }
}
