/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.runtime.graph;

import ape.runtime.contracts.RxChild;
import ape.runtime.reactives.RxRecordBase;
import ape.runtime.reactives.RxTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

/** an assoc table is a container of edges between two tables drive by a source */
public class DifferentialEdgeTracker<B extends RxRecordBase<B>, T extends RxRecordBase<T>> implements RxChild {
  private final RxTable<B> source;
  private final EdgeMaker<B> maker;

  private class EdgeCache {
    private final int from;
    private final int to;

    private EdgeCache(int from, int to) {
      this.from = from;
      this.to = to;
    }
  }

  private final HashMap<Integer, EdgeCache> edgeCache;
  private final HashSet<Integer> invalid;
  private final RxAssocGraph<T> graph;

  public DifferentialEdgeTracker(RxTable<B> source, RxAssocGraph<T> graph, EdgeMaker<B> maker) {
    this.source = source;
    this.graph = graph;
    this.maker = maker;
    this.edgeCache = new HashMap<>();
    this.invalid = new HashSet<>();
    graph.registerTracker(this);
  }

  public void primaryKeyChange(int id) {
    EdgeCache ec = edgeCache.remove(id);
    if (ec != null) {
      graph.decr(ec.from, ec.to);
    }
    invalid.add(id);
  }

  public void traverseInvalid(TreeSet<Integer> left, TreeSet<Integer> right) {
    for (Integer id : invalid) {
      B row = source.getById(id);
      if (row != null && row.__isAlive()) {
        Integer from = maker.from(row);
        if (from != null ) {
          if (left.contains(from)) {
            Integer to = maker.to(row);
            if (to != null) {
              right.add(to);
            }
          }
        }
      }
    }
  }

  public void compute() {
    for (Integer id : invalid) {
      B row = source.getById(id);
      if (row != null && row.__isAlive()) {
        Integer from = maker.from(row);
        if (from != null) {
          Integer to = maker.to(row);
          if (to != null) {
            edgeCache.put(id, new EdgeCache(from, to));
            graph.incr(from, to);
          }
        }
      }
    }
    invalid.clear();
  }

  public void kill() {
    for (Map.Entry<Integer, EdgeCache> entry : edgeCache.entrySet()) {
      graph.decr(entry.getValue().from, entry.getValue().to);
    }
    edgeCache.clear();
    invalid.clear();
  }

  @Override
  public boolean __raiseInvalid() {
    for (Map.Entry<Integer, EdgeCache> entry : edgeCache.entrySet()) {
      invalid.add(entry.getKey());
      graph.decr(entry.getValue().from, entry.getValue().to);
    }
    edgeCache.clear();
    return source.__isAlive();
  }

  public boolean alive() {
    return source.__isAlive();
  }

  public long memory() {
    return 256 + edgeCache.size() * 64 + invalid.size() * 32;
  }
}
