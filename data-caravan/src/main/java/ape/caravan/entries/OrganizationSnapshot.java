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
package ape.caravan.entries;

import io.netty.buffer.ByteBuf;
import ape.caravan.contracts.WALEntry;
import ape.caravan.index.Heap;
import ape.caravan.index.Index;
import ape.caravan.index.KeyMap;

public class OrganizationSnapshot implements WALEntry {
  private final Heap heap;
  private final Index index;
  private final KeyMap keymap;

  public OrganizationSnapshot(Heap heap, Index index, KeyMap keymap) {
    this.heap = heap;
    this.index = index;
    this.keymap = keymap;
  }

  public static void populateAfterTypeId(ByteBuf buf, Heap heap, Index index, KeyMap keymap) {
    heap.load(buf);
    index.load(buf);
    keymap.load(buf);
  }

  @Override
  public void write(ByteBuf buf) {
    buf.writeByte(0x57);
    heap.snapshot(buf);
    index.snapshot(buf);
    keymap.snapshot(buf);
  }
}
