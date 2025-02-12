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
import ape.runtime.exceptions.AbortMessageException;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtMessageBase;
import ape.runtime.natives.algo.HashBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class MaterializedNtListTests {

  public class SortableMessage extends NtMessageBase {
    public int x;
    public int y;
    public int id;

    @Override
    public String[] __getIndexColumns() {
      return new String[] { "x" };
    }

    @Override
    public int[] __getIndexValues() {
      return new int[] { x, y };
    }

    @Override
    public void __hash(HashBuilder __hash) {
    }

    @Override
    public void __writeOut(JsonStreamWriter writer) {
    }

    @Override
    public void __ingest(JsonStreamReader reader) {
    }

    @Override
    public long __memory() {
      return 100000;
    }

    @Override
    public void __parsed() throws AbortMessageException {
    }
  }

  ArrayNtList<SortableMessage> demo() {
    ArrayList<SortableMessage> list = new ArrayList<>();
    for (int k = 0; k < 17 * 125; k++) {
      SortableMessage msg = new SortableMessage();
      msg.x = k % 17;
      msg.y = (3 * k) % 5;
      msg.id = k;
      list.add(msg);
    }
    return new ArrayNtList<SortableMessage>(list);
  }

  @Test
  public void flow() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    list.get();
    Assert.assertTrue(list.lookup(0).has());
    Assert.assertTrue(list.lookup(new NtMaybe<>(0)).has());
    list.map((t) -> {});
    list.mapFunction((t) -> t.id);
    list.transform((t) -> t.id);
    list.orderBy(true, Comparator.comparingInt(a -> a.id));
    list.reduce((t) -> t.x, (r) -> r);
    list.shuffle(true, new Random());
    Assert.assertEquals(17 * 125, list.size());
    Assert.assertEquals(17 * 125 - 1000, list.skip(true, 1000).size());
    Assert.assertEquals(100, list.limit(true, 100).size());
    list.toArray((n) -> new SortableMessage[n]);
    list.iterator();
    list.__delete();
    list.rank(new Ranker<SortableMessage>() {
      @Override
      public double rank(SortableMessage item) {
        return 1;
      }

      @Override
      public double threshold() {
        return 0;
      }
    });
  }

  @Test
  public void noscoping_x_eq_15() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    NtList<SortableMessage> noScoping = list.where(true, new WhereClause<SortableMessage>() {

      @Override
      public Integer getPrimaryKey() {
        throw new IllegalStateException();
      }

      @Override
      public void scopeByIndicies(IndexQuerySet __set) {
      }

      @Override
      public boolean test(SortableMessage item) {
        return item.x == 15;
      }
    });
    Assert.assertEquals(125, noScoping.size());
  }

  @Test
  public void noscoping_x_eq_15_and_y_eq_3() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    NtList<SortableMessage> noScoping = list.where(true, new WhereClause<SortableMessage>() {

      @Override
      public Integer getPrimaryKey() {
        throw new IllegalStateException();
      }

      @Override
      public void scopeByIndicies(IndexQuerySet __set) {
        __set.finish();
      }

      @Override
      public boolean test(SortableMessage item) {
        return item.x == 15 && item.y == 3;
      }
    });
    Assert.assertEquals(25, noScoping.size());
  }

  @Test
  public void scoping_primary() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    for (int scope = 0; scope < 17; scope++) {
      int scopeToUse = scope;
      NtList<SortableMessage> result = list.where(true, new WhereClause<SortableMessage>() {
        @Override
        public Integer getPrimaryKey() {
          throw new IllegalStateException();
        }

        @Override
        public void scopeByIndicies(IndexQuerySet __set) {
          __set.primary(scopeToUse);
          __set.finish();
        }

        @Override
        public boolean test(SortableMessage item) {
          return true;
        }
      });
      Assert.assertEquals(1, result.size());
    }
  }

  @Test
  public void scoping_primary_after_good() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    NtList<SortableMessage> result = list.where(true, new WhereClause<SortableMessage>() {
      @Override
      public Integer getPrimaryKey() {
        throw new IllegalStateException();
      }

      @Override
      public void scopeByIndicies(IndexQuerySet __set) {
        __set.intersect(0, 1, IndexQuerySet.LookupMode.Equals);
        __set.primary(1);
        __set.finish();
      }

      @Override
      public boolean test(SortableMessage item) {
        return true;
      }
    });
    Assert.assertEquals(1, result.size());
  }

  @Test
  public void scoping_primary_after_bad() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    NtList<SortableMessage> result = list.where(true, new WhereClause<SortableMessage>() {
      @Override
      public Integer getPrimaryKey() {
        throw new IllegalStateException();
      }

      @Override
      public void scopeByIndicies(IndexQuerySet __set) {
        __set.intersect(0, 2, IndexQuerySet.LookupMode.Equals);
        __set.primary(1);
        __set.finish();
      }

      @Override
      public boolean test(SortableMessage item) {
        return true;
      }
    });
    Assert.assertEquals(0, result.size());
  }

  @Test
  public void scoping_all_x() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    for (int scope = 0; scope < 17; scope++) {
      int scopeToUse = scope;
      NtList<SortableMessage> result = list.where(true, new WhereClause<SortableMessage>() {
        @Override
        public Integer getPrimaryKey() {
          throw new IllegalStateException();
        }

        @Override
        public void scopeByIndicies(IndexQuerySet __set) {
          __set.intersect(0, scopeToUse, IndexQuerySet.LookupMode.Equals);
          __set.finish();
        }

        @Override
        public boolean test(SortableMessage item) {
          return true;
        }
      });
      Assert.assertEquals(125, result.size());
    }
  }

  @Test
  public void scoping_all_x_and_y() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    for (int scopeX = 0; scopeX < 17; scopeX++) {
      for (int scopeY = 0; scopeY < 5; scopeY++) {
        int scopeXToUse = scopeX;
        int scopeYToUse = scopeY;
        NtList<SortableMessage> result = list.where(true, new WhereClause<SortableMessage>() {
          @Override
          public Integer getPrimaryKey() {
            throw new IllegalStateException();
          }

          @Override
          public void scopeByIndicies(IndexQuerySet __set) {
            __set.intersect(0, scopeXToUse, IndexQuerySet.LookupMode.Equals);
            __set.intersect(1, scopeYToUse, IndexQuerySet.LookupMode.Equals);
            __set.finish();
          }

          @Override
          public boolean test(SortableMessage item) {
            Assert.assertEquals(item.x, scopeXToUse);
            Assert.assertEquals(item.y, scopeYToUse);
            return true;
          }
        });
        Assert.assertEquals(25, result.size());
      }
    }
  }

  @Test
  public void scoping_all_y_and_x() {
    MaterializedNtList<SortableMessage> list = new MaterializedNtList<>(demo(), 2);
    for (int scopeX = 0; scopeX < 17; scopeX++) {
      for (int scopeY = 0; scopeY < 5; scopeY++) {
        int scopeXToUse = scopeX;
        int scopeYToUse = scopeY;
        NtList<SortableMessage> result = list.where(true, new WhereClause<SortableMessage>() {
          @Override
          public Integer getPrimaryKey() {
            throw new IllegalStateException();
          }

          @Override
          public void scopeByIndicies(IndexQuerySet __set) {
            __set.intersect(1, scopeYToUse, IndexQuerySet.LookupMode.Equals);
            __set.intersect(0, scopeXToUse, IndexQuerySet.LookupMode.Equals);
            __set.finish();
          }

          @Override
          public boolean test(SortableMessage item) {
            Assert.assertEquals(item.x, scopeXToUse);
            Assert.assertEquals(item.y, scopeYToUse);
            return true;
          }
        });
        Assert.assertEquals(25, result.size());
      }
    }
  }

  @Test
  public void unique_last() {
    MaterializedNtList<ArrayNtListTests.UniqueSample> s = new MaterializedNtList<>(new ArrayNtList<>(ArrayNtListTests.samples(1, 2, 1, 3, 4, 5, 4, 6)), 0);
    NtList<ArrayNtListTests.UniqueSample> result = s.unique(ListUniqueMode.Last, (x) -> x.a).orderBy(true, Comparator.comparingInt(a -> a.b));
    Assert.assertEquals(2, result.size());
    Assert.assertEquals(3, result.lookup(0).get().b);
    Assert.assertEquals(6, result.lookup(1).get().b);
  }

  @Test
  public void unique_first() {
    MaterializedNtList<ArrayNtListTests.UniqueSample> s = new MaterializedNtList<>(new ArrayNtList<>(ArrayNtListTests.samples(1, 2, 1, 3, 4, 5, 4, 6)), 0);
    NtList<ArrayNtListTests.UniqueSample> result = s.unique(ListUniqueMode.First, (x) -> x.a).orderBy(true, Comparator.comparingInt(a -> a.b));
    Assert.assertEquals(2, result.size());
    Assert.assertEquals(2, result.lookup(0).get().b);
    Assert.assertEquals(5, result.lookup(1).get().b);
  }
}
