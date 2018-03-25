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

  public DataNode getStart() {
    return start;
  }

  public DataNode getEnd() {
    return end;
  }

  public int getCount() {
    return count;
  }


}
