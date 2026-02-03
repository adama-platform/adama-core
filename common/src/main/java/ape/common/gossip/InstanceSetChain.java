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
package ape.common.gossip;

import ape.common.TimeSource;
import ape.common.gossip.codec.GossipProtocol;

import java.util.*;
import java.util.function.Consumer;

/**
 * Manages cluster membership state with historical snapshots for gossip reconciliation.
 * Maintains the current InstanceSet, recent history for hash-based lookups,
 * recently added instances for incremental sync, and recently deleted instances
 * for deletion propagation. Handles instance ingestion, expiration scanning,
 * and garbage collection.
 */
public class InstanceSetChain {
  private final TimeSource time;
  private final HashMap<String, Instance> primary;
  private final GarbageMap<InstanceSet> history;
  private final GarbageMap<Instance> recentlyLearnedAbout;
  private final GarbageMap<Instance> recentlyDeleted;
  private InstanceSet current;
  private Consumer<GossipProtocol.Endpoint[]> watcher;

  public InstanceSetChain(TimeSource time) {
    this.time = time;
    this.primary = new HashMap<>();
    this.history = new GarbageMap<>(Constants.MAX_HISTORY);
    this.current = new InstanceSet(new TreeSet<>(), time.nowMilliseconds());
    this.recentlyLearnedAbout = new GarbageMap<>(Constants.MAX_RECENT_ENTRIES);
    this.recentlyDeleted = new GarbageMap<>(Constants.MAX_DELETES);
    this.watcher = null;
  }

  public void setWatcher(Consumer<GossipProtocol.Endpoint[]> watcher) {
    this.watcher = watcher;
    broadcast();
  }

  private void broadcast() {
    if (watcher != null) {
      watcher.accept(all());
    }
  }

  public GossipProtocol.Endpoint[] all() {
    return current.toEndpoints();
  }

  public InstanceSet find(String hash) {
    if (current.hash().equals(hash)) {
      return current;
    }
    return history.get(hash);
  }

  public InstanceSet current() {
    return this.current;
  }

  public GossipProtocol.Endpoint[] recent() {
    ArrayList<GossipProtocol.Endpoint> list = new ArrayList<>();
    Iterator<Instance> instance = recentlyLearnedAbout.iterator();
    while (instance.hasNext()) {
      list.add(instance.next().toEndpoint());
    }
    return list.toArray(new GossipProtocol.Endpoint[list.size()]);
  }

  public long now() {
    return time.nowMilliseconds();
  }

  public GossipProtocol.Endpoint[] missing(InstanceSet set) {
    return current.missing(set);
  }

  public Runnable pick(String id) {
    Instance instance = primary.get(id);
    if (instance != null) {
      return () -> instance.bump(time.nowMilliseconds());
    } else {
      return null;
    }
  }

  public long scan() {
    long now = time.nowMilliseconds();
    long min = now;
    TreeSet<Instance> clone = null;
    Iterator<Map.Entry<String, Instance>> iterator = primary.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Instance> entry = iterator.next();
      Instance instance = entry.getValue();
      if (instance.tooOldMustDelete(now) && !instance.local) {
        if (clone == null) {
          clone = current.clone();
        }
        recentlyDeleted.put(entry.getKey(), instance, now);
        recentlyLearnedAbout.remove(entry.getKey());
        iterator.remove();
        clone.remove(instance);
      } else if (instance.witnessed() < min) {
        min = instance.witnessed();
      }
    }
    if (clone != null) {
      history.put(current.hash(), current, now);
      current = new InstanceSet(clone, now);
      broadcast();
    }
    return min;
  }

  public void gc() {
    long now = time.nowMilliseconds();
    history.gc(now);
    recentlyDeleted.gc(now);
    recentlyLearnedAbout.gc(now);
  }

  public boolean ingest(GossipProtocol.Endpoint[] endpoints, String[] deletes, boolean local) {
    long now = time.nowMilliseconds();
    TreeSet<Instance> clone = null;
    for (GossipProtocol.Endpoint ep : endpoints) {
      Instance prior = primary.get(ep.id);
      if (prior != null) {
        prior.absorb(ep.counter, now);
      } else {
        Instance newInstance = recentlyDeleted.remove(ep.id);
        if (newInstance == null) {
          newInstance = new Instance(ep, now, local);
        } else {
          newInstance.absorb(ep.counter, now);
        }
        if (clone == null) {
          clone = current.clone();
        }
        primary.put(ep.id, newInstance);
        clone.add(newInstance);
        recentlyLearnedAbout.put(ep.id, newInstance, now);
      }
    }
    for (String delId : deletes) {
      Instance prior = primary.get(delId);
      if (prior != null) {
        if (prior.canDelete(now)) {
          recentlyLearnedAbout.remove(delId);
          recentlyDeleted.put(delId, prior, now);
          primary.remove(delId);
          if (clone == null) {
            clone = current.clone();
          }
          clone.remove(prior);
        }
      }
    }
    if (clone != null) {
      history.put(current.hash(), current, now);
      current = new InstanceSet(clone, now);
      broadcast();
      return true;
    }
    return false;
  }

  public String[] deletes() {
    Collection<String> keys = recentlyDeleted.keys();
    return keys.toArray(new String[keys.size()]);
  }
}
