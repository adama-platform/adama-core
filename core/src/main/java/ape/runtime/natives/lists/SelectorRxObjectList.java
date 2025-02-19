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

import ape.runtime.contracts.Ranker;
import ape.runtime.contracts.WhereClause;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMap;
import ape.runtime.natives.NtMaybe;
import ape.runtime.reactives.RxRecordBase;
import ape.runtime.reactives.RxTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/** adapts a table to a list; the birthplace for the query optimized stuff */
public class SelectorRxObjectList<Ty extends RxRecordBase<Ty>> implements NtList<Ty> {
  private final RxTable<Ty> table;
  private WhereClause<Ty> filter;
  private ArrayList<Ty> finalized;

  public SelectorRxObjectList(final RxTable<Ty> table) {
    this.table = table;
    this.filter = null;
  }

  @Override
  public void __delete() {
    ensureFinalized();
    for (final Ty item : finalized) {
      item.__delete();
    }
    table.__raiseDirty();
  }

  private void ensureFinalized() {
    if (this.finalized == null) {
      int cost = 0;
      finalized = new ArrayList<>();
      if (filter != null) {
        for (final Ty item : table.scan(filter)) {
          cost++;
          if (item.__isDying()) {
            continue;
          }
          if (filter.test(item)) {
            finalized.add(item);
          }
        }
      } else {
        table.readAll();
        for (final Ty item : table) {
          cost++;
          if (item.__isDying()) {
            continue;
          }
          finalized.add(item);
        }
      }
      table.__cost(cost);
    }
  }

  @Override
  public NtList<Ty> get() {
    return this;
  }

  @Override
  public NtMaybe<Ty> lookup(final int k) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).lookup(k);
  }

  @Override
  public NtMaybe<Ty> lookup(NtMaybe<Integer> k) {
    if (k.has()) {
      return lookup(k.get());
    }
    return new NtMaybe<>();
  }

  @Override
  public void map(final Consumer<Ty> t) {
    ensureFinalized();
    for (final Ty item : finalized) {
      t.accept(item);
    }
  }

  @Override
  public <R> NtList<R> mapFunction(Function<Ty, R> foo) {
    ensureFinalized();
    ArrayList<R> result = new ArrayList<>();
    for (final Ty item : finalized) {
      result.add(foo.apply(item));
    }
    return new ArrayNtList<>(result);
  }

  @Override
  public NtList<Ty> orderBy(final boolean done, final Comparator<Ty> cmp) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).orderBy(true, cmp);
  }

  @Override
  public <TIn, TOut> NtMap<TIn, TOut> reduce(final Function<Ty, TIn> domain, final Function<NtList<Ty>, TOut> reducer) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).reduce(domain, reducer);
  }

  @Override
  public NtList<Ty> shuffle(final boolean done, final Random rng) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).shuffle(true, rng);
  }

  @Override
  public int size() {
    // should this be optimized... the deletion mechanism kind of sucks
    ensureFinalized();
    return finalized.size();
  }

  @Override
  public NtList<Ty> skip(final boolean done, final int skip) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).skip(true, skip);
  }

  @Override
  public NtList<Ty> limit(final boolean done, final int limit) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).limit(true, limit);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Ty[] toArray(final Function<Integer, Object> arrayMaker) {
    ensureFinalized();
    return finalized.toArray((Ty[]) arrayMaker.apply(finalized.size()));
  }

  @Override
  public <Out> NtList<Out> transform(final Function<Ty, Out> t) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).transform(t);
  }

  @Override
  public NtList<Ty> where(final boolean done, final WhereClause<Ty> filter) {
    if (filter.getPrimaryKey() != null) {
      table.readPrimaryKey(filter.getPrimaryKey());
      final var primary = table.getById(filter.getPrimaryKey());
      finalized = new ArrayList<>(0);
      if (primary != null) {
        if (!primary.__isDying()) {
          finalized.add(primary);
        }
      }
      return new ArrayNtList<>(finalized).where(true, filter);
    }
    this.filter = filter;
    ensureFinalized();
    return new ArrayNtList<>(finalized);
  }

  @Override
  public Iterator<Ty> iterator() {
    ensureFinalized();
    return finalized.iterator();
  }

  @Override
  public <KeyT> NtList<Ty> unique(ListUniqueMode mode, Function<Ty, KeyT> extract) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).unique(mode, extract);
  }

  @Override
  public NtList<Ty> rank(Ranker<Ty> ranker) {
    ensureFinalized();
    return new ArrayNtList<>(finalized).rank(ranker);
  }
}
