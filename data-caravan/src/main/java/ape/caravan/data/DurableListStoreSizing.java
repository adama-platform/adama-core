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
package ape.caravan.data;

import ape.caravan.index.Heap;
import ape.caravan.index.heaps.IndexedHeap;
import ape.caravan.index.heaps.LimitHeap;
import ape.caravan.index.heaps.SequenceHeap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Setup the heap and storage for very large data durable list store */
public class DurableListStoreSizing {
  private static final long SIZE_CUT_OFF = 1024 * 1024 * 1024;
  public final Heap heap;
  public final Storage storage;

  public DurableListStoreSizing(long totalSize, File base) throws IOException {
    ArrayList<Heap> heaps = new ArrayList<>();
    ArrayList<Storage> storages = new ArrayList<>();

    long size = totalSize;
    if (size >= SIZE_CUT_OFF) {
      heaps.add(new LimitHeap(new IndexedHeap(SIZE_CUT_OFF / 4), 8196));
      heaps.add(new LimitHeap(new IndexedHeap(SIZE_CUT_OFF / 4), 4 * 8196));
      heaps.add(new IndexedHeap(SIZE_CUT_OFF / 2));
      storages.add(new MemoryMappedFileStorage(new File(base.getParentFile(), base.getName() + "-PRIME"), SIZE_CUT_OFF));
      size -= SIZE_CUT_OFF;
    }
    int k = 0;
    while (size > 0) {
      long sizeToUse = size;
      if (sizeToUse > SIZE_CUT_OFF) {
        sizeToUse = SIZE_CUT_OFF;
      }
      size -= sizeToUse;
      heaps.add(new IndexedHeap(sizeToUse));
      storages.add(new MemoryMappedFileStorage(new File(base.getParentFile(), base.getName() + "-" + k), sizeToUse));
      k++;
    }

    this.heap = new SequenceHeap(heaps.toArray(new Heap[heaps.size()]));
    this.storage = new SequenceStorage(storages.toArray(new Storage[storages.size()]));
  }
}
