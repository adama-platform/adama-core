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

import ape.runtime.mocks.MockLivingDocument;
import ape.runtime.mocks.MockRecord;
import ape.runtime.mocks.MockRecordEdge;
import ape.runtime.mocks.MockRxParent;
import ape.runtime.natives.NtList;
import ape.runtime.natives.lists.ArrayNtList;
import ape.runtime.reactives.RxTable;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

public class RxAssocGraphTests {

  @Test
  public void mem() {
    RxAssocGraph<MockRecord> sg = new RxAssocGraph<MockRecord>();
    Assert.assertEquals(2048, sg.memory());
    sg.incr(5, 10);
    Assert.assertEquals(2368, sg.memory());
    sg.incr(5, 15);
    Assert.assertEquals(2432, sg.memory());
    sg.incr(2, 1);
    Assert.assertEquals(2752, sg.memory());
    sg.decr(5, 10);
    Assert.assertEquals(2688, sg.memory());
    sg.decr(5, 15);
    Assert.assertEquals(2368, sg.memory());
    sg.decr(5, 6);
    Assert.assertEquals(2368, sg.memory());
    sg.decr(2, 1);
    Assert.assertEquals(2048, sg.memory());
  }

  @Test
  public void empty() {
    RxAssocGraph<MockRecord> sg = new RxAssocGraph<MockRecord>();
    sg.incr(5, 10);
    sg.incr(2, 7);
    sg.incr(2, 4);
    TreeSet<Integer> input = new TreeSet<>();
    TreeSet<Integer> a = sg.traverse(input);
    Assert.assertEquals(0, a.size());
  }

  @Test
  public void union() {
    RxAssocGraph<MockRecord> sg = new RxAssocGraph<MockRecord>();
    sg.compute(); // no-op
    sg.incr(5, 10);
    sg.incr(2, 7);
    sg.incr(2, 4);
    sg.incr(5, 4);
    TreeSet<Integer> input = new TreeSet<>();
    input.add(2);
    input.add(5);
    input.add(10);
    TreeSet<Integer> a = sg.traverse(input);
    Assert.assertEquals(3, a.size());
    Assert.assertTrue(a.contains(10));
    Assert.assertTrue(a.contains(7));
    Assert.assertTrue(a.contains(4));
    sg.compute(); // no-op
  }

  @Test
  public void single() {
    RxAssocGraph<MockRecord> sg = new RxAssocGraph<MockRecord>();
    sg.incr(5, 10);
    sg.incr(2, 7);
    sg.incr(2, 4);
    sg.incr(5, 4);
    TreeSet<Integer> input = new TreeSet<>();
    input.add(2);
    TreeSet<Integer> a = sg.traverse(input);
    Assert.assertEquals(2, a.size());
    Assert.assertTrue(a.contains(7));
    Assert.assertTrue(a.contains(4));
    sg.compute(); // no-op
  }

  @Test
  public void ref_counts() {
    RxAssocGraph<MockRecord> sg = new RxAssocGraph<MockRecord>();
    sg.incr(5, 10);
    sg.incr(5, 10);
    sg.incr(5, 10);
    sg.incr(5, 10);
    {
      TreeSet<Integer> input = new TreeSet<>();
      input.add(5);
      TreeSet<Integer> a = sg.traverse(input);
      Assert.assertTrue(a.contains(10));
    }
    sg.decr(5, 10);
    sg.decr(5, 10);
    sg.decr(5, 10);
    {
      TreeSet<Integer> input = new TreeSet<>();
      input.add(5);
      TreeSet<Integer> a = sg.traverse(input);
      Assert.assertTrue(a.contains(10));
    }
    sg.decr(5, 10);
    {
      TreeSet<Integer> input = new TreeSet<>();
      input.add(5);
      TreeSet<Integer> a = sg.traverse(input);
      Assert.assertFalse(a.contains(10));
    }
    sg.compute(); // no-op
  }

  private MockRecordEdge makeEdge(RxTable<MockRecordEdge> records, int from, int to) {
    MockRecordEdge edge = records.make();
    edge.from = from;
    edge.to = to;
    return edge;
  }

  private TreeSet<Integer> leftOf(int... v) {
    TreeSet<Integer> vals = new TreeSet<>();
    for (int e : v) {
      vals.add(e);
    }
    return vals;
  }

