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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/** a list backed by nothing */
public class EmptyNtList<T> implements NtList<T> {
  public EmptyNtList() {
  }

  @Override
  public void __delete() {
  }

  @Override
  public NtList<T> get() {
    return this;
  }

  @Override
  public NtMaybe<T> lookup(final int k) {
    return new NtMaybe<>();
  }

  @Override
  public NtMaybe<T> lookup(NtMaybe<Integer> k) {
    return new NtMaybe<>();
  }

  @Override
  public void map(final Consumer<T> t) {
  }

  @Override
  public <R> NtList<R> mapFunction(Function<T, R> foo) {
    return new EmptyNtList<>();
  }

  @Override
  public NtList<T> orderBy(final boolean done, final Comparator<T> cmp) {
    return this;
  }

  @Override
  public <TIn, TOut> NtMap<TIn, TOut> reduce(final Function<T, TIn> domain, final Function<NtList<T>, TOut> reducer) {
    return new NtMap<>();
  }

  @Override
  public NtList<T> shuffle(final boolean done, final Random rng) {
    return this;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public NtList<T> skip(final boolean done, final int skip) {
    return this;
  }

  @Override
  public NtList<T> limit(final boolean done, final int limit) {
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T[] toArray(final Function<Integer, Object> arrayMaker) {
    return (T[]) arrayMaker.apply(0);
  }

  @Override
  public <Out> NtList<Out> transform(final Function<T, Out> t) {
    return new EmptyNtList<>();
  }

  @Override
  public NtList<T> where(final boolean done, final WhereClause<T> filter) {
    return this;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public T next() {
        return null;
      }
    };
  }

  @Override
  public <KeyT> NtList<T> unique(ListUniqueMode mode, Function<T, KeyT> extract) {
    return this;
  }

  @Override
  public NtList<T> rank(Ranker<T> ranker) {
    return this;
  }
}
