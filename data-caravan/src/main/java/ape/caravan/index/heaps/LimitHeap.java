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
import ape.caravan.index.Heap;
import ape.caravan.index.Region;
import ape.caravan.index.Report;

/** a passthrough heap which ensures everything allocated from this heap is under a specific size */
public class LimitHeap implements Heap {

  private final Heap parent;
  private final int sizeLimit;

  public LimitHeap(Heap parent, int sizeLimit) {
    this.parent = parent;
    this.sizeLimit = sizeLimit;
  }

  @Override
  public void report(Report report) {
    parent.report(report);
  }

  @Override
  public long available() {
    return parent.available();
  }

  @Override
  public long max() {
    return parent.max();
  }

  @Override
  public Region ask(int size) {
    if (size > sizeLimit) {
      return null;
    }
    return parent.ask(size);
  }

  @Override
  public void free(Region region) {
    parent.free(region);
  }

  @Override
  public void snapshot(ByteBuf buf) {
    parent.snapshot(buf);
  }

  @Override
  public void load(ByteBuf buf) {
    parent.load(buf);
  }

  @Override
  public String toString() {
    return parent.toString();
  }
}
