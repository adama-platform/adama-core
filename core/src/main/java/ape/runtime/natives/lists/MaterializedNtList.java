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
package ape.runtime.natives.lists;

import ape.runtime.contracts.IndexQuerySet;
import ape.runtime.contracts.MultiIndexable;
import ape.runtime.contracts.Ranker;
import ape.runtime.contracts.WhereClause;
import ape.runtime.index.EvaluateLookupMode;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMap;
import ape.runtime.natives.NtMaybe;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MaterializedNtList<Ty extends MultiIndexable> implements NtList<Ty> {
  private final NtList<Ty> list;
  private final TreeMap<Integer, Ty> byIndex;
  private final TreeMap<Integer, TreeSet<Integer>>[] indicies;

  public MaterializedNtList(NtList<Ty> list, int numberIndicies) {
    this.list = list;
    this.byIndex = new TreeMap<>();
    this.indicies = new TreeMap[numberIndicies];
    for (int k = 0; k < numberIndicies; k++) {
      this.indicies[k] = new TreeMap<>();
    }
    int id = 0;
    for (Ty item : list) {
      byIndex.put(id, item);
      int[] vals = item.__getIndexValues();
      for (int k = 0; k < vals.length; k++) {
        TreeSet<Integer> indexValues = indicies[k].get(vals[k]);
        if (indexValues == null) {
          indexValues = new TreeSet<>();
          indicies[k].put(vals[k], indexValues);
        }
        indexValues.add(id);
      }
      id++;
    }
  }

  @Override
  public void __delete() {
    list.__delete();;
  }

  @Override
  public NtList<Ty> get() {
    return list;
  }

  @Override
  public NtMaybe<Ty> lookup(int k) {
    return list.lookup(k);
  }

  @Override
  public NtMaybe<Ty> lookup(NtMaybe<Integer> k) {
    return list.lookup(k);
  }

  @Override
  public void map(Consumer<Ty> t) {
    list.map(t);
  }

  @Override
  public <R> NtList<R> mapFunction(Function<Ty, R> foo) {
    return list.mapFunction(foo);
  }

  @Override
  public NtList<Ty> orderBy(boolean done, Comparator<Ty> cmp) {
    return list.orderBy(done, cmp);
  }

  @Override
  public <TIn, TOut> NtMap<TIn, TOut> reduce(Function<Ty, TIn> domain, Function<NtList<Ty>, TOut> reducer) {
    return list.reduce(domain, reducer);
  }

  @Override
  public NtList<Ty> shuffle(boolean done, Random rng) {
    return list.shuffle(done, rng);
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public NtList<Ty> skip(boolean done, int skip) {
    return list.skip(done, skip);
  }

  @Override
  public NtList<Ty> limit(boolean done, int limit) {
    return list.limit(done, limit);
  }

  @Override
  public Ty[] toArray(Function<Integer, Object> arrayMaker) {
    return list.toArray(arrayMaker);
  }

  @Override
  public <Out> NtList<Out> transform(Function<Ty, Out> t) {
    return list.transform(t);
  }

  @Override
  public NtList<Ty> where(boolean done, WhereClause<Ty> filter) {
    MaterializedIndexQuerySet miqs = new MaterializedIndexQuerySet();
    filter.scopeByIndicies(miqs);
    if (miqs.resultAll || miqs.resultComplete == null) {
      return list.where(done, filter);
    } else {
      ArrayList<Ty> results = new ArrayList<>();
      for (int id : miqs.resultComplete) {
        Ty item = byIndex.get(id);
        if (filter.test(item)) {
          results.add(item);
        }
      }
      return new ArrayNtList<>(results);
    }
  }

  @Override
  public Iterator<Ty> iterator() {
    return list.iterator();
  }

  private TreeSet<Integer> of(int column, int value, IndexQuerySet.LookupMode mode) {
    return EvaluateLookupMode.of(indicies[column], value, mode);
  }

  public class MaterializedIndexQuerySet extends IndexQuerySet {
    private boolean resultAll;
    private TreeSet<Integer> resultComplete;
    private boolean currentAll;
    private TreeSet<Integer> currentSet;

    public MaterializedIndexQuerySet() {
      this.resultAll = false;
      this.resultComplete = null;
      this.currentAll = true;
      this.currentSet = null;
    }

    @Override
    public void intersect(int column, int value, LookupMode mode) {
      if (currentAll) {
        currentAll = false;
        currentSet = of(column, value, mode);
      } else {
        TreeSet<Integer> result = new TreeSet<>();
        TreeSet<Integer> a = currentSet;
        TreeSet<Integer> b = of(column, value, mode);
        if (b.size() < a.size()) {
          a = b;
          b = currentSet;
        }
        for (Integer x : a) {
          if (b.contains(x)) {
            result.add(x);
          }
        }
        currentSet = result;
      }
    }

    @Override
    public void primary(int value) {
      if (currentAll) {
        currentAll = false;
        currentSet = new TreeSet<>();
        currentSet.add(value);
      } else {
        boolean keep = currentSet.contains(value);
        currentSet.clear();
        if (keep) {
          currentSet.add(value);
        }
      }
    }

    public void push() {
      if (currentAll) { // this branch has everything enabled, so the final result is turned fully on as well
        resultAll = true;
        resultComplete = null;
      } else {
        if (!resultAll) { // we haven't yet fully switched over to doing everything
          if (resultComplete == null) { // so union the complete branch with the current branch results
            resultComplete = currentSet;
          } else {
            resultComplete.addAll(currentSet);
          }
        }
      }
      this.currentAll = true;
      this.currentSet = null;
    }

    @Override
    public void finish() {
      push();
    }
  }

  @Override
  public <KeyT> NtList<Ty> unique(ListUniqueMode mode, Function<Ty, KeyT> extract) {
    return list.unique(mode, extract);
  }

  @Override
  public NtList<Ty> rank(Ranker<Ty> ranker) {
    return list.rank(ranker);
  }
}
