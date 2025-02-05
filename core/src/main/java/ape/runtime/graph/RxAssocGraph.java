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
package ape.runtime.graph;

import ape.runtime.natives.NtList;
import ape.runtime.natives.lists.ArrayNtList;
import ape.runtime.reactives.RxMapGuard;
import ape.runtime.reactives.RxRecordBase;
import ape.runtime.reactives.RxTable;
import ape.runtime.reactives.maps.MapGuardTarget;
import ape.runtime.reactives.maps.MapPubSub;
import ape.runtime.reactives.maps.MapSubscription;

import java.util.*;

/** within a graph, this represents all the edges for a single assoc */
public class RxAssocGraph<TyTo extends RxRecordBase<TyTo>> implements MapGuardTarget {
  private final HashMap<Integer, TreeMap<Integer, Integer>> edges;
  private final ArrayList<DifferentialEdgeTracker<?, ?>> partials;
  private final ArrayList<RxTable<TyTo>> tables;
  private final MapPubSub<Integer> pubsub;
  private final Stack<RxMapGuard> guardsInflight;
  private RxMapGuard activeGuard;


  public RxAssocGraph() {
    this.edges = new HashMap<>();
    this.partials = new ArrayList<>();
    this.tables = new ArrayList<>();
    this.pubsub = new MapPubSub<>(null);
    this.guardsInflight = new Stack<>();
    this.activeGuard = null;
  }

  public void incr(int from, int to) {
    TreeMap<Integer, Integer> right = edges.get(from);
    if (right == null) {
      right = new TreeMap<>();
      edges.put(from, right);
    }
    Integer prior = right.get(to);
    if (prior == null) {
      prior = 0;
    }
    right.put(to, prior + 1);
    pubsub.changed(from);
  }

  public void decr(int from, int to) {
    TreeMap<Integer, Integer> right = edges.get(from);
    if (right != null) {
      Integer prior = right.get(to);
      if (prior != null) {
        if (prior > 1) {
          right.put(to, prior - 1);
        } else {
          right.remove(to);
        }
      }
      if (right.size() == 0) {
        edges.remove(from);
      }
    }
    pubsub.changed(from);
  }

  public long memory() {
    int mem = 2048;
    for (TreeMap<Integer, Integer> set : edges.values()) {
      mem += 256 + set.size() * 64;
    }
    return mem;
  }

  public TreeSet<Integer> traverse(TreeSet<Integer> left) {
    TreeSet<Integer> right = new TreeSet<>();
    for (int l : left) {
      TreeMap<Integer, Integer> pr = edges.get(l);
      if (pr != null) {
        right.addAll(pr.keySet());
      }
    }
    if (partials.size() > 0) {
      for (DifferentialEdgeTracker<?, ?> p : partials) {
        p.traverseInvalid(left, right);
      }
    }
    return right;
  }

  public void compute() {
    Iterator<DifferentialEdgeTracker<?, ?>> pit = partials.iterator();
    while (pit.hasNext()) {
      DifferentialEdgeTracker<?, ?> det = pit.next();
      if (det.alive()) {
        det.compute();
      } else {
        pit.remove();
        det.kill();
      }
    }
  }

  public void registerTracker(DifferentialEdgeTracker<?, ?> partial) {
    partials.add(partial);
  }

  public void registerTo(RxTable<TyTo> table) {
    tables.add(table);
  }

  public void __settle(Set<Integer> __viewers) {
    Iterator<RxTable<TyTo>> it = tables.iterator();
    while (it.hasNext()) {
      if (!it.next().__isAlive()) {
        it.remove();
      }
    }
  }

  public NtList<TyTo> map(NtList<? extends RxRecordBase<?>> list) {
    TreeSet<Integer> ids = new TreeSet<>();
    for (RxRecordBase<?> item : list) {
      int from = item.__id();
      ids.add(from);
      if (activeGuard != null) {
        activeGuard.readKey(from);
      }
    }
    ArrayList<TyTo> output = new ArrayList<>();
    TreeSet<Integer> result = traverse(ids);
    for (Integer out : result) {
      for (RxTable<TyTo> table : tables) {
        TyTo candidate = table.getById(out);
        if (candidate != null) {
          table.readPrimaryKey(out);
          output.add(candidate);
          break;
        }
      }
    }
    return new ArrayNtList<>(output);
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

  public void __subscribe(MapSubscription<?> ms) {
    pubsub.subscribe(ms);
  }
}
