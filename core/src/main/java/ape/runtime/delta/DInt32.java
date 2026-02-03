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

/**
 * Delta tracker for 32-bit integers with privacy-aware change detection.
 * Caches previously sent value and only emits to PrivateLazyDeltaWriter
 * when the value actually changes. The hide() method sends null when a
 * field becomes private. Used by generated PrivateView code to minimize
 * bandwidth in delta synchronization.
 */
public class DInt32 implements DeltaNode {
  private Integer prior;

  public DInt32() {
    prior = null;
  }

  /** the int32 is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (prior != null) {
      writer.writeNull();
      prior = null;
    }
  }

  @Override
  public void clear() {
    prior = null;
  }

  /** memory usage */
  @Override
  public long __memory() {
    return 40;
  }

  /** the int32 is visible, so show changes */
  public void show(final int value, final PrivateLazyDeltaWriter writer) {
    if (prior == null || value != prior.intValue()) {
      writer.writeInt(value);
    }
    prior = value;
  }
}
