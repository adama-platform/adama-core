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
package ape.caravan.index.heaps;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ape.caravan.index.Region;
import ape.caravan.index.Report;
import org.junit.Assert;
import org.junit.Test;

public class LimitHeapTests {

  private void assetEqualsAfterSnapshot(String expected, LimitHeap heap) {
    Assert.assertEquals(expected, heap.toString());
    ByteBuf buf = Unpooled.buffer();
    heap.snapshot(buf);
    LimitHeap heap2 = new LimitHeap(new IndexedHeap(1024), 2048);
    heap2.load(buf);
    Assert.assertEquals(expected, heap2.toString());
  }

  @Test
  public void passthru_index() {
    LimitHeap heap = new LimitHeap(new IndexedHeap(1024), 2048);
    Assert.assertEquals(1024, heap.max());
    Assert.assertNull(heap.ask(50000));
    assetEqualsAfterSnapshot("[0,1024)", heap);
    Assert.assertEquals(1024, heap.available());
    Region a1 = heap.ask(7);
    Assert.assertEquals(1017, heap.available());
    assetEqualsAfterSnapshot("[7,1024)", heap);
    Region a2 = heap.ask(76);
    Assert.assertEquals(941, heap.available());
    assetEqualsAfterSnapshot("[83,1024)", heap);
    Region a3 = heap.ask(2);
    assetEqualsAfterSnapshot("[85,1024)", heap);
    Region a4 = heap.ask(12);
    assetEqualsAfterSnapshot("[97,1024)", heap);
    heap.free(a2);
    assetEqualsAfterSnapshot("[7,83)[97,1024)", heap);
    Region r1 = heap.ask(5);
    assetEqualsAfterSnapshot("[12,83)[97,1024)", heap);
    Region r2 = heap.ask(50);
    assetEqualsAfterSnapshot("[62,83)[97,1024)", heap);
    heap.free(r1);
    assetEqualsAfterSnapshot("[7,12)[62,83)[97,1024)", heap);
    Region az = heap.ask(15);
    {
      Report report = new Report();
      heap.report(report);
      Assert.assertEquals(1024, report.getTotalBytes());
      Assert.assertEquals(938, report.getFreeBytesAvailable());
    }
    assetEqualsAfterSnapshot("[7,12)[77,83)[97,1024)", heap);
    heap.free(a1);
    assetEqualsAfterSnapshot("[0,12)[77,83)[97,1024)", heap);
    heap.free(a3);
    assetEqualsAfterSnapshot("[0,12)[77,85)[97,1024)", heap);
    heap.free(a4);
    assetEqualsAfterSnapshot("[0,12)[77,1024)", heap);
    heap.free(r2);
    assetEqualsAfterSnapshot("[0,62)[77,1024)", heap);
    heap.free(az);
    assetEqualsAfterSnapshot("[0,1024)", heap);
    Region x0 = heap.ask(100);
    assetEqualsAfterSnapshot("[100,1024)", heap);
    heap.free(x0);
    assetEqualsAfterSnapshot("[0,1024)", heap);
    Region y0 = heap.ask(100);
    assetEqualsAfterSnapshot("[100,1024)", heap);
    Region y1 = heap.ask(100);
    assetEqualsAfterSnapshot("[200,1024)", heap);
    Region y2 = heap.ask(100);
    assetEqualsAfterSnapshot("[300,1024)", heap);
    Region y3 = heap.ask(100);
    assetEqualsAfterSnapshot("[400,1024)", heap);
    heap.free(y3);
    assetEqualsAfterSnapshot("[300,1024)", heap);
    heap.free(y1);
    assetEqualsAfterSnapshot("[100,200)[300,1024)", heap);
    heap.free(y2);
    assetEqualsAfterSnapshot("[100,1024)", heap);
    heap.free(y0);
    assetEqualsAfterSnapshot("[0,1024)", heap);
    Region k0 = heap.ask(100);
    assetEqualsAfterSnapshot("[100,1024)", heap);
    Region k1 = heap.ask(100);
    assetEqualsAfterSnapshot("[200,1024)", heap);
    Region k2 = heap.ask(100);
    assetEqualsAfterSnapshot("[300,1024)", heap);
    Region k3 = heap.ask(100);
    assetEqualsAfterSnapshot("[400,1024)", heap);
    Region k4 = heap.ask(100);
    assetEqualsAfterSnapshot("[500,1024)", heap);
    Region k5 = heap.ask(100);
    assetEqualsAfterSnapshot("[600,1024)", heap);
    Region k6 = heap.ask(100);
    assetEqualsAfterSnapshot("[700,1024)", heap);
    heap.free(k1);
    assetEqualsAfterSnapshot("[100,200)[700,1024)", heap);
    heap.free(k3);
    assetEqualsAfterSnapshot("[100,200)[300,400)[700,1024)", heap);
    heap.free(k5);
    assetEqualsAfterSnapshot("[100,200)[300,400)[500,600)[700,1024)", heap);
    heap.free(k6);
    assetEqualsAfterSnapshot("[100,200)[300,400)[500,1024)", heap);
    heap.free(k4);
    assetEqualsAfterSnapshot("[100,200)[300,1024)", heap);
    heap.free(k2);
    assetEqualsAfterSnapshot("[100,1024)", heap);
    heap.free(k0);
    assetEqualsAfterSnapshot("[0,1024)", heap);
    Assert.assertNull(heap.ask(2048));
    assetEqualsAfterSnapshot("[0,1024)", heap);
  }
}
