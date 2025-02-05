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

import java.util.Arrays;

/** A sequence heap allows multiple heaps to come together into one; this does the translation of the returned region to the children heaps and vice-versa. */
public class SequenceHeap implements Heap {
  private final Heap[] heaps;
  private final long max;

  public SequenceHeap(Heap... heaps) {
    this.heaps = heaps;
    {
      long _max = 0;
      for (Heap heap : heaps) {
        _max += heap.max();
      }
      this.max = _max;
    }
  }

  @Override
  public void report(Report report) {
    for (Heap heap : heaps) {
      heap.report(report);
    }
  }

  @Override
  public long available() {
    long ret = 0;
    for (Heap heap : heaps) {
      ret += heap.available();
    }
    return ret;
  }

  @Override
  public long max() {
    return max;
  }

  @Override
  public Region ask(int size) {
    long newOffset = 0L;
    for (Heap heap : heaps) {
      Region got = heap.ask(size);
      if (got != null) {
        return new Region(newOffset + got.position, got.size);
      }
      newOffset += heap.max();
    }
    return null;
  }

  @Override
  public void free(Region region) {
    long withinOffset = region.position;
    for (Heap heap : heaps) {
      if (withinOffset < heap.max()) {
        heap.free(new Region(withinOffset, region.size));
        return;
      } else {
        withinOffset -= heap.max();
      }
    }
  }

  @Override
  public void snapshot(ByteBuf buf) {
    for (Heap heap : heaps) {
      heap.snapshot(buf);
    }
  }

  @Override
  public void load(ByteBuf buf) {
    for (Heap heap : heaps) {
      heap.load(buf);
    }
  }

  @Override
  public String toString() {
    return "Seq{" + Arrays.toString(heaps) + "}";
  }
}
