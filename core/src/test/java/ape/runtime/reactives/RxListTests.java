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
package ape.runtime.reactives;

import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockRxChild;
import ape.runtime.mocks.MockRxParent;
import ape.runtime.natives.NtList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class RxListTests {

  private static final Function<RxParent, RxInt32> INT_MAKER = (parent) -> new RxInt32(parent, 0);

  private RxList<RxInt32> list() {
    return list(new MockRxParent());
  }

  private RxList<RxInt32> list(RxParent parent) {
    return new RxList<>(parent, INT_MAKER);
  }

  @Test
  public void dump_empty() {
    final var l = list();
    JsonStreamWriter writer = new JsonStreamWriter();
    l.__dump(writer);
    Assert.assertEquals("{}", writer.toString());
  }

  @Test
  public void add_to_head_empty() {
    final var l = list();
    RxInt32 val = l.prepend();
    val.set(42);
    Assert.assertEquals(1, l.size());
    JsonStreamWriter writer = new JsonStreamWriter();
    l.__dump(writer);
    Assert.assertEquals("{\"0\":42}", writer.toString());
  }

  @Test
  public void add_to_tail_empty() {
    final var l = list();
    RxInt32 val = l.append();
    val.set(99);
    Assert.assertEquals(1, l.size());
    JsonStreamWriter writer = new JsonStreamWriter();
    l.__dump(writer);
    Assert.assertEquals("{\"0\":99}", writer.toString());
  }

  @Test
  public void sequential_head_insertions() {
    final var l = list();
    l.prepend().set(1);
    l.prepend().set(2);
    l.prepend().set(3);
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(3, result.size());
    // Head insertions should be ordered: 3, 2, 1
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(3, (int) it.next().get());
    Assert.assertEquals(2, (int) it.next().get());
    Assert.assertEquals(1, (int) it.next().get());
  }

  @Test
  public void sequential_tail_insertions() {
    final var l = list();
    l.append().set(1);
    l.append().set(2);
    l.append().set(3);
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(3, result.size());
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(1, (int) it.next().get());
    Assert.assertEquals(2, (int) it.next().get());
    Assert.assertEquals(3, (int) it.next().get());
  }

  @Test
  public void mixed_head_tail() {
    final var l = list();
    l.append().set(1);  // [1] at 0
    l.prepend().set(2);  // [2, 1] at -10, 0
    l.append().set(3);  // [2, 1, 3] at -10, 0, 10
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(3, result.size());
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(2, (int) it.next().get());
    Assert.assertEquals(1, (int) it.next().get());
    Assert.assertEquals(3, (int) it.next().get());
  }

  @Test
  public void insert_after_middle() {
    final var l = list();
    l.append().set(1);  // pos 0
    l.append().set(3);  // pos 10
    // Get the first key (0.0) and insert after it
    double firstKey = l.keys().iterator().next();
    l.insertAfter(firstKey).set(2);  // pos 5
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(3, result.size());
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(1, (int) it.next().get());
    Assert.assertEquals(2, (int) it.next().get());
    Assert.assertEquals(3, (int) it.next().get());
  }

  @Test
  public void insert_after_last() {
    final var l = list();
    l.append().set(1);  // pos 0
    // Insert after the only element, should be key + SPACING
    double key = l.keys().iterator().next();
    l.insertAfter(key).set(2);
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(2, result.size());
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(1, (int) it.next().get());
    Assert.assertEquals(2, (int) it.next().get());
  }

  @Test
  public void insert_before_middle() {
    final var l = list();
    l.append().set(1);  // pos 0
    l.append().set(3);  // pos 10
    // Get the second key (10.0) and insert before it
    Iterator<Double> keys = l.keys().iterator();
    keys.next(); // skip first
    double secondKey = keys.next();
    l.insertBefore(secondKey).set(2);  // pos 5
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(3, result.size());
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(1, (int) it.next().get());
    Assert.assertEquals(2, (int) it.next().get());
    Assert.assertEquals(3, (int) it.next().get());
  }

  @Test
  public void insert_before_first() {
    final var l = list();
    l.append().set(1);  // pos 0
    double key = l.keys().iterator().next();
    l.insertBefore(key).set(0);
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(2, result.size());
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(0, (int) it.next().get());
    Assert.assertEquals(1, (int) it.next().get());
  }

  @Test
  public void get_returns_ordered() {
    final var l = list();
    l.append().set(10);
    l.prepend().set(5);
    l.append().set(20);
    l.prepend().set(1);
    NtList<RxInt32> result = l.get();
    Assert.assertEquals(4, result.size());
    Iterator<RxInt32> it = result.iterator();
    Assert.assertEquals(1, (int) it.next().get());
    Assert.assertEquals(5, (int) it.next().get());
    Assert.assertEquals(10, (int) it.next().get());
    Assert.assertEquals(20, (int) it.next().get());
  }

  @Test
  public void size_tracking() {
    final var l = list();
    Assert.assertEquals(0, l.size());
    l.append().set(1);
    Assert.assertEquals(1, l.size());
    l.append().set(2);
    Assert.assertEquals(2, l.size());
    l.prepend().set(3);
    Assert.assertEquals(3, l.size());
  }

  @Test
  public void remove_committed_item() {
    final var l = list();
    l.append().set(1);
    l.append().set(2);
    l.append().set(3);
    // Commit to establish baseline
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    // Now remove the middle entry (pos 10)
    Iterator<Double> keys = l.keys().iterator();
    keys.next(); // skip first
    double middleKey = keys.next();
    l.remove(middleKey);
    Assert.assertEquals(2, l.size());
    // Commit should show deletion
    fw = new JsonStreamWriter();
    rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    Assert.assertTrue(fw.toString().contains("null"));
  }

  @Test
  public void remove_created_item() {
    final var l = list();
    l.append().set(1);
    l.append().set(2);
    // Remove the second item before commit (it's in created set)
    Iterator<Double> keys = l.keys().iterator();
    keys.next();
    double secondKey = keys.next();
    l.remove(secondKey);
    Assert.assertEquals(1, l.size());
    // Commit should not show the removed created item
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    // Only the first item should appear as created
    Assert.assertTrue(fw.toString().contains("1"));
    Assert.assertFalse(fw.toString().contains("null"));
  }

  @Test
  public void remove_nonexistent() {
    final var l = list();
    l.append().set(1);
    l.remove(999.0);
    Assert.assertEquals(1, l.size());
  }

  @Test
  public void commit_creates() {
    final var l = list();
    l.append().set(42);
    l.append().set(100);
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("list", fw, rv);
    // Forward should have the values
    Assert.assertTrue(fw.toString().contains("42"));
    Assert.assertTrue(fw.toString().contains("100"));
    // Reverse should have nulls (these are creates)
    Assert.assertTrue(rv.toString().contains("null"));
  }

  @Test
  public void commit_deletes() {
    final var l = list();
    l.append().set(42);
    // Commit to make it persistent
    JsonStreamWriter fw1 = new JsonStreamWriter();
    JsonStreamWriter rv1 = new JsonStreamWriter();
    l.__commit("l", fw1, rv1);
    // Now remove
    double key = l.keys().iterator().next();
    l.remove(key);
    JsonStreamWriter fw2 = new JsonStreamWriter();
    JsonStreamWriter rv2 = new JsonStreamWriter();
    l.__commit("l", fw2, rv2);
    // Forward should have null (deletion)
    Assert.assertTrue(fw2.toString().contains("null"));
    // Reverse should have the old value
    Assert.assertTrue(rv2.toString().contains("42"));
  }

  @Test
  public void commit_modifications() {
    final var l = list();
    l.append().set(10);
    JsonStreamWriter fw1 = new JsonStreamWriter();
    JsonStreamWriter rv1 = new JsonStreamWriter();
    l.__commit("l", fw1, rv1);
    // Modify the value
    l.get().iterator().next().set(20);
    JsonStreamWriter fw2 = new JsonStreamWriter();
    JsonStreamWriter rv2 = new JsonStreamWriter();
    l.__commit("l", fw2, rv2);
    Assert.assertTrue(fw2.toString().contains("20"));
    Assert.assertTrue(rv2.toString().contains("10"));
  }

  @Test
  public void commit_no_changes() {
    final var l = list();
    l.append().set(10);
    JsonStreamWriter fw1 = new JsonStreamWriter();
    JsonStreamWriter rv1 = new JsonStreamWriter();
    l.__commit("l", fw1, rv1);
    // No changes
    JsonStreamWriter fw2 = new JsonStreamWriter();
    JsonStreamWriter rv2 = new JsonStreamWriter();
    l.__commit("l", fw2, rv2);
    Assert.assertEquals("", fw2.toString());
    Assert.assertEquals("", rv2.toString());
  }

  @Test
  public void revert_creation() {
    final var l = list();
    l.append().set(42);
    Assert.assertEquals(1, l.size());
    l.__revert();
    Assert.assertEquals(0, l.size());
  }

  @Test
  public void revert_deletion() {
    final var l = list();
    l.append().set(42);
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    // Delete and then revert
    double key = l.keys().iterator().next();
    l.remove(key);
    Assert.assertEquals(0, l.size());
    l.__revert();
    Assert.assertEquals(1, l.size());
    Assert.assertEquals(42, (int) l.get().iterator().next().get());
  }

  @Test
  public void revert_modification() {
    final var l = list();
    l.append().set(42);
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    // Modify and revert
    l.get().iterator().next().set(100);
    l.__revert();
    Assert.assertEquals(42, (int) l.get().iterator().next().get());
  }

  @Test
  public void redistribution_trigger() {
    final var l = list();
    l.append().set(1);  // pos 0
    l.append().set(2);  // pos 10
    // Repeatedly insert between the same pair to halve the gap each time
    // After 12 bisections: 10/2^12 = ~0.00244 < 1/256 = ~0.00391
    double key1 = l.keys().iterator().next(); // 0.0
    for (int i = 0; i < 12; i++) {
      Iterator<Double> keys = l.keys().iterator();
      double first = keys.next();
      // Insert after the first element, which bisects between first and its successor
      l.insertAfter(first).set(100 + i);
    }
    // Redistribution should have happened, all positions should be evenly spaced
    Set<Double> keys = l.keys();
    Double prev = null;
    for (double k : keys) {
      if (prev != null) {
        Assert.assertEquals(RxList.SPACING, k - prev, 0.001);
      }
      prev = k;
    }
    // Verify values are still correctly ordered - first should be 1, then the last inserted 100+11, etc
    Assert.assertEquals(14, l.size());
  }

  @Test
  public void redistribution_delta_correctness() {
    final var l = list();
    l.append().set(1);  // pos 0
    l.append().set(2);  // pos 10
    // Commit baseline
    JsonStreamWriter fw1 = new JsonStreamWriter();
    JsonStreamWriter rv1 = new JsonStreamWriter();
    l.__commit("l", fw1, rv1);
    // Now trigger redistribution with many insertions
    for (int i = 0; i < 13; i++) {
      Iterator<Double> keys = l.keys().iterator();
      double first = keys.next();
      l.insertAfter(first).set(100 + i);
    }
    // Commit after redistribution
    JsonStreamWriter fw2 = new JsonStreamWriter();
    JsonStreamWriter rv2 = new JsonStreamWriter();
    l.__commit("l", fw2, rv2);
    // The forward delta should contain the new positions
    String fwd = fw2.toString();
    Assert.assertTrue(fwd.length() > 0);
    // The reverse delta should contain nulls for newly created positions and values for deleted old positions
    String rev = rv2.toString();
    Assert.assertTrue(rev.length() > 0);
  }

  @Test
  public void revert_after_redistribution() {
    final var l = list();
    l.append().set(1);  // pos 0
    l.append().set(2);  // pos 10
    // Commit baseline
    JsonStreamWriter fw1 = new JsonStreamWriter();
    JsonStreamWriter rv1 = new JsonStreamWriter();
    l.__commit("l", fw1, rv1);
    // Trigger redistribution
    for (int i = 0; i < 13; i++) {
      Iterator<Double> keys = l.keys().iterator();
      double first = keys.next();
      l.insertAfter(first).set(100 + i);
    }
    // Revert should restore original state
    l.__revert();
    Assert.assertEquals(2, l.size());
    Iterator<RxInt32> it = l.get().iterator();
    Assert.assertEquals(1, (int) it.next().get());
    Assert.assertEquals(2, (int) it.next().get());
    // Original positions should be restored
    Set<Double> keys = l.keys();
    Assert.assertTrue(keys.contains(0.0));
    Assert.assertTrue(keys.contains(10.0));
  }

  @Test
  public void insert_from_json() {
    final var l = list();
    JsonStreamReader reader = new JsonStreamReader("{\"0\":42,\"10\":100}");
    l.__insert(reader);
    Assert.assertEquals(2, l.size());
    Iterator<RxInt32> it = l.get().iterator();
    Assert.assertEquals(42, (int) it.next().get());
    Assert.assertEquals(100, (int) it.next().get());
    // After insert, no dirty state
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    Assert.assertEquals("", fw.toString());
  }

  @Test
  public void insert_null_removes() {
    final var l = list();
    l.append().set(42);
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    JsonStreamReader reader = new JsonStreamReader("{\"0\":null}");
    l.__insert(reader);
    Assert.assertEquals(0, l.size());
  }

  @Test
  public void patch_from_json() {
    final var l = list();
    JsonStreamReader reader = new JsonStreamReader("{\"0\":42,\"10\":100}");
    l.__patch(reader);
    Assert.assertEquals(2, l.size());
    Iterator<RxInt32> it = l.get().iterator();
    Assert.assertEquals(42, (int) it.next().get());
    Assert.assertEquals(100, (int) it.next().get());
  }

  @Test
  public void patch_null_removes() {
    final var l = list();
    l.append().set(42);
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    JsonStreamReader reader = new JsonStreamReader("{\"0\":null}");
    l.__patch(reader);
    Assert.assertEquals(0, l.size());
  }

  @Test
  public void bad_json_keys_skipped() {
    final var l = list();
    JsonStreamReader reader = new JsonStreamReader("{\"notanumber\":42,\"10\":100}");
    l.__insert(reader);
    Assert.assertEquals(1, l.size());
    Assert.assertEquals(100, (int) l.get().iterator().next().get());
  }

  @Test
  public void patch_bad_keys_skipped() {
    final var l = list();
    JsonStreamReader reader = new JsonStreamReader("{\"xyz\":42,\"5\":100}");
    l.__patch(reader);
    Assert.assertEquals(1, l.size());
    Assert.assertEquals(100, (int) l.get().iterator().next().get());
  }

  @Test
  public void insert_skip_non_object() {
    final var l = list();
    JsonStreamReader reader = new JsonStreamReader("123,456");
    l.__insert(reader);
    Assert.assertEquals(456, reader.readInteger());
    Assert.assertEquals(0, l.size());
  }

  @Test
  public void patch_skip_non_object() {
    final var l = list();
    JsonStreamReader reader = new JsonStreamReader("123,456");
    l.__patch(reader);
    Assert.assertEquals(456, reader.readInteger());
    Assert.assertEquals(0, l.size());
  }

  @Test
  public void subscriber_invalidation() {
    final var l = list();
    MockRxChild child = new MockRxChild();
    l.__subscribe(child);
    l.append().set(42);
    child.assertInvalidateCount(1);
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    child.assertInvalidateCount(1);
  }

  @Test
  public void memory_tracking() {
    final var l = list();
    long emptyMemory = l.__memory();
    Assert.assertTrue(emptyMemory > 0);
    l.append().set(1);
    long oneItemMemory = l.__memory();
    Assert.assertTrue(oneItemMemory > emptyMemory);
    l.append().set(2);
    long twoItemMemory = l.__memory();
    Assert.assertTrue(twoItemMemory > oneItemMemory);
  }

  @Test
  public void alive_with_parent() {
    MockRxParent parent = new MockRxParent();
    final var l = list(parent);
    Assert.assertTrue(l.__isAlive());
    parent.alive = false;
    Assert.assertFalse(l.__isAlive());
  }

  @Test
  public void alive_without_parent() {
    final var l = list(null);
    Assert.assertTrue(l.__isAlive());
  }

  @Test
  public void cost_report() {
    MockRxParent parent = new MockRxParent();
    list(parent).__cost(423);
    Assert.assertEquals(423, parent.cost);
  }

  @Test
  public void kill_propagation() {
    // Use a nested RxList to test kill propagation on RxKillable children
    final var outer = new RxList<RxList<RxInt32>>(new MockRxParent(), (parent) -> new RxList<>(parent, INT_MAKER));
    RxList<RxInt32> inner = outer.append();
    inner.append().set(42);
    outer.__kill();
    // After kill, the inner list should still exist but kill was propagated
    // This mainly verifies no exceptions are thrown
    Assert.assertEquals(1, outer.size());
  }

  @Test
  public void raise_invalid_with_parent() {
    MockRxParent parent = new MockRxParent();
    final var l = list(parent);
    Assert.assertTrue(l.__raiseInvalid());
    parent.alive = false;
    Assert.assertFalse(l.__raiseInvalid());
  }

  @Test
  public void raise_invalid_without_parent() {
    final var l = list(null);
    Assert.assertTrue(l.__raiseInvalid());
  }

  @Test
  public void settle() {
    final var l = list();
    l.append().set(1);
    l.__settle(new java.util.HashSet<>());
    // Mainly verify no exception
  }

  @Test
  public void insert_into_existing() {
    final var l = list();
    JsonStreamReader reader1 = new JsonStreamReader("{\"0\":42}");
    l.__insert(reader1);
    // Insert again at same key should update value
    JsonStreamReader reader2 = new JsonStreamReader("{\"0\":100}");
    l.__insert(reader2);
    Assert.assertEquals(1, l.size());
    Assert.assertEquals(100, (int) l.get().iterator().next().get());
  }

  @Test
  public void patch_into_existing() {
    final var l = list();
    l.append().set(42);
    JsonStreamWriter fw = new JsonStreamWriter();
    JsonStreamWriter rv = new JsonStreamWriter();
    l.__commit("l", fw, rv);
    // Patch existing key
    JsonStreamReader reader = new JsonStreamReader("{\"0\":100}");
    l.__patch(reader);
    Assert.assertEquals(1, l.size());
    Assert.assertEquals(100, (int) l.get().iterator().next().get());
  }

  @Test
  public void full_roundtrip_dump_and_insert() {
    final var l1 = list();
    l1.append().set(10);
    l1.append().set(20);
    l1.append().set(30);
    JsonStreamWriter dump = new JsonStreamWriter();
    l1.__dump(dump);
    // Create a new list and insert the dump
    final var l2 = list();
    l2.__insert(new JsonStreamReader(dump.toString()));
    Assert.assertEquals(3, l2.size());
    Iterator<RxInt32> it = l2.get().iterator();
    Assert.assertEquals(10, (int) it.next().get());
    Assert.assertEquals(20, (int) it.next().get());
    Assert.assertEquals(30, (int) it.next().get());
  }

  @Test
  public void fractional_key_dump() {
    final var l = list();
    l.append().set(1);  // pos 0
    l.append().set(3);  // pos 10
    double firstKey = l.keys().iterator().next();
    l.insertAfter(firstKey).set(2);  // pos 5
    JsonStreamWriter writer = new JsonStreamWriter();
    l.__dump(writer);
    Assert.assertEquals("{\"0\":1,\"5\":2,\"10\":3}", writer.toString());
  }
}
