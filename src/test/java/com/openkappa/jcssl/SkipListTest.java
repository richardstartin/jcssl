package com.openkappa.jcssl;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SkipListTest {

  @Test
  public void createSkipList() {
    SkipList sl = SkipList.createSkipList((byte)2, (byte)256);
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
    SkipList sl = SkipList.createSkipList((byte)2, (byte)256);
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
    SkipList sl = SkipList.createSkipList((byte)2, (byte)4);
    sl.insert(0);
    sl.insert(10000);
    sl.insert(20000);
    assertTrue(sl.contains(0));
    assertTrue(sl.contains(10000));
    assertTrue(sl.contains(20000));
    assertFalse(sl.contains(3));
  }



}