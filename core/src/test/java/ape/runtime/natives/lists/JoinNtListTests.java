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
import ape.runtime.contracts.Ranker;
import ape.runtime.contracts.WhereClause;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMap;
import ape.runtime.natives.NtMaybe;
import ape.runtime.stdlib.LibLists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class JoinNtListTests {

  public NtList<String> make(String... pp) {
    ArrayList<String> parts = new ArrayList<>();
    for(String p : pp) {
      parts.add(p);
    }
    return new ArrayNtList<>(parts);
  }

  @Test
  public void trivial() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f"));
    X.__delete();
    Assert.assertEquals(X, X.get());
    Assert.assertEquals(6, X.size());
  }

  @Test
  public void lookup() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f"));
    Assert.assertEquals("a", X.lookup(0).get());
    Assert.assertEquals("b", X.lookup(1).get());
    Assert.assertEquals("c", X.lookup(2).get());
    Assert.assertEquals("d", X.lookup(3).get());
    Assert.assertEquals("e", X.lookup(4).get());
    Assert.assertEquals("f", X.lookup(5).get());
  }

  @Test
  public void rank() {
    NtList<String> X = LibLists.join(make("aaa", "b", "ccc"), make("d", "eee", "f")).rank(new Ranker<String>() {
      @Override
      public double rank(String item) {
        return item.length();
      }

      @Override
      public double threshold() {
        return 2;
      }
    });
    Assert.assertEquals("aaa", X.lookup(0).get());
    Assert.assertEquals("ccc", X.lookup(1).get());
    Assert.assertEquals("eee", X.lookup(2).get());
  }

  @Test
  public void lookupm() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f"));
    Assert.assertEquals("a", X.lookup(new NtMaybe<>(0)).get());
    Assert.assertEquals("b", X.lookup(new NtMaybe<>(1)).get());
    Assert.assertEquals("c", X.lookup(new NtMaybe<>(2)).get());
    Assert.assertEquals("d", X.lookup(new NtMaybe<>(3)).get());
    Assert.assertEquals("e", X.lookup(new NtMaybe<>(4)).get());
    Assert.assertEquals("f", X.lookup(new NtMaybe<>(5)).get());
  }

  @Test
  public void map() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f"));
    ArrayList<String> p = new ArrayList<>();
    X.map((s) -> p.add(s));
    Assert.assertEquals("a", p.get(0));
    Assert.assertEquals("b", p.get(1));
    Assert.assertEquals("c", p.get(2));
    Assert.assertEquals("d", p.get(3));
    Assert.assertEquals("e", p.get(4));
    Assert.assertEquals("f", p.get(5));
  }

  @Test
  public void iterator() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f"));
    ArrayList<String> p = new ArrayList<>();
    for (String x : X) {
      p.add(x);
    }
    Assert.assertEquals("a", p.get(0));
    Assert.assertEquals("b", p.get(1));
    Assert.assertEquals("c", p.get(2));
    Assert.assertEquals("d", p.get(3));
    Assert.assertEquals("e", p.get(4));
    Assert.assertEquals("f", p.get(5));
  }

  @Test
  public void arr() {
    String[] X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).toArray((n) -> new String[n]);
    Assert.assertEquals("a", X[0]);
    Assert.assertEquals("b", X[1]);
    Assert.assertEquals("c", X[2]);
    Assert.assertEquals("d", X[3]);
    Assert.assertEquals("e", X[4]);
    Assert.assertEquals("f", X[5]);
  }

  @Test
  public void limitL() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).limit(true, 2);
    ArrayList<String> p = new ArrayList<>();
    for (String x : X) {
      p.add(x);
    }
    Assert.assertEquals("a", p.get(0));
    Assert.assertEquals("b", p.get(1));
  }

  @Test
  public void limitR() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).limit(true, 5);
    ArrayList<String> p = new ArrayList<>();
    for (String x : X) {
      p.add(x);
    }
    Assert.assertEquals("a", p.get(0));
    Assert.assertEquals("b", p.get(1));
    Assert.assertEquals("c", p.get(2));
    Assert.assertEquals("d", p.get(3));
    Assert.assertEquals("e", p.get(4));
  }

  @Test
  public void skipL() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).skip(true, 1);
    ArrayList<String> p = new ArrayList<>();
    for (String x : X) {
      p.add(x);
    }
    Assert.assertEquals("b", p.get(0));
    Assert.assertEquals("c", p.get(1));
    Assert.assertEquals("d", p.get(2));
    Assert.assertEquals("e", p.get(3));
    Assert.assertEquals("f", p.get(4));
  }

  @Test
  public void skipR() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).skip(true, 4);
    ArrayList<String> p = new ArrayList<>();
    for (String x : X) {
      p.add(x);
    }
    Assert.assertEquals("e", p.get(0));
    Assert.assertEquals("f", p.get(1));
  }

  @Test
  public void mapFunction() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).mapFunction((s) -> s + "-" + s);
    Assert.assertEquals("a-a", X.lookup(0).get());
    Assert.assertEquals("b-b", X.lookup(1).get());
    Assert.assertEquals("c-c", X.lookup(2).get());
    Assert.assertEquals("d-d", X.lookup(3).get());
    Assert.assertEquals("e-e", X.lookup(4).get());
    Assert.assertEquals("f-f", X.lookup(5).get());
  }

  @Test
  public void transform() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).transform((s) -> s + "-" + s);
    Assert.assertEquals("a-a", X.lookup(0).get());
    Assert.assertEquals("b-b", X.lookup(1).get());
    Assert.assertEquals("c-c", X.lookup(2).get());
    Assert.assertEquals("d-d", X.lookup(3).get());
    Assert.assertEquals("e-e", X.lookup(4).get());
    Assert.assertEquals("f-f", X.lookup(5).get());
  }

  @Test
  public void materialized() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).orderBy(true, (a, b) -> -a.compareTo(b));
    Assert.assertEquals("f", X.lookup(0).get());
    Assert.assertEquals("e", X.lookup(1).get());
    Assert.assertEquals("d", X.lookup(2).get());
    Assert.assertEquals("c", X.lookup(3).get());
    Assert.assertEquals("b", X.lookup(4).get());
    Assert.assertEquals("a", X.lookup(5).get());
  }

  @Test
  public void shuffle() {
    Random rng = new Random(42);
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).shuffle(true, rng);
    Assert.assertEquals("e", X.lookup(0).get());
    Assert.assertEquals("b", X.lookup(1).get());
    Assert.assertEquals("f", X.lookup(2).get());
    Assert.assertEquals("a", X.lookup(3).get());
    Assert.assertEquals("d", X.lookup(4).get());
    Assert.assertEquals("c", X.lookup(5).get());
  }

  @Test
  public void where() {
    NtList<String> X = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).where(true, new WhereClause<String>() {
      @Override
      public Integer getPrimaryKey() {
        return null;
      }

      @Override
      public void scopeByIndicies(IndexQuerySet __set) {

      }

      @Override
      public boolean test(String item) {
        return "b".equals(item) || "e".equals(item);
      }
    });
    Assert.assertEquals("b", X.lookup(0).get());
    Assert.assertEquals("e", X.lookup(1).get());
  }

  @Test
  public void reduce() {
    NtMap<String, Integer> result = LibLists.join(make("a", "b", "c"), make("d", "e", "f")).reduce((String t) -> t, (x) -> x.size());
    Assert.assertEquals(1, (int) result.storage.get("a"));
    Assert.assertEquals(1, (int) result.storage.get("b"));
    Assert.assertEquals(1, (int) result.storage.get("c"));
    Assert.assertEquals(1, (int) result.storage.get("d"));
    Assert.assertEquals(1, (int) result.storage.get("e"));
    Assert.assertEquals(1, (int) result.storage.get("f"));
  }

  @Test
  public void unique_last() {
    JoinNtList<ArrayNtListTests.UniqueSample> s = new JoinNtList<>(new ArrayNtList<>(ArrayNtListTests.samples(1, 2, 4, 5)),new ArrayNtList<>(ArrayNtListTests.samples(1, 3, 4, 6)));
    NtList<ArrayNtListTests.UniqueSample> result = s.unique(ListUniqueMode.Last, (x) -> x.a).orderBy(true, Comparator.comparingInt(a -> a.b));
    Assert.assertEquals(2, result.size());
    Assert.assertEquals(3, result.lookup(0).get().b);
    Assert.assertEquals(6, result.lookup(1).get().b);
  }

  @Test
  public void unique_first() {
    JoinNtList<ArrayNtListTests.UniqueSample> s = new JoinNtList<>(new ArrayNtList<>(ArrayNtListTests.samples(1, 2, 4, 5)),new ArrayNtList<>(ArrayNtListTests.samples(1, 3, 4, 6)));
    NtList<ArrayNtListTests.UniqueSample> result = s.unique(ListUniqueMode.First, (x) -> x.a).orderBy(true, Comparator.comparingInt(a -> a.b));
    Assert.assertEquals(2, result.size());
    Assert.assertEquals(2, result.lookup(0).get().b);
    Assert.assertEquals(5, result.lookup(1).get().b);
  }
}
