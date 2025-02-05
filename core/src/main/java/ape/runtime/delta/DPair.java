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

import java.util.function.Supplier;

/** a cached pair of a key and a value for map results and other pairing; a delta version of NtPair */
public class DPair<dTyIn extends DeltaNode, dTyOut extends DeltaNode> implements DeltaNode {
  private dTyIn priorKey;
  private dTyOut priorValue;

  public DPair() {
    priorKey = null;
    priorValue = null;
  }

  /** the double is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (priorKey != null) {
      writer.writeNull();
      priorKey = null;
      priorValue = null;
    }
  }

  public dTyIn key(Supplier<dTyIn> make) {
    if (priorKey == null) {
      priorKey = make.get();
    }
    return priorKey;
  }

  public dTyOut value(Supplier<dTyOut> make) {
    if (priorValue == null) {
      priorValue = make.get();
    }
    return priorValue;
  }

  @Override
  public void clear() {
    priorKey = null;
    priorValue = null;
  }

  @Override
  public long __memory() {
    return 16 + (priorKey != null ? priorKey.__memory() : 0) + (priorValue != null ? priorValue.__memory() : 0);
  }
}
