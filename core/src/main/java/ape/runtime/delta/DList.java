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
package ape.runtime.delta;

import ape.runtime.contracts.DeltaNode;
import ape.runtime.json.PrivateLazyDeltaWriter;

import java.util.ArrayList;
import java.util.function.Supplier;

/** a list that will respect privacy and sends state to client only on changes */
public class DList<dTy extends DeltaNode> implements DeltaNode {
  public final ArrayList<dTy> cachedDeltas;
  private int emittedSize;

  public DList() {
    this.cachedDeltas = new ArrayList<>();
    this.emittedSize = 0;
  }

  @Override
  public void clear() {
    this.cachedDeltas.clear();
    this.emittedSize = 0;
  }

  /** memory usage */
  @Override
  public long __memory() {
    long memory = 128;
    for (dTy item : cachedDeltas) {
      memory += item.__memory();
    }
    return memory;
  }

  /** the list is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (emittedSize > 0) {
      emittedSize = 0;
      cachedDeltas.clear();
      writer.writeNull();
    }
  }

  /** get the prior value at the given index using the supplier to fill in holes */
  public dTy getPrior(final int k, final Supplier<dTy> maker) {
    // Note: this function is called by generated code (See CodeGenDeltaClass) for each item in the
    // array, and the value
    // will be written out per item if there is something.
    while (cachedDeltas.size() <= k) {
      cachedDeltas.add(maker.get());
    }
    return cachedDeltas.get(k);
  }

  /** rectify the size */
  public void rectify(final int size, final PrivateLazyDeltaWriter writer) {
    // Note; this is called after the iteration to null out items
    int oldSize = cachedDeltas.size();
    for (var k = size; k < oldSize; k++) {
      writer.planField("" + k).writeNull();
      cachedDeltas.remove(cachedDeltas.size() - 1);
    }
    if (emittedSize != cachedDeltas.size()) {
      writer.planField("@s").writeInt(size);
      emittedSize = cachedDeltas.size();
    }
  }
}
