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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

/** a map that will respect privacy and sends state to client only on changes */
public class DMap<TyIn, dTyOut extends DeltaNode> implements DeltaNode {
  // cache of all the items in the map
  private final HashMap<TyIn, dTyOut> cache;

  public DMap() {
    this.cache = new HashMap<>();
  }

  /** start walking items; the generated code in CodeGenDeltaClass is related */
  public DMap.Walk begin() {
    return new DMap.Walk();
  }

  /** the map is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (cache.size() > 0) {
      cache.clear();
      writer.writeNull();
    }
  }

  @Override
  public void clear() {
    cache.clear();
  }

  /** memory usage */
  @Override
  public long __memory() {
    long memory = 40;
    for (Map.Entry<TyIn, dTyOut> entry : cache.entrySet()) {
      memory += 40 + entry.getValue().__memory();
    }
    return memory;
  }

  public class Walk {
    private final HashSet<TyIn> seen;

    private Walk() {
      this.seen = new HashSet<>();
    }

    /** a new element in the map */
    public dTyOut next(final TyIn key, final Supplier<dTyOut> maker) {
      seen.add(key);
      var value = cache.get(key);
      if (value == null) {
        value = maker.get();
        cache.put(key, value);
      }
      return value;
    }

    /** the map iteration is over */
    public void end(final PrivateLazyDeltaWriter parent) {
      final var cacheIt = cache.entrySet().iterator();
      while (cacheIt.hasNext()) {
        final var entry = cacheIt.next();
        if (!seen.contains(entry.getKey())) {
          cacheIt.remove();
          parent.planField("" + entry.getKey()).writeNull();
        }
      }
    }
  }
}
