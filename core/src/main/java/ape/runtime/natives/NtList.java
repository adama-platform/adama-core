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
package ape.runtime.natives;

import ape.runtime.contracts.Ranker;
import ape.runtime.contracts.WhereClause;
import ape.runtime.natives.lists.ListUniqueMode;

import java.util.Comparator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Functional list interface for query operations on table results.
 * Provides LINQ-style operations: where (filter), orderBy (sort), skip/limit
 * (pagination), shuffle (randomize), transform/map (projection), reduce
 * (aggregation), unique (deduplication), and rank (scoring). Operations
 * chain lazily where possible. Backed by SelectorRxObjectList for table
 * queries or ArrayNtList for materialized results.
 */
public interface NtList<Ty> extends Iterable<Ty> {
  void __delete();

  NtList<Ty> get();

  NtMaybe<Ty> lookup(int k);

  NtMaybe<Ty> lookup(NtMaybe<Integer> k);

  void map(Consumer<Ty> t);

  <R> NtList<R> mapFunction(Function<Ty, R> foo);

  NtList<Ty> orderBy(boolean done, Comparator<Ty> cmp);

  <TIn, TOut> NtMap<TIn, TOut> reduce(Function<Ty, TIn> domain, Function<NtList<Ty>, TOut> reducer);

  NtList<Ty> shuffle(boolean done, Random rng);

  int size();

  NtList<Ty> skip(boolean done, int skip);

  NtList<Ty> limit(boolean done, int limit);

  Ty[] toArray(Function<Integer, Object> arrayMaker);

  <Out> NtList<Out> transform(Function<Ty, Out> t);

  NtList<Ty> where(boolean done, WhereClause<Ty> filter);

  <KeyT> NtList<Ty> unique(ListUniqueMode mode, Function<Ty, KeyT> extract);

  NtList<Ty> rank(Ranker<Ty> ranker);
}
