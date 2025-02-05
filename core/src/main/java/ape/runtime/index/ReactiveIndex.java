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

import ape.runtime.contracts.IndexQuerySet;
import ape.runtime.reactives.RxRecordBase;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** an index of a single column of data */
public class ReactiveIndex<Ty extends RxRecordBase> {
  /** a data structure which is precise; we know that the given item is in this bucket for SURE */
  private final TreeMap<Integer, TreeSet<Ty>> index;
  /**
   * as things change, we lose certainty of where items exist and have a grab-all bucket; this is an
   * optimization such that indexing happens between operations
   */
  private final TreeSet<Ty> unknowns;

  public ReactiveIndex(final TreeSet<Ty> unknowns) {
    this.index = new TreeMap<>();
    this.unknowns = unknowns;
  }

  /** add the item to the given index (via value `at`) */
  public void add(final int at, final Ty item) {
    var set = index.get(at);
    if (set == null) {
      set = new TreeSet<>();
      index.put(at, set);
    }
    set.add(item);
  }

  /** remove the item from the unknowns */
  public void delete(final Ty item) {
    unknowns.remove(item);
  }

  /** get the index */
  public TreeSet<Ty> of(final int at, IndexQuerySet.LookupMode mode) {
    return EvaluateLookupMode.of(index, at, mode);
  }

  /** remove the item from the index */
  public void remove(final int at, final Ty item) {
    if (delete(at, item)) {
      unknowns.add(item);
    }
  }

  /** delete the item from the given index (via value `at`) */
  public boolean delete(final int at, final Ty item) {
    final var set = index.get(at);
    final var result = set.remove(item);
    if (set.size() == 0) {
      index.remove(at);
    }
    return result;
  }

  /** (approx) how many bytes of memory does this index use */
  public long memory() {
    long sum = 64;
    for (Map.Entry<Integer, TreeSet<Ty>> entry : index.entrySet()) {
      sum += entry.getValue().size() * 20L + 20;
    }
    return sum;
  }
}
