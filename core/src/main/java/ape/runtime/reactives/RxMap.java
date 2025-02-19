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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.common.SlashStringArrayEncoder;
import ape.runtime.contracts.RxChild;
import ape.runtime.contracts.RxKillable;
import ape.runtime.contracts.RxParent;
import ape.runtime.contracts.RxParentIntercept;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.json.JsonSum;
import ape.runtime.natives.NtMap;
import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtPair;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.reactives.maps.MapGuardTarget;
import ape.runtime.reactives.maps.MapPubSub;

import java.util.*;

/** a reactive map */
public class RxMap<DomainTy, RangeTy extends RxBase> extends RxBase implements Iterable<NtPair<DomainTy, RangeTy>>, RxParent, RxChild, RxKillable, MapGuardTarget {
  public final Codec<DomainTy, RangeTy> codec;
  public final LinkedHashMap<DomainTy, RangeTy> deleted;
  public final HashSet<DomainTy> created;
  private final NtMap<DomainTy, RangeTy> objects;
  private final Stack<RxMapGuard> guardsInflight;
  private RxMapGuard activeGuard;
  private MapPubSub<DomainTy> pubsub;

  public RxMap(final RxParent owner, final Codec<DomainTy, RangeTy> codec) {
    super(owner);
    this.codec = codec;
    this.objects = new NtMap<>();
    this.deleted = new LinkedHashMap<>();
    this.created = new HashSet<>();
    this.guardsInflight = new Stack<>();
    this.activeGuard = null;
    this.pubsub = new MapPubSub<>(owner);
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
    pubsub.settle();
    for (RangeTy item : objects.storage.values()) {
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
      pubsub.gc();
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.beginObject();
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.beginObject();
      for (final Map.Entry<DomainTy, RangeTy> entry : deleted.entrySet()) {
        if (entry.getValue() instanceof RxKillable) {
          ((RxKillable) entry.getValue()).__kill();
        }
        String key = codec.toStr(entry.getKey());
        final var value = entry.getValue();
        forwardDelta.writeObjectFieldIntro(key);
        forwardDelta.writeNull();
        reverseDelta.writeObjectFieldIntro(key);
        value.__dump(reverseDelta);
      }

      for (final Map.Entry<DomainTy, RangeTy> entry : objects.entries()) {
        String key = codec.toStr(entry.getKey());
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
    for (final Map.Entry<DomainTy, RangeTy> entry : objects.entries()) {
      writer.writeObjectFieldIntro(codec.toStr(entry.getKey()));
      entry.getValue().__dump(writer);
    }
    writer.endObject();
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        try {
          final var key = codec.fromStr(reader.fieldName());
          if (reader.testLackOfNull()) {
            var value = getOrCreateAndMaybeSignalDirty(key, false);
            value.__insert(reader);
            created.remove(key);
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
          final var key = codec.fromStr(reader.fieldName());
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
      for (final Map.Entry<DomainTy, RangeTy> entry : deleted.entrySet()) {
        objects.put(entry.getKey(), entry.getValue());
      }
      for (final DomainTy axe : created) {
        objects.removeDirect(axe);
      }
      for (final Map.Entry<DomainTy, RangeTy> entry : objects.entries()) {
        entry.getValue().__revert();
      }
      created.clear();
      deleted.clear();
      __lowerDirtyRevert();
    }
  }

  @Override
  public long __memory() {
    long sum = super.__memory() + 128 + pubsub.__memory();
    for (Map.Entry<DomainTy, RangeTy> entry : objects.entries()) {
      sum += entry.getValue().__memory() + 128;
      if (entry.getKey() instanceof String) {
        sum += ((String) entry.getKey()).length() * 2L;
      }
    }
    return sum;
  }

  public RangeTy getOrCreate(DomainTy key) {
    return getOrCreateAndMaybeSignalDirty(key, true);
  }

  public RangeTy getOrCreateAndMaybeSignalDirty(DomainTy key, boolean signalDirty) {
    if (activeGuard != null) {
      activeGuard.readKey(key);
    }
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
    value = deleted.remove(key);
    if (value != null) {
      __raiseDirty();
      objects.put(key, value);
      pubsub.changed(key);
      return value;
    }
    if (signalDirty) {
      __raiseDirty();
    }
    value = codec.make(new RxParentIntercept(this) {
      @Override
      public void __invalidateUp() {
        pubsub.changed(key);
        __invalidateSubscribers();
      }
    });
    objects.put(key, value);
    value.__subscribe(this);
    created.add(key);
    pubsub.changed(key);
    return value;
  }

  public boolean has(DomainTy key) {
    if (activeGuard != null) {
      activeGuard.readKey(key);
    }
    return objects.storage.containsKey(key);
  }

  public void remove(DomainTy key) {
    removeAndMaybeSignalDirty(key, true);
  }

  public void removeAndMaybeSignalDirty(DomainTy key, boolean signalDirty) {
    if (activeGuard != null) {
      activeGuard.readKey(key);
    }
    if (__parent != null) {
      __parent.__cost(10);
    }
    RangeTy value = objects.removeDirect(key);
    if (signalDirty) {
      pubsub.changed(key);
    }
    if (value != null) {
      if (!created.contains(key)) {
        if (signalDirty) {
          __raiseDirty();
        }
        deleted.put(key, value);
      }
    }
  }

  public void __subscribe(RxMapGuard<DomainTy> guard) {
    pubsub.subscribe(guard);
  }

  public void clear() {
    if (activeGuard != null) {
      activeGuard.readAll();
    }
    if (__parent != null) {
      __parent.__cost(2 * objects.size());
    }
    for (Map.Entry<DomainTy, RangeTy> entry : objects.storage.entrySet()) {
      deleted.put(entry.getKey(), entry.getValue());
    }
    __raiseDirty();
    objects.storage.clear();
  }

  @Override
  public void __kill() {
    for (final Map.Entry<DomainTy, RangeTy> entry : objects.entries()) {
      if (entry.getValue() instanceof RxKillable) {
        ((RxKillable) entry.getValue()).__kill();
      }
    }
  }

  public NtMaybe<RangeTy> lookup(DomainTy key) {
    if (activeGuard != null) {
      activeGuard.readKey(key);
    }
    if (__parent != null) {
      __parent.__cost(5);
    }
    return new NtMaybe<>(objects.get(key));
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
  public Iterator<NtPair<DomainTy, RangeTy>> iterator() {
    if (activeGuard != null) {
      activeGuard.readAll();
    }
    if (__parent != null) {
      __parent.__cost(objects.size() * 4);
    }
    return objects.iterator();
  }

  public int size() {
    if (activeGuard != null) {
      activeGuard.readAll();
    }
    if (__parent != null) {
      __parent.__cost(5);
    }
    return objects.size();
  }

  public NtMaybe<NtPair<DomainTy, RangeTy>> min() {
    if (activeGuard != null) {
      activeGuard.readAll();
    }
    if (__parent != null) {
      __parent.__cost(100);
    }
    return objects.min();
  }

  public NtMaybe<NtPair<DomainTy, RangeTy>> max() {
    if (activeGuard != null) {
      activeGuard.readAll();
    }
    if (__parent != null) {
      __parent.__cost(100);
    }
    return objects.max();
  }

  public interface Codec<DomainTy, RangeTy extends RxBase> {
    RangeTy make(RxParent maker);

    String toStr(DomainTy key);

    DomainTy fromStr(String key);
  }

  @Override
  public void __reportRx(String name, JsonStreamWriter __writer) {
    ArrayList<ObjectNode> children = new ArrayList<>();
    for (Map.Entry<DomainTy, RangeTy> entry : objects.entries()) {
      JsonStreamWriter childWriter = new JsonStreamWriter();
      childWriter.beginObject();
      entry.getValue().__reportRx("_", childWriter);
      childWriter.endObject();
      children.add(Json.parseJsonObject(childWriter.toString()));
    }

    int fromDirect = __getSubscriberCount();
    int fromPubsub = pubsub.count();

    __writer.writeObjectFieldIntro(name);
    __writer.beginObject();
    __writer.writeObjectFieldIntro("subscribers");
    __writer.writeInteger(fromDirect + fromPubsub);
    __writer.writeObjectFieldIntro("direct");
    __writer.writeInteger(fromDirect);
    __writer.writeObjectFieldIntro("pubsub");
    __writer.writeInteger(fromPubsub);
    __writer.writeObjectFieldIntro("count");
    __writer.writeInteger(objects.size());
    __writer.writeObjectFieldIntro("sum_items");
    __writer.injectJson(JsonSum.sum(children).get("_").toString());
    __writer.endObject();
  }

  @Override
  public void pushGuard(RxMapGuard guard) {
    guardsInflight.push(guard);
    activeGuard = guard;
  }

  @Override
  public void popGuard() {
    guardsInflight.pop();
    if (guardsInflight.empty()) {
      activeGuard = null;
    } else {
      activeGuard = guardsInflight.peek();
    }
  }

  public abstract static class IntegerCodec<R extends RxBase> implements Codec<Integer, R> {
    public String toStr(Integer key) {
      return "" + key;
    }

    public Integer fromStr(String key) {
      return Integer.parseInt(key);
    }
  }

  public abstract static class LongCodec<R extends RxBase> implements Codec<Long, R> {
    public String toStr(Long key) {
      return "" + key;
    }

    public Long fromStr(String key) {
      return Long.parseLong(key);
    }
  }

  public abstract static class StringCodec<R extends RxBase> implements Codec<String, R> {
    public String toStr(String key) {
      return key;
    }

    public String fromStr(String key) {
      return key;
    }
  }

  public abstract static class PrincipalCodec<R extends RxBase> implements Codec<NtPrincipal, R> {
    public String toStr(NtPrincipal key) {
      return SlashStringArrayEncoder.encode(key.agent, key.authority);
    }

    public NtPrincipal fromStr(String key) {
      String[] parts = SlashStringArrayEncoder.decode(key);
      return new NtPrincipal(parts[0], parts[1]);
    }
  }
}