  @Test
  public void differential_tracking() {
    RxAssocGraph<MockRecord> sg = new RxAssocGraph<MockRecord>();
    MockLivingDocument document = new MockLivingDocument();
    RxTable<MockRecordEdge> records = new RxTable<MockRecordEdge>(document, document, "R", parent -> new MockRecordEdge(parent), 0);
    DifferentialEdgeTracker det = new DifferentialEdgeTracker<MockRecordEdge, MockRecord>(records, sg, new EdgeMaker<MockRecordEdge>() {
      @Override
      public Integer from(MockRecordEdge row) {
        return row.from;
      }

      @Override
      public Integer to(MockRecordEdge row) {
        return row.to;
      }
    });
    records.pump(det);

    MockRecordEdge edge1 = makeEdge(records, 2, 5);
    MockRecordEdge edge2 = makeEdge(records, 2, 7);
    MockRecordEdge edge3 = makeEdge(records, 5, 9);
    MockRecordEdge edge4 = makeEdge(records, 9, 12);

    {
      TreeSet<Integer> result2 = sg.traverse(leftOf(2));
      Assert.assertTrue(result2.contains(5));
      Assert.assertTrue(result2.contains(7));
      Assert.assertEquals(2, result2.size());
    }
    sg.compute();
    {
      TreeSet<Integer> result2 = sg.traverse(leftOf(2));
      Assert.assertTrue(result2.contains(5));
      Assert.assertTrue(result2.contains(7));
      Assert.assertEquals(2, result2.size());
    }
    edge2.__kill();
    {
      TreeSet<Integer> result2 = sg.traverse(leftOf(2));
      Assert.assertTrue(result2.contains(5));
      Assert.assertEquals(1, result2.size());
    }
    sg.compute();
    {
      TreeSet<Integer> result2 = sg.traverse(leftOf(2));
      Assert.assertTrue(result2.contains(5));
      Assert.assertEquals(1, result2.size());
    }
    edge1.__kill();
    {
      TreeSet<Integer> result2 = sg.traverse(leftOf(2));
      Assert.assertEquals(0, result2.size());
    }
    sg.compute();
    {
      TreeSet<Integer> result2 = sg.traverse(leftOf(2));
      Assert.assertEquals(0, result2.size());
    }
  }

  private NtList<MockRecord> mockRecordListWithJustIds(int... ids) {
    ArrayList<MockRecord> records = new ArrayList<>();
    for (int id : ids) {
      MockRecord record = new MockRecord(null);
      record.id = id;
      records.add(record);
    }
    return new ArrayNtList<>(records);
  }

  @Test
  public void traverse_join() {
    RxAssocGraph<MockRecord> sg = new RxAssocGraph<MockRecord>();
    MockLivingDocument document = new MockLivingDocument();
    MockRxParent tableParent = new MockRxParent();
    RxTable<MockRecord> table1 = new RxTable<>(document, tableParent, "T", (parent) -> new MockRecord(parent), 1);
    sg.registerTo(table1);
    RxTable<MockRecord> table2 = new RxTable<>(document, tableParent, "T", (parent) -> new MockRecord(parent), 1);
    sg.registerTo(table2);
    RxTable<MockRecordEdge> records = new RxTable<MockRecordEdge>(document, document, "R", parent -> new MockRecordEdge(parent), 0);
    DifferentialEdgeTracker det = new DifferentialEdgeTracker<MockRecordEdge, MockRecord>(records, sg, new EdgeMaker<MockRecordEdge>() {
      @Override
      public Integer from(MockRecordEdge row) {
        return row.from;
      }

      @Override
      public Integer to(MockRecordEdge row) {
        return row.to;
      }
    });
    records.pump(det);

    MockRecordEdge edge1 = makeEdge(records, 2, table1.make().id);
    MockRecordEdge edge2 = makeEdge(records, 2, table1.make().id);
    MockRecordEdge edge3 = makeEdge(records, 5, table1.make().id);
    MockRecordEdge edge4 = makeEdge(records, 9, table2.make().id);

    {
      NtList<MockRecord> output = sg.map(mockRecordListWithJustIds(2, 9)).orderBy(true, Comparator.comparingInt((MockRecord a) -> a.id));
      Assert.assertEquals(3, output.size());
      Assert.assertEquals(1, output.lookup(0).get().id);
      Assert.assertEquals(3, output.lookup(1).get().id);
      Assert.assertEquals(7, output.lookup(2).get().id);
    }
    {
      NtList<MockRecord> output = sg.map(mockRecordListWithJustIds(1, 3)).orderBy(true, Comparator.comparingInt((MockRecord a) -> a.id));
      Assert.assertEquals(0, output.size());
    }
    {
      NtList<MockRecord> output = sg.map(mockRecordListWithJustIds(5)).orderBy(true, Comparator.comparingInt((MockRecord a) -> a.id));
      Assert.assertEquals(1, output.size());
      Assert.assertEquals(5, output.lookup(0).get().id);
    }
    tableParent.alive = false;
    sg.__settle(null);
    {
      NtList<MockRecord> output = sg.map(mockRecordListWithJustIds(1, 2, 9, 5)).orderBy(true, Comparator.comparingInt((MockRecord a) -> a.id));
      Assert.assertEquals(0, output.size());
    }
  }
}
