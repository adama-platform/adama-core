/**
 * MIT License
 * 
 * Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ape.runtime.stdlib;

import ape.runtime.natives.lists.ArrayNtList;
import ape.runtime.natives.lists.EmptyNtList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class LibStatisticsTests {
  @Test
  public void minmaxEmpties() {
    Assert.assertFalse(LibStatistics.minInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.minLongs(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.minDoubles(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.maxInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.maxLongs(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.maxDoubles(new EmptyNtList<>()).has());
  }

  @Test
  public void minMaxDoubles() {
    final var vals = new ArrayList<Double>();
    vals.add(20.5);
    vals.add(1.5);
    vals.add(300.75);
    vals.add(100.5);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(1.5, LibStatistics.minDoubles(list).get(), 0.1);
    Assert.assertEquals(300.75, LibStatistics.maxDoubles(list).get(), 0.1);
  }

  @Test
  public void minMaxIntegers() {
    final var ints = new ArrayList<Integer>();
    ints.add(20);
    ints.add(1);
    ints.add(300);
    ints.add(100);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(1, (int) LibStatistics.minInts(list).get());
    Assert.assertEquals(300, (int) LibStatistics.maxInts(list).get());
  }

  @Test
  public void minMaxLongs() {
    final var lngs = new ArrayList<Long>();
    lngs.add(20L);
    lngs.add(-25L);
    lngs.add(450L);
    lngs.add(100L);
    final var list = new ArrayNtList<>(lngs);
    Assert.assertEquals(-25, (long) LibStatistics.minLongs(list).get());
    Assert.assertEquals(450, (long) LibStatistics.maxLongs(list).get());
  }

  @Test
  public void avgEmpties() {
    Assert.assertFalse(LibStatistics.avgInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.avgLongs(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.avgDoubles(new EmptyNtList<>()).has());
  }

  @Test
  public void avgDoubles() {
    final var vals = new ArrayList<Double>();
    vals.add(1.5);
    vals.add(20.5);
    vals.add(300.75);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(107.58333333333333, LibStatistics.avgDoubles(list).get(), 0.1);
  }

  @Test
  public void avgIntegers() {
    final var ints = new ArrayList<Integer>();
    ints.add(1);
    ints.add(20);
    ints.add(300);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(107.0, LibStatistics.avgInts(list).get(), 0.1);
  }

  @Test
  public void avgLongs() {
    final var lngs = new ArrayList<Long>();
    lngs.add(1L);
    lngs.add(20L);
    lngs.add(300L);
    final var list = new ArrayNtList<>(lngs);
    Assert.assertEquals(107.0, LibStatistics.avgLongs(list).get(), 0.1);
  }

  @Test
  public void sumEmpties() {
    Assert.assertFalse(LibStatistics.sumInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.sumLongs(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.sumDoubles(new EmptyNtList<>()).has());
  }

  @Test
  public void sumDoubles() {
    final var ints = new ArrayList<Double>();
    ints.add(1.5);
    ints.add(20.5);
    ints.add(300.75);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(322.75, LibStatistics.sumDoubles(list).get(), 0.1);
  }

  @Test
  public void sumIntegers() {
    final var ints = new ArrayList<Integer>();
    ints.add(1);
    ints.add(20);
    ints.add(300);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(321, (int) LibStatistics.sumInts(list).get());
  }

  @Test
  public void sumLongs() {
    final var longs = new ArrayList<Long>();
    longs.add(1L);
    longs.add(200000L);
    longs.add(30000000000L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(30000200001L, (long) LibStatistics.sumLongs(list).get());
  }

  @Test
  public void medianEmpties() {
    Assert.assertFalse(LibStatistics.medianDoubles(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.medianInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.medianLongs(new EmptyNtList<>()).has());
  }

  @Test
  public void medianDoubles1() {
    final var vals = new ArrayList<Double>();
    vals.add(1.5);
    vals.add(20.5);
    vals.add(300.75);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(20.5, LibStatistics.medianDoubles(list).get(), 0.1);
  }

  @Test
  public void medianIntegers1() {
    final var ints = new ArrayList<Integer>();
    ints.add(1);
    ints.add(20);
    ints.add(300);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(20, (int) LibStatistics.medianInts(list).get());
  }

  @Test
  public void medianLongs1() {
    final var longs = new ArrayList<Long>();
    longs.add(1L);
    longs.add(200000L);
    longs.add(30000000000L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(200000L, (long) LibStatistics.medianLongs(list).get());
  }

  @Test
  public void medianDoubles2() {
    final var vals = new ArrayList<Double>();
    vals.add(20.5);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(20.5, LibStatistics.medianDoubles(list).get(), 0.1);
  }

  @Test
  public void medianIntegers2() {
    final var ints = new ArrayList<Integer>();
    ints.add(20);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(20, (int) LibStatistics.medianInts(list).get());
  }

  @Test
  public void medianLongs2() {
    final var longs = new ArrayList<Long>();
    longs.add(200000L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(200000L, (long) LibStatistics.medianLongs(list).get());
  }

  @Test
  public void medianDoubles3() {
    final var vals = new ArrayList<Double>();
    vals.add(400.5);
    vals.add(1.5);
    vals.add(10.5);
    vals.add(20.5);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(15.5, LibStatistics.medianDoubles(list).get(), 0.1);
  }

  @Test
  public void medianIntegers3() {
    final var ints = new ArrayList<Integer>();
    ints.add(400);
    ints.add(1);
    ints.add(10);
    ints.add(20);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(15, (int) LibStatistics.medianInts(list).get());
  }

  @Test
  public void medianLongs3() {
    final var longs = new ArrayList<Long>();
    longs.add(400L);
    longs.add(1L);
    longs.add(10L);
    longs.add(20L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(15L, (long) LibStatistics.medianLongs(list).get());
  }

  @Test
  public void percentilesEmpties() {
    Assert.assertFalse(LibStatistics.percentileInts(new EmptyNtList<>(), 0.9).has());
    Assert.assertFalse(LibStatistics.percentileDoubles(new EmptyNtList<>(), 0.9).has());
    Assert.assertFalse(LibStatistics.percentileLongs(new EmptyNtList<>(), 0.9).has());
  }

  @Test
  public void percentilesDoubles() {
    final var vals = new ArrayList<Double>();
    vals.add(400.5);
    vals.add(1.5);
    vals.add(10.5);
    vals.add(20.5);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(400.5, LibStatistics.percentileDoubles(list, 0.95).get(), 0.1);
  }

  @Test
  public void percentilesInteger() {
    final var ints = new ArrayList<Integer>();
    ints.add(400);
    ints.add(1);
    ints.add(10);
    ints.add(20);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(400, (int) LibStatistics.percentileInts(list, 0.95).get());
  }

  @Test
  public void percentilesLong() {
    final var longs = new ArrayList<Long>();
    longs.add(400L);
    longs.add(1L);
    longs.add(10L);
    longs.add(20L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(400L, (long) LibStatistics.percentileLongs(list, 0.95).get());
  }

  @Test
  public void varianceEmpties() {
    Assert.assertFalse(LibStatistics.varianceDoubles(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.varianceInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.varianceLongs(new EmptyNtList<>()).has());
  }

  @Test
  public void varianceDoubles() {
    final var vals = new ArrayList<Double>();
    vals.add(2.0);
    vals.add(4.0);
    vals.add(4.0);
    vals.add(4.0);
    vals.add(5.0);
    vals.add(5.0);
    vals.add(7.0);
    vals.add(9.0);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(4.0, LibStatistics.varianceDoubles(list).get(), 0.01);
  }

  @Test
  public void varianceIntegers() {
    final var ints = new ArrayList<Integer>();
    ints.add(2);
    ints.add(4);
    ints.add(4);
    ints.add(4);
    ints.add(5);
    ints.add(5);
    ints.add(7);
    ints.add(9);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(4.0, LibStatistics.varianceInts(list).get(), 0.01);
  }

  @Test
  public void varianceLongs() {
    final var longs = new ArrayList<Long>();
    longs.add(2L);
    longs.add(4L);
    longs.add(4L);
    longs.add(4L);
    longs.add(5L);
    longs.add(5L);
    longs.add(7L);
    longs.add(9L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(4.0, LibStatistics.varianceLongs(list).get(), 0.01);
  }

  @Test
  public void stddevEmpties() {
    Assert.assertFalse(LibStatistics.stddevDoubles(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.stddevInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.stddevLongs(new EmptyNtList<>()).has());
  }

  @Test
  public void stddevDoubles() {
    final var vals = new ArrayList<Double>();
    vals.add(2.0);
    vals.add(4.0);
    vals.add(4.0);
    vals.add(4.0);
    vals.add(5.0);
    vals.add(5.0);
    vals.add(7.0);
    vals.add(9.0);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(2.0, LibStatistics.stddevDoubles(list).get(), 0.01);
  }

  @Test
  public void stddevIntegers() {
    final var ints = new ArrayList<Integer>();
    ints.add(2);
    ints.add(4);
    ints.add(4);
    ints.add(4);
    ints.add(5);
    ints.add(5);
    ints.add(7);
    ints.add(9);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(2.0, LibStatistics.stddevInts(list).get(), 0.01);
  }

  @Test
  public void stddevLongs() {
    final var longs = new ArrayList<Long>();
    longs.add(2L);
    longs.add(4L);
    longs.add(4L);
    longs.add(4L);
    longs.add(5L);
    longs.add(5L);
    longs.add(7L);
    longs.add(9L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(2.0, LibStatistics.stddevLongs(list).get(), 0.01);
  }

  @Test
  public void rangeEmpties() {
    Assert.assertFalse(LibStatistics.rangeDoubles(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.rangeInts(new EmptyNtList<>()).has());
    Assert.assertFalse(LibStatistics.rangeLongs(new EmptyNtList<>()).has());
  }

  @Test
  public void rangeDoubles() {
    final var vals = new ArrayList<Double>();
    vals.add(1.5);
    vals.add(20.5);
    vals.add(300.75);
    final var list = new ArrayNtList<>(vals);
    Assert.assertEquals(299.25, LibStatistics.rangeDoubles(list).get(), 0.01);
  }

  @Test
  public void rangeIntegers() {
    final var ints = new ArrayList<Integer>();
    ints.add(1);
    ints.add(20);
    ints.add(300);
    final var list = new ArrayNtList<>(ints);
    Assert.assertEquals(299, (int) LibStatistics.rangeInts(list).get());
  }

  @Test
  public void rangeLongs() {
    final var longs = new ArrayList<Long>();
    longs.add(1L);
    longs.add(200000L);
    longs.add(30000000000L);
    final var list = new ArrayNtList<>(longs);
    Assert.assertEquals(29999999999L, (long) LibStatistics.rangeLongs(list).get());
  }
}
