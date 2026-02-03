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
package ape.runtime.contracts;

import java.util.Set;

/**
 * Interface for parent nodes in the reactive object tree.
 * Parents receive dirty notifications from children, track computational costs,
 * propagate invalidations upward, and coordinate settling of reactive state.
 * The reactive tree structure enables efficient change tracking and delta
 * computation by bubbling changes up to the document root.
 */
public interface RxParent {
  /** make this item dirty */
  void __raiseDirty();

  /** is the parent alive */
  boolean __isAlive();

  /** hidden costs made manifest up the parent chain */
  void __cost(int cost);

  /** children may request an upward invalidation */
  void __invalidateUp();

  /** settle down the reactivity */
  void __settle(Set<Integer> viewers);

  public static final RxParent DEAD = new RxParent() {
    @Override
    public void __raiseDirty() {
    }

    @Override
    public boolean __isAlive() {
      return false;
    }

    @Override
    public void __cost(int cost) {
    }

    @Override
    public void __invalidateUp() {
    }

    @Override
    public void __settle(Set<Integer> viewers) {
    }
  };
}
