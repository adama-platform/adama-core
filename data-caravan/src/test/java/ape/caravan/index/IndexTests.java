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
package ape.caravan.index;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ape.caravan.data.DiskMetrics;
import ape.caravan.index.heaps.IndexedHeap;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class IndexTests {
  public void assertEquals(String expected, Index index) {
    Assert.assertEquals(expected, index.toString());
    ByteBuf buf = Unpooled.buffer();
    index.snapshot(buf);
    Index index2 = new Index();
    index2.load(buf);
    Assert.assertEquals(expected, index2.toString());
  }

  private AnnotatedRegion wrap(Region r, AtomicInteger tracker) {
    return new AnnotatedRegion(r.position, r.size, tracker.getAndIncrement(), 0L);
  }

  @Test
  public void flow() {
    Index index = new Index();
    assertEquals("", index);
    Heap heap = new IndexedHeap(1024);
    AtomicInteger tracker = new AtomicInteger(0);

    index.append(1L, wrap(heap.ask(100), tracker));
    index.append(1L, wrap(heap.ask(100), tracker));
    assertEquals("1=[0,100=0][100,200=1];", index);
    index.append(2L, wrap(heap.ask(100), tracker));
    index.append(2L, wrap(heap.ask(100), tracker));
    assertEquals("1=[0,100=0][100,200=1];2=[200,300=2][300,400=3];", index);
    index.append(3L, wrap(heap.ask(100), tracker));
    index.append(3L, wrap(heap.ask(100), tracker));
    assertEquals("1=[0,100=0][100,200=1];2=[200,300=2][300,400=3];3=[400,500=4][500,600=5];", index);

    index.append(4L, wrap(heap.ask(1), tracker));
    index.append(4L, wrap(heap.ask(2), tracker));
    index.append(4L, wrap(heap.ask(3), tracker));
    index.append(4L, wrap(heap.ask(4), tracker));
    assertEquals("1=[0,100=0][100,200=1];2=[200,300=2][300,400=3];3=[400,500=4][500,600=5];4=[600,601=6][601,603=7][603,606=8][606,610=9];", index);

    for (Region region : index.trim(4L, 3)) {
      heap.free(region);
    }
    assertEquals("1=[0,100=0][100,200=1];2=[200,300=2][300,400=3];3=[400,500=4][500,600=5];4=[601,603=7][603,606=8][606,610=9];", index);

    Assert.assertTrue(index.exists(3));
    for (Region region : index.delete(3)) {
      heap.free(region);
    }
    Assert.assertFalse(index.exists(3));
    assertEquals("1=[0,100=0][100,200=1];2=[200,300=2][300,400=3];4=[601,603=7][603,606=8][606,610=9];", index);

    for (Region region : index.delete(4)) {
      heap.free(region);
    }
    for (Region region : index.delete(2)) {
      heap.free(region);
    }
    for (Region region : index.delete(1)) {
      heap.free(region);
    }
    assertEquals("", index);
    Assert.assertEquals("[0,1024)", heap.toString());
    Assert.assertNull(index.trim(500, 1));
  }

  @Test
  public void histogram() {
    Index index = new Index();
    long at = 0;
    int size = 1;
    for (int pow10 = 0; pow10 < 9; pow10++) {
      index.append(1, new AnnotatedRegion(at, size, 0, 0));
      at += size;
      size *= 10;
    }
    index.report(new DiskMetrics(new NoOpMetricsFactory()));
  }
}
