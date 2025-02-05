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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class JoinNtList<Ty> implements NtList<Ty> {
  private final NtList<Ty> left;
  private final NtList<Ty> right;

  public JoinNtList(NtList<Ty> left, NtList<Ty> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public void __delete() {
    left.__delete();
    right.__delete();
  }

  @Override
  public NtList<Ty> get() {
    return this;
  }

  @Override
  public NtMaybe<Ty> lookup(int k) {
    int n = left.size();
    if (k < n) {
      return left.lookup(k);
    } else {
      return right.lookup(k - n);
    }
  }

  @Override
  public NtMaybe<Ty> lookup(NtMaybe<Integer> k) {
    if (k.has()) {
      return lookup(k.get());
    } else {
      return new NtMaybe<>();
    }
  }

  @Override
  public void map(Consumer<Ty> t) {
    left.map(t);
    right.map(t);
  }

  @Override
  public <R> NtList<R> mapFunction(Function<Ty, R> foo) {
    return new JoinNtList<>(left.mapFunction(foo), right.mapFunction(foo));
  }

  private ArrayNtList<Ty> materialize() {
    ArrayList<Ty> vals = new ArrayList<>();
    for (Ty val : left) {
      vals.add(val);
    }
    for (Ty val : right) {
      vals.add(val);
    }
    return new ArrayNtList<>(vals);
  }

  @Override
  public NtList<Ty> orderBy(boolean done, Comparator<Ty> cmp) {
    return materialize().orderBy(done, cmp);
  }

  @Override
  public <TIn, TOut> NtMap<TIn, TOut> reduce(Function<Ty, TIn> domain, Function<NtList<Ty>, TOut> reducer) {
    return materialize().reduce(domain, reducer);
  }

  @Override
  public NtList<Ty> shuffle(boolean done, Random rng) {
    return materialize().shuffle(done, rng);
  }

  @Override
  public int size() {
    return left.size() + right.size();
  }

  @Override
  public NtList<Ty> skip(boolean done, int skip) {
    int n = left.size();
    if (skip < n) {
      return new JoinNtList<>(left.skip(done, skip), right);
    } else {
      return right.skip(done, skip - n);
    }
  }

  @Override
  public NtList<Ty> limit(boolean done, int limit) {
    int n = left.size();
    if (limit < n) {
      return left.limit(done, limit);
    } else {
      return new JoinNtList<>(left, right.limit(done, limit - n));
    }
  }

  @Override
  public Ty[] toArray(Function<Integer, Object> arrayMaker) {
    Ty[] arr = (Ty[]) arrayMaker.apply(left.size() + right.size());
    int at = 0;
    Iterator<Ty> it = iterator();
    while (it.hasNext()) {
      arr[at] = it.next();
      at++;
    }
    return arr;
  }

  @Override
  public <Out> NtList<Out> transform(Function<Ty, Out> t) {
    return new JoinNtList<>(left.transform(t), right.transform(t));
  }

  @Override
  public NtList<Ty> where(boolean done, WhereClause<Ty> filter) {
    return new JoinNtList<>(left.where(done, filter), right.where(done, filter));
  }

  @Override
  public Iterator<Ty> iterator() {
    return new Iterator<Ty>() {
      Iterator<Ty> a = left.iterator();
      Iterator<Ty> b = right.iterator();
      boolean useA = true;

      @Override
      public boolean hasNext() {
        if (useA) {
          if (a.hasNext()) {
            return true;
          } else {
            useA = false;
          }
        }
        return b.hasNext();
      }

      @Override
      public Ty next() {
        if (useA) {
          return a.next();
        } else {
          return b.next();
        }
      }
    };
  }

  @Override
  public <KeyT> NtList<Ty> unique(ListUniqueMode mode, Function<Ty, KeyT> extract) {
    return materialize().unique(mode, extract);
  }

  @Override
  public NtList<Ty> rank(Ranker<Ty> ranker) {
    ArrayList<Ty> sum = new ArrayList<>();
    for (Ty item : left) {
      sum.add(item);
    }
    for (Ty item : right) {
      sum.add(item);
    }
    return new ArrayNtList<>(sum).rank(ranker);
  }
}
