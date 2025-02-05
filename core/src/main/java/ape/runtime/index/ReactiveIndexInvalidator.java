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
package ape.runtime.index;

import ape.runtime.contracts.RxChild;
import ape.runtime.reactives.RxRecordBase;

/**
 * an index value must respond to change, and this enables that indexing to occur reactively to data
 * changes.
 */
public abstract class ReactiveIndexInvalidator<Ty extends RxRecordBase<Ty>> implements RxChild {
  private final ReactiveIndex<Ty> index;
  private final Ty item;
  private Integer indexedAt;

  public ReactiveIndexInvalidator(final ReactiveIndex<Ty> index, final Ty item) {
    this.index = index;
    this.item = item;
    this.indexedAt = null;
  }

  /** a change happened, so remove from the index */
  @Override
  public boolean __raiseInvalid() {
    if (indexedAt != null) {
      index.remove(indexedAt, item);
      indexedAt = null;
    }
    return true;
  }

  /** index the item by it's given value */
  public void reindex() {
    if (indexedAt == null) {
      indexedAt = pullValue();
      index.add(indexedAt, item);
    }
  }

  /** pull the value to index on */
  public abstract int pullValue();

  /** remove from all index */
  public void deindex() {
    if (indexedAt != null) {
      index.delete(indexedAt, item);
      indexedAt = null;
    } else {
      index.delete(item);
    }
  }
}
