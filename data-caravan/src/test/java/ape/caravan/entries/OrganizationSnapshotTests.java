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

import ape.caravan.index.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ape.caravan.index.*;
import ape.caravan.index.heaps.IndexedHeap;
import ape.runtime.data.Key;
import org.junit.Assert;
import org.junit.Test;

public class OrganizationSnapshotTests {

  private AnnotatedRegion wrap(Region r) {
    return new AnnotatedRegion(r.position, r.size, 0, 0L);
  }

  @Test
  public void snapshot() {
    ByteBuf buf = Unpooled.buffer();
    final String givenHeap;
    final String givenIndex;
    final String givenKeymap;
    {
      Heap heap = new IndexedHeap(1024);
      Index index = new Index();
      KeyMap keymap = new KeyMap();
      keymap.apply(new MapKey(new Key("space", "key"), 1));
      index.append(1, wrap(heap.ask(42)));
      index.append(2, wrap(heap.ask(100)));
      index.append(2, wrap(heap.ask(100)));
      index.append(2, wrap(heap.ask(100)));
      index.append(3, wrap(heap.ask(50)));
      givenHeap = heap.toString();
      givenIndex = index.toString();
      givenKeymap = keymap.toString();
      OrganizationSnapshot snapshot = new OrganizationSnapshot(heap, index, keymap);
      snapshot.write(buf);
    }
    {
      Assert.assertEquals(0x57, buf.readByte());
      Heap heap = new IndexedHeap(1024);
      Index index = new Index();
      KeyMap keymap = new KeyMap();
      OrganizationSnapshot.populateAfterTypeId(buf, heap, index, keymap);
      Assert.assertEquals(givenHeap, heap.toString());
      Assert.assertEquals(givenIndex, index.toString());
      Assert.assertEquals(givenKeymap, keymap.toString());
    }
  }
}
