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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/** a list backed by an array */
public class ArrayNtList<Ty> implements NtList<Ty> {
  private final ArrayList<Ty> list;

  public ArrayNtList(final ArrayList<Ty> list) {
    this.list = list;
  }

  @Override
  public void __delete() {
    for (final Ty item : list) {
      if (item instanceof RxRecordBase) {
        ((RxRecordBase) item).__delete();
      }
    }
  }

  @Override
  public NtList<Ty> get() {
    return this;
  }

  @Override
  public NtMaybe<Ty> lookup(final int k) {
    final var result = new NtMaybe<Ty>();
    if (0 <= k && k < list.size()) {
      result.set(list.get(k));
    }
    return result;
  }

  @Override
  public NtMaybe<Ty> lookup(NtMaybe<Integer> k) {
    if (k.has()) {
      return lookup(k.get());
    }
    return new NtMaybe<Ty>();
  }

  @Override
  public void map(final Consumer<Ty> t) {
    for (final Ty item : list) {
      t.accept(item);
    }
  }

  @Override
  public <R> NtList<R> mapFunction(Function<Ty, R> foo) {
    ArrayList<R> result = new ArrayList<>();
    for (final Ty item : list) {
      result.add(foo.apply(item));
    }
    return new ArrayNtList<>(result);
  }

  @Override
  public NtList<Ty> orderBy(final boolean done, final Comparator<Ty> cmp) {
    list.sort(cmp);
    return this;
  }

  @Override
  public <TIn, TOut> NtMap<TIn, TOut> reduce(final Function<Ty, TIn> domainExtract, final Function<NtList<Ty>, TOut> reducer) {
    final var map = new NtMap<TIn, TOut>();
    final var shredded = new TreeMap<TIn, ArrayList<Ty>>();
    for (final Ty item : list) {
      final var domain = domainExtract.apply(item);
      var bucket = shredded.get(domain);
      if (bucket == null) {
        bucket = new ArrayList<>();
        shredded.put(domain, bucket);
      }
      bucket.add(item);
    }
    for (final Map.Entry<TIn, ArrayList<Ty>> entry : shredded.entrySet()) {
      final var value = reducer.apply(new ArrayNtList<>(entry.getValue()));
      map.storage.put(entry.getKey(), value);
    }
    return map;
  }

  @Override
  public NtList<Ty> shuffle(final boolean done, final Random rng) {
    for (var k = list.size() - 1; k >= 0; k--) {
      final var swapWith = rng.nextInt(list.size());
      if (swapWith != k) {
        final var t = list.get(k);
        list.set(k, list.get(swapWith));
        list.set(swapWith, t);
      }
    }
    return this;
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public NtList<Ty> skip(final boolean done, final int skip) {
    final var next = new ArrayList<Ty>();
    final var it = iterator();
    for (var k = 0; k < skip && it.hasNext(); k++) {
      it.next();
    }
    while (it.hasNext()) {
      next.add(it.next());
    }
    return new ArrayNtList<>(next);
  }

  @Override
  public NtList<Ty> limit(final boolean done, final int limit) {
    final var next = new ArrayList<Ty>(limit);
    final var it = iterator();
    for (var k = 0; k < limit && it.hasNext(); k++) {
      next.add(it.next());
    }
    return new ArrayNtList<>(next);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Ty[] toArray(final Function<Integer, Object> arrayMaker) {
    return list.toArray((Ty[]) arrayMaker.apply(list.size()));
  }

  @Override
  public <Out> NtList<Out> transform(final Function<Ty, Out> t) {
    final var output = new ArrayList<Out>(list.size());
    for (final Ty item : list) {
      output.add(t.apply(item));
    }
    return new ArrayNtList<>(output);
  }

  @Override
  public NtList<Ty> where(final boolean done, final WhereClause<Ty> filter) {
    final var next = new ArrayList<Ty>();
    for (final Ty item : list) {
      if (filter.test(item)) {
        next.add(item);
      }
    }
    return new ArrayNtList<>(next);
  }

  @Override
  public Iterator<Ty> iterator() {
    return list.iterator();
  }

  @Override
  public <KeyT> NtList<Ty> unique(ListUniqueMode mode, Function<Ty, KeyT> extract) {
    switch (mode) {
      case First: {
        TreeSet<KeyT> seen = new TreeSet<>();
        ArrayList<Ty> results = new ArrayList<>();
        for (Ty item : list) {
          KeyT key = extract.apply(item);
          if (!seen.contains(key)) {
            seen.add(key);
            results.add(item);
          }
        }
        return new ArrayNtList<>(results);
      }
      default:
      case Last: {
        HashMap<KeyT, Ty> last = new HashMap<>();
        for (Ty item : list) {
          last.put(extract.apply(item), item);
        }
        return new ArrayNtList<>(new ArrayList<>(last.values()));
      }
    }
  }

  private class ItemWithRank {
    private final Ty item;
    private final double score;

    private ItemWithRank(Ty item, double score) {
      this.item = item;
      this.score = score;
    }
  }

  @Override
  public NtList<Ty> rank(Ranker<Ty> ranker) {
    ArrayList<ItemWithRank> scored = new ArrayList<>(list.size());
    double threshold = ranker.threshold();
    for (Ty item : list) {
      double rank = ranker.rank(item);
      if (rank >= threshold) {
        scored.add(new ItemWithRank(item, rank));
      }
    }
    scored.sort(Comparator.comparingDouble((ItemWithRank x) -> -x.score));
    ArrayList<Ty> next = new ArrayList<>(scored.size());
    for (ItemWithRank item : scored) {
      next.add(item.item);
    }
    return new ArrayNtList<>(next);
  }
}
