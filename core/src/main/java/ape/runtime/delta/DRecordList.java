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

import java.util.*;
import java.util.function.Supplier;

/**
 * Delta-tracking list for privacy-aware record synchronization to clients.
 * Maintains cached delta state per client and computes minimal ordering diffs.
 * Uses a Walk pattern to track which records are present in the current view,
 * detects ordering changes by comparing against previous state, and emits
 * compact range-based diffs when contiguous sequences move together.
 * Records no longer visible are removed and null-ed out in the delta.
 */
public class DRecordList<dRecordTy extends DeltaNode> implements DeltaNode {
  public final HashMap<Integer, dRecordTy> cache;
  public final ArrayList<Integer> order;

  public DRecordList() {
    this.order = new ArrayList<>();
    this.cache = new HashMap<>();
  }

  @Override
  public void clear() {
    this.order.clear();
    this.cache.clear();
  }

  /** memory usage */
  @Override
  public long __memory() {
    long memory = order.size() * 32L;
    for (Map.Entry<Integer, dRecordTy> entry : cache.entrySet()) {
      memory += 40 + entry.getValue().__memory();
    }
    return memory;
  }

  /** start walking the records */
  public Walk begin() {
    return new Walk();
  }

  /** get the cached item */
  public dRecordTy getPrior(final int id, final Supplier<dRecordTy> maker) {
    var prior = cache.get(id);
    if (prior == null) {
      prior = maker.get();
      cache.put(id, prior);
    }
    return prior;
  }

  /** the list of records is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (cache.size() > 0) {
      order.clear();
      cache.clear();
      writer.writeNull();
    }
  }

  public class Walk {
    private final ArrayList<Integer> newOrdering;
    private final Iterator<Integer> oldOrderingIt;
    private final HashSet<Integer> seen;
    private boolean orderingUnchanged;

    private Walk() {
      newOrdering = new ArrayList<>();
      orderingUnchanged = true;
      oldOrderingIt = order.iterator();
      seen = new HashSet<>();
    }

    /** we completely walked over the map */
    public void end(final PrivateLazyDeltaWriter parent) {
      // we didn't invalidate the ordering
      if (orderingUnchanged) {
        // let's make sure the ordering respected the size
        orderingUnchanged = newOrdering.size() == order.size();
      }
      // the ordering did not change, so let's send a new ordering differential
      if (!orderingUnchanged) {
        final var orderingField = parent.planField("@o");
        final var array = orderingField.planArray();
        array.manifest();
        final var keyToOldPosition = new HashMap<Integer, Integer>();
        // let's record the hold positions of the keys and their index
        for (var k = 0; k < order.size(); k++) {
          keyToOldPosition.put(order.get(k), k);
        }
        // let's walk the new ordering
        for (var k = 0; k < newOrdering.size(); k++) {
          final int newOrderKey = newOrdering.get(k);
          final var oldPosition = keyToOldPosition.get(newOrderKey);
          if (oldPosition != null) {
            // the new key has a key within the old array, cool
            var good = true;
            var top = k;
            int trackPosition = oldPosition;
            // let's see how much of the new ordering starting at k is a sub ordering of the old
            for (var j = k + 1; good && j < newOrdering.size(); j++) {
              final int testOrderKey = newOrdering.get(j);
              final var testOldPosition = keyToOldPosition.get(testOrderKey);
              if (testOldPosition == null || testOldPosition.intValue() != trackPosition + 1) {
                good = false;
              } else {
                top = j;
                trackPosition++;
              }
            }
            if (top - k < 2) {
              // too not enough overlap, write the new key and move on
              array.writeInt(newOrderKey);
            } else {
              // ok, now write a range and skip the items
              final var rangeArr = array.planArray();
              rangeArr.writeInt(oldPosition);
              rangeArr.writeInt(oldPosition + (top - k));
              rangeArr.end();
              k = top;
            }
          } else {
            // just directly write the new key and move on
            array.writeInt(newOrderKey);
          }
        }
        array.end();
        order.clear();
        order.addAll(newOrdering);
      }
      final var cacheIt = cache.entrySet().iterator();
      while (cacheIt.hasNext()) {
        final var entry = cacheIt.next();
        if (!seen.contains(entry.getKey())) {
          cacheIt.remove();
          parent.planField(entry.getKey()).writeNull();
        }
      }
    }

    /** a new id shows up */
    public void next(final int id) {
      seen.add(id);
      newOrdering.add(id);
      if (orderingUnchanged) {
        if (oldOrderingIt.hasNext()) {
          if (id != oldOrderingIt.next()) {
            orderingUnchanged = false;
          }
        }
      }
    }
  }
}
