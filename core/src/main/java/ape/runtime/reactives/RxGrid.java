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

import ape.common.Pair;
import ape.common.SimpleStringArrayCodec;
import ape.runtime.contracts.RxChild;
import ape.runtime.contracts.RxKillable;
import ape.runtime.contracts.RxParent;
import ape.runtime.contracts.RxParentIntercept;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtPair;

import java.util.*;
import java.util.function.Function;

/** a 2D grid of a reactive type */
public class RxGrid<DomainTy, RangeTy extends RxBase> extends RxBase implements RxParent, RxChild, RxKillable, Iterable<NtPair<Pair<DomainTy>, RangeTy>> {
  public final RxMap.Codec<DomainTy, RangeTy> codec;
  private final HashMap<Pair<DomainTy>, RangeTy> objects;
  private final HashMap<Pair<DomainTy>, RangeTy> creates;
  private final HashMap<Pair<DomainTy>, RangeTy> deletes;
  private int cachedMinX;
  private int cachedMinY;
  private int cachedWidth;
  private int cachedHeight;
  private boolean widthAndHeightValid;

  public RxGrid(final RxParent owner, final RxMap.Codec<DomainTy, RangeTy> codec) {
    super(owner);
    this.codec = codec;
    this.objects = new HashMap<>();
    this.creates = new HashMap<>();
    this.deletes = new HashMap<>();
    this.cachedMinX = 0;
    this.cachedMinY = 0;
    this.cachedWidth = 0;
    this.cachedHeight = 0;
    this.widthAndHeightValid = true;
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
  public void __kill() {
    for (final Map.Entry<Pair<DomainTy>, RangeTy> entry : objects.entrySet()) {
      if (entry.getValue() instanceof RxKillable) {
        ((RxKillable) entry.getValue()).__kill();
      }
    }
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
  public void __invalidateUp() {
    __invalidateSubscribers();
  }

  @Override
  public void __settle(Set<Integer> viewers) {
    for (RangeTy item : objects.values()) {
      if (item instanceof RxParent) {
        ((RxParent) item).__settle(viewers);
      } else {
        return;
      }
    }
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.beginObject();
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.beginObject();
      for (final Map.Entry<Pair<DomainTy>, RangeTy> entry : deletes.entrySet()) {
        if (entry.getValue() instanceof RxKillable) {
          ((RxKillable) entry.getValue()).__kill();
        }
        String key = encodeKey(entry.getKey());
        final var value = entry.getValue();
        forwardDelta.writeObjectFieldIntro(key);
        forwardDelta.writeNull();
        reverseDelta.writeObjectFieldIntro(key);
        value.__dump(reverseDelta);
      }

      for (final Map.Entry<Pair<DomainTy>, RangeTy> entry : objects.entrySet()) {
        String key = encodeKey(entry.getKey());
        final var value = entry.getValue();
        if (creates.containsKey(entry.getKey())) {
          value.__commit(key, forwardDelta, new JsonStreamWriter());
          reverseDelta.writeObjectFieldIntro(key);
          reverseDelta.writeNull();
        } else {
          value.__commit(key, forwardDelta, reverseDelta);
        }
      }
      creates.clear();
      deletes.clear();
      forwardDelta.endObject();
      reverseDelta.endObject();
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(JsonStreamWriter writer) {
    writer.beginObject();
    for (final Map.Entry<Pair<DomainTy>, RangeTy> entry : objects.entrySet()) {
      writer.writeObjectFieldIntro(encodeKey(entry.getKey()));
      entry.getValue().__dump(writer);
    }
    writer.endObject();
  }

  @Override
  public void __insert(JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        try {
          final var key = decodeKey(reader.fieldName());
          if (reader.testLackOfNull()) {
            var value = getOrCreateAndMaybeSignalDirty(key, false);
            value.__insert(reader);
            creates.remove(key);
          } else {
            removeAndMaybeSignalDirty(key, false);
          }
        } catch (Exception ex) {
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
          final var key = decodeKey(reader.fieldName());
          if (reader.testLackOfNull()) {
            getOrCreate(key).__patch(reader);
          } else {
            remove(key);
          }
        } catch (Exception ex) {
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
      for (final Map.Entry<Pair<DomainTy>, RangeTy> entry : deletes.entrySet()) {
        objects.put(entry.getKey(), entry.getValue());
      }
      for (final Pair<DomainTy> axe : creates.keySet()) {
        objects.remove(axe);
      }
      for (final Map.Entry<Pair<DomainTy>, RangeTy> entry : objects.entrySet()) {
        entry.getValue().__revert();
      }
      creates.clear();
      deletes.clear();
      __lowerDirtyRevert();
    }
  }

  @Override
  public long __memory() {
    long sum = super.__memory() + 128;
    for (Map.Entry<Pair<DomainTy>, RangeTy> entry : objects.entrySet()) {
      sum += entry.getValue().__memory() + 256;
    }
    return sum;
  }

  private String encodeKey(Pair<DomainTy> key) {
    String x = codec.toStr(key.x);
    String y = codec.toStr(key.y);
    return SimpleStringArrayCodec.pack(x, y);
  }

  private Pair<DomainTy> decodeKey(String key) {
    String[] coords = SimpleStringArrayCodec.unpack(key);
    return new Pair<>(codec.fromStr(coords[0]), codec.fromStr(coords[1]));
  }

  public RangeTy getOrCreate(Pair<DomainTy> key) {
    return getOrCreateAndMaybeSignalDirty(key, true);
  }

  public void remove(Pair<DomainTy> key) {
    removeAndMaybeSignalDirty(key, true);
  }

  public void removeAndMaybeSignalDirty(Pair<DomainTy> key, boolean signalDirty) {
    if (__parent != null) {
      __parent.__cost(10);
    }
    RangeTy value = objects.remove(key);
    if (value != null) {
      if (!creates.containsKey(key)) {
        if (signalDirty) {
          __raiseDirty();
        }
        widthAndHeightValid = false;
        deletes.put(key, value);
      }
    }
  }

  public RangeTy getOrCreateAndMaybeSignalDirty(Pair<DomainTy> key, boolean signalDirty) {
    RangeTy value = objects.get(key);
    if (value != null) {
      if (__parent != null) {
        __parent.__cost(1);
      }
      return value;
    }
    if (__parent != null) {
      __parent.__cost(10);
    }
    value = deletes.remove(key);
    if (value != null) {
      widthAndHeightValid = false;
      __raiseDirty();
      objects.put(key, value);
      return value;
    }
    if (signalDirty) {
      __raiseDirty();
    }
    widthAndHeightValid = false;
    value = codec.make(new RxParentIntercept(this) {
      @Override
      public void __invalidateUp() {
        __invalidateSubscribers();
      }
    });
    objects.put(key, value);
    value.__subscribe(this);
    creates.put(key, value);
    return value;
  }

  public void remove(DomainTy x, DomainTy y) {
    remove(new Pair<>(x, y));
  }

  public boolean has(DomainTy x, DomainTy y) {
    return objects.containsKey(new Pair<>(x, y));
  }

  public void clear() {
    ArrayList<Pair<DomainTy>> all = new ArrayList<>(objects.keySet());
    for (Pair<DomainTy> key : all) {
      remove(key);
    }
  }



  public RangeTy lookup(DomainTy x, DomainTy y) {
    return getOrCreate(new Pair<>(x, y));
  }

  private void buildWidthAndHeight() {
    cachedMinX = 0;
    cachedMinY = 0;
    int maxX = -1;
    int maxY = -1;
    for (Pair<DomainTy> key : objects.keySet()) {
      int u = (Integer) key.x;
      int v = (Integer) key.y;
      cachedMinX = Math.min(u, cachedMinX);
      cachedMinY = Math.min(v, cachedMinY);
      maxX = Math.max(u, maxX);
      maxY = Math.max(v, maxY);
    }
    cachedWidth = maxX - cachedMinX + 1;
    cachedHeight = maxY - cachedMinY + 1;
  }

  public RxBase[] base_flatten() {
    if (!widthAndHeightValid) {
      buildWidthAndHeight();
    }
    RxBase[] result = new RxBase[cachedWidth * cachedHeight];
    for (int k = 0; k < result.length; k++) {
      result[k] = null;
    }
    for (Map.Entry<Pair<DomainTy>, RangeTy> entry : objects.entrySet()) {
      Pair<DomainTy> key = entry.getKey();
      int x = ((Integer) key.x) - cachedMinX;
      int y = ((Integer) key.y) - cachedMinY;
      result[y * cachedWidth + x] = entry.getValue();
    }

    return result;
  }

  public double[] flatten(double defValue) {
    RxBase[] flat = base_flatten();
    double[] result = new double[flat.length];
    for (int i = 0; i < result.length; i++) {
      if (flat[i] != null) {
        result[i] = ((RxDouble) flat[i]).get();
      } else {
        result[i] = defValue;
      }
    }
    return result;
  }

  public int[] flatten(int defValue) {
    RxBase[] flat = base_flatten();
    int[] result = new int[flat.length];
    for (int i = 0; i < result.length; i++) {
      if (flat[i] != null) {
        result[i] = ((RxInt32) flat[i]).get();
      } else {
        result[i] = defValue;
      }
    }
    return result;
  }

  public boolean[] flatten(boolean defValue) {
    RxBase[] flat = base_flatten();
    boolean[] result = new boolean[flat.length];
    for (int i = 0; i < result.length; i++) {
      if (flat[i] != null) {
        result[i] = ((RxBoolean) flat[i]).get();
      } else {
        result[i] = defValue;
      }
    }
    return result;
  }

  public int min_x() {
    if (!widthAndHeightValid) {
      buildWidthAndHeight();
    }
    return cachedMinX;
  }

  public int min_y() {
    if (!widthAndHeightValid) {
      buildWidthAndHeight();
    }
    return cachedMinY;
  }

  public int width() {
    if (!widthAndHeightValid) {
      buildWidthAndHeight();
    }
    return cachedWidth;
  }

  public int height() {
    if (!widthAndHeightValid) {
      buildWidthAndHeight();
    }
    return cachedHeight;
  }

  @Override
  public Iterator<NtPair<Pair<DomainTy>, RangeTy>> iterator() {
    Iterator<Map.Entry<Pair<DomainTy>, RangeTy>> it = objects.entrySet().iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public NtPair<Pair<DomainTy>, RangeTy> next() {
        Map.Entry<Pair<DomainTy>, RangeTy> next = it.next();
        return new NtPair<>(next.getKey(), next.getValue());
      }
    };
  }
}
