package com.openkappa.jcssl;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class SkipListTest {


  @Test(expected = IllegalArgumentException.class)
  public void rejectSkipsLargerThanMaxSkip() {
    SkipList.createSkipList(9, 6);
  }

  @Test
  public void createSkipList() {
    SkipList sl = SkipList.createSkipList(9, 5);
    sl.insert(0);
    sl.insert(1);
    sl.insert(2);
    assertTrue(sl.contains(0));
    assertTrue(sl.contains(1));
    assertTrue(sl.contains(2));
    assertFalse(sl.contains(3));
  }


  @Test
  public void createSkipListMissingValue() {
    SkipList sl = SkipList.createSkipList(9, 5);
    sl.insert(0);
    sl.insert(1);
    sl.insert(3);
    assertTrue(sl.contains(0));
    assertTrue(sl.contains(1));
    assertTrue(sl.contains(3));
    assertFalse(sl.contains(2));
  }


  @Test
  public void createSkipListSparse() {
    SkipList sl = SkipList.createSkipList(9, 5);
    sl.insert(0);
    sl.insert(10000);
    sl.insert(20000);
    assertTrue(sl.contains(0));
    assertTrue(sl.contains(10000));
    assertTrue(sl.contains(20000));
    assertFalse(sl.contains(3));
  }

  @Test
  public void createSkipListDense() {
    SkipList sl = SkipList.createSkipList(9, 5);
    for (int i = 0; i < 100_000; ++i) {
      sl.insert(i);
    }
    for (int i = 0; i < 100_000; ++i) {
      assertTrue(String.valueOf(i) + " is missing", sl.contains(i));
    }
  }


  @Test
  public void testRangeQueryDense() {
    SkipList sl = SkipList.createSkipList(9, 5);
    for (int i = 0; i < 100_000; ++i) {
      sl.insert(i);
    }

    RangeSearchResult r1 = sl.searchRange(0, 1000);
    assertEquals(1000, r1.getCount());
    assertEquals(0, r1.getStart().getKey());
    assertEquals(1000, r1.getEnd().getKey());
    RangeSearchResult r2 = sl.searchRange(1, 1001);
    assertEquals(1000, r2.getCount());
    assertEquals(1, r2.getStart().getKey());
    assertEquals(1001, r2.getEnd().getKey());

  }


  @Test
  public void testRangeQueryDenseRepeatedValues2() {
    SkipList sl = SkipList.createSkipList(9, 5);
    for (int i = 0; i < 100_000; ++i) {
      sl.insert(i);
      sl.insert(i);
    }

    RangeSearchResult r1 = sl.searchRange(0, 1000);
    assertEquals(2000, r1.getCount());
    assertEquals(0, r1.getStart().getKey());
    assertEquals(1000, r1.getEnd().getKey());
    RangeSearchResult r2 = sl.searchRange(1, 1001);
    assertEquals(2000, r2.getCount());
    assertEquals(1, r2.getStart().getKey());
    assertEquals(1001, r2.getEnd().getKey());
  }


  @Test
  public void testRangeQueryDenseRepeatedValues3() {
    SkipList sl = SkipList.createSkipList(9, 5);
    for (int i = 0; i < 100_000; ++i) {
      sl.insert(i);
      sl.insert(i);
      sl.insert(i);
    }
    RangeSearchResult r1 = sl.searchRange(0, 1000);
    assertEquals(3000, r1.getCount());
    assertEquals(0, r1.getStart().getKey());
    assertEquals(1000, r1.getEnd().getKey());
    assertArrayEquals(IntStream.range(0, 1000).flatMap(i -> IntStream.of(new int[] { i, i, i})).toArray(), r1.stream().toArray());
    RangeSearchResult r2 = sl.searchRange(1, 1001);
    assertEquals(3000, r2.getCount());
    assertEquals(1, r2.getStart().getKey());
    assertEquals(1001, r2.getEnd().getKey());
    RangeSearchResult r3 = sl.searchRange(93, 128);
    assertEquals((128 - 93) * 3, r3.getCount());
    assertEquals(93, r3.getStart().getKey());
    assertEquals(128, r3.getEnd().getKey());
  }


  @Test
  public void testRangeSparseSkipList() {
    SkipList sl = SkipList.createSkipList(9, 5);
    for (int i = 0; i < 100_000; i += 1000) {
      sl.insert(i);
    }
    RangeSearchResult result = sl.searchRange(500, 2500);
    assertEquals(1000, result.getStart().getKey());
    assertEquals(3000, result.getEnd().getKey());
    assertEquals(2, result.getCount());
    assertEquals(new int[] {1000, 2000}, result.stream().toArray());
  }


  @Test
  public void testRangeSparseSkipList2() {
    SkipList sl = SkipList.createSkipList(9, 5);
    for (int i = 0; i < 100_000; i += 10) {
      sl.insert(i);
    }
    RangeSearchResult result = sl.searchRange(50, 110);
    assertEquals(50, result.getStart().getKey());
    assertEquals(110, result.getEnd().getKey());
    assertEquals(6, result.getCount());
    assertArrayEquals(new int[] {50, 60, 70, 80, 90, 100}, result.stream().toArray());
  }


  @Test
  public void testRangeSparseSkipList3() {
    SkipList sl = SkipList.createSkipList(9, 5);
    for (int i = 0; i < 100_000; i += 100) {
      sl.insert(i);
    }
    RangeSearchResult result = sl.searchRange(50, 50_000);
    assertEquals(100, result.getStart().getKey());
    assertEquals(50_000, result.getEnd().getKey());
    assertEquals(499, result.getCount());
    assertArrayEquals(IntStream.range(1, 500).map(i -> i * 100).toArray(), result.stream().toArray());
  }


}