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
package ape.runtime.reactives;

import ape.runtime.contracts.RxChild;
import ape.runtime.contracts.RxKillable;
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtPair;
import ape.runtime.natives.lists.ArrayNtList;

import java.util.*;
import java.util.function.Function;

/**
 * Reactive ordered list using doubles as position keys in a TreeMap.
 * Supports efficient ordered insertions (head, tail, between elements)
 * with automatic position redistribution when positions get too close.
 * get() returns elements ordered by position as an NtList.
 */
public class RxList<RangeTy extends RxBase> extends RxBase implements Iterable<NtPair<Double, RangeTy>>, RxParent, RxChild, RxKillable {
  public static final double SPACING = 10.0;
  public static final double MIN_GAP = 1.0 / 256.0;

  private final TreeMap<Double, RangeTy> items;
  public final LinkedHashMap<Double, RangeTy> deleted;
  public final HashSet<Double> created;
  private final Function<RxParent, RangeTy> maker;

  public RxList(final RxParent owner, final Function<RxParent, RangeTy> maker) {
    super(owner);
    this.items = new TreeMap<>();
    this.deleted = new LinkedHashMap<>();
    this.created = new HashSet<>();
    this.maker = maker;
  }

  @Override
  public boolean __isAlive() {
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  @Override
  public void __cost(int cost) {
    if (__parent != null) {
      __parent.__cost(cost);
    }
  }

  @Override
  public void __settle(Set<Integer> viewers) {
    __lowerInvalid();
    for (RangeTy item : items.values()) {
      if (item instanceof RxParent) {
        ((RxParent) item).__settle(viewers);
      } else {
        return;
      }
    }
  }

  public RangeTy prepend() {
    double pos;
    if (items.isEmpty()) {
      pos = 0.0;
    } else {
      pos = items.firstKey() - SPACING;
    }
    return insertAt(pos);
  }

  public RangeTy append() {
    double pos;
    if (items.isEmpty()) {
      pos = 0.0;
    } else {
      pos = items.lastKey() + SPACING;
    }
    return insertAt(pos);
  }

  public RangeTy insertAfter(double key) {
    Double higher = items.higherKey(key);
    double pos;
    if (higher == null) {
      pos = key + SPACING;
    } else {
      pos = (key + higher) / 2.0;
    }
    return insertAt(pos);
  }

  public RangeTy insertBefore(double key) {
    Double lower = items.lowerKey(key);
    double pos;
    if (lower == null) {
      pos = key - SPACING;
    } else {
      pos = (lower + key) / 2.0;
    }
    return insertAt(pos);
  }

  private RangeTy insertAt(double pos) {
    __raiseDirty();
    RangeTy value = maker.apply(this);
    value.__subscribe(this);
    items.put(pos, value);
    created.add(pos);
    maybeRedistribute(pos);
    return value;
  }

  private void maybeRedistribute(double pos) {
    Double lower = items.lowerKey(pos);
    Double higher = items.higherKey(pos);
    double gap = Double.MAX_VALUE;
    if (lower != null) {
      gap = Math.min(gap, pos - lower);
    }
    if (higher != null) {
      gap = Math.min(gap, higher - pos);
    }
    if (gap < MIN_GAP) {
      redistribute();
    }
  }

  private void redistribute() {
    ArrayList<Map.Entry<Double, RangeTy>> entries = new ArrayList<>(items.entrySet());
    HashSet<Double> oldCreated = new HashSet<>(created);
    items.clear();
    created.clear();
    double pos = 0.0;
    for (Map.Entry<Double, RangeTy> entry : entries) {
      double oldPos = entry.getKey();
      RangeTy val = entry.getValue();
      boolean wasCreated = oldCreated.contains(oldPos);
      items.put(pos, val);
      if (wasCreated) {
        // Was created this transaction, remains created at new position
        created.add(pos);
      } else if (pos != oldPos) {
        // Committed position changed, mark old as deleted and new as created
        deleted.put(oldPos, val);
        created.add(pos);
      }
      // If pos == oldPos and was committed, no action needed
      pos += SPACING;
    }
  }

  public void remove(double key) {
    RangeTy value = items.remove(key);
    if (value != null) {
      __raiseDirty();
      if (!created.remove(key)) {
        deleted.put(key, value);
      }
    }
  }

  public NtList<RangeTy> get() {
    return new ArrayNtList<>(new ArrayList<>(items.values()));
  }

  public int size() {
    return items.size();
  }

  /** Returns an unmodifiable view of the position keys */
  public Set<Double> keys() {
    return Collections.unmodifiableSet(items.keySet());
  }

  @Override
  public Iterator<NtPair<Double, RangeTy>> iterator() {
    return items.entrySet().stream()
      .map(e -> new NtPair<>(e.getKey(), e.getValue()))
      .iterator();
  }

  public void clear() {
    __raiseDirty();
    for (final Map.Entry<Double, RangeTy> entry : items.entrySet()) {
      if (!created.remove(entry.getKey())) {
        deleted.put(entry.getKey(), entry.getValue());
      }
    }
    items.clear();
    created.clear();
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.beginObject();
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.beginObject();
      // Handle deleted entries
      for (final Map.Entry<Double, RangeTy> entry : deleted.entrySet()) {
        if (entry.getValue() instanceof RxKillable) {
          ((RxKillable) entry.getValue()).__kill();
        }
        String key = keyToStr(entry.getKey());
        forwardDelta.writeObjectFieldIntro(key);
        forwardDelta.writeNull();
        reverseDelta.writeObjectFieldIntro(key);
        entry.getValue().__dump(reverseDelta);
      }
      // Handle existing entries
      for (final Map.Entry<Double, RangeTy> entry : items.entrySet()) {
        String key = keyToStr(entry.getKey());
        final var value = entry.getValue();
        if (created.contains(entry.getKey())) {
          value.__commit(key, forwardDelta, new JsonStreamWriter());
          reverseDelta.writeObjectFieldIntro(key);
          reverseDelta.writeNull();
        } else {
          value.__commit(key, forwardDelta, reverseDelta);
        }
      }
      created.clear();
      deleted.clear();
      forwardDelta.endObject();
      reverseDelta.endObject();
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.beginObject();
    for (final Map.Entry<Double, RangeTy> entry : items.entrySet()) {
      writer.writeObjectFieldIntro(keyToStr(entry.getKey()));
      entry.getValue().__dump(writer);
    }
    writer.endObject();
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        try {
          final double key = Double.parseDouble(reader.fieldName());
          if (reader.testLackOfNull()) {
            RangeTy value = items.get(key);
            if (value == null) {
              value = maker.apply(this);
              value.__subscribe(this);
              items.put(key, value);
            }
            value.__insert(reader);
            created.remove(key);
          } else {
            RangeTy removed = items.remove(key);
            if (removed != null && !created.remove(key)) {
              // was committed, no need to track in deleted during insert
            }
          }
        } catch (NumberFormatException ex) {
          reader.skipValue();
        }
      }
    } else {
      reader.skipValue();
    }
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        try {
          final double key = Double.parseDouble(reader.fieldName());
          if (reader.testLackOfNull()) {
            RangeTy value = items.get(key);
            if (value == null) {
              __raiseDirty();
              value = maker.apply(this);
              value.__subscribe(this);
              items.put(key, value);
              created.add(key);
            }
            value.__patch(reader);
          } else {
            remove(key);
          }
        } catch (NumberFormatException ex) {
          reader.skipValue();
        }
      }
    } else {
      reader.skipValue();
    }
  }

  @Override
  public void __revert() {
    if (__isDirty()) {
      // Remove created entries first (before restoring deleted) because
      // redistribution can assign a created position that collides with a deleted one
      for (final Double key : created) {
        items.remove(key);
      }
      // Restore deleted entries
      for (final Map.Entry<Double, RangeTy> entry : deleted.entrySet()) {
        items.put(entry.getKey(), entry.getValue());
      }
      // Revert remaining values
      for (final Map.Entry<Double, RangeTy> entry : items.entrySet()) {
        entry.getValue().__revert();
      }
      created.clear();
      deleted.clear();
      __lowerDirtyRevert();
    }
  }

  @Override
  public long __memory() {
    long sum = super.__memory() + 128;
    for (Map.Entry<Double, RangeTy> entry : items.entrySet()) {
      sum += entry.getValue().__memory() + 48;
    }
    return sum;
  }

  @Override
  public void __kill() {
    for (final Map.Entry<Double, RangeTy> entry : items.entrySet()) {
      if (entry.getValue() instanceof RxKillable) {
        ((RxKillable) entry.getValue()).__kill();
      }
    }
  }

  @Override
  public boolean __raiseInvalid() {
    __invalidateSubscribers();
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  @Override
  public void __invalidateUp() {
    __invalidateSubscribers();
  }

  @Override
  public void __raiseDirty() {
    super.__raiseDirty();
  }

  private static String keyToStr(double key) {
    if (key == Math.floor(key) && !Double.isInfinite(key)) {
      return Long.toString((long) key);
    }
    return Double.toString(key);
  }
}
