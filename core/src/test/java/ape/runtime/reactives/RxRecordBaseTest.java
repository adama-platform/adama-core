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

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockRecord;
import ape.runtime.mocks.MockRxChild;
import ape.runtime.mocks.MockRxParent;
import ape.runtime.natives.NtDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;

public class RxRecordBaseTest {
  @Test
  public void cmp() {
    final var a = new MockRecord(null);
    final var b = new MockRecord(null);
    a.id = 1;
    b.id = 2;
    Assert.assertEquals(-1, a.compareTo(b));
    Assert.assertEquals(1, b.compareTo(a));
    Assert.assertFalse(a.equals(b));
    a.__invalidateSubscribers();
    a.__raiseInvalid();
  }

  @Test
  public void subDt() {
    final var x = new MockRecord(null);
    RxDateTime dt = new RxDateTime(null, new NtDateTime(ZonedDateTime.parse("2021-04-24T17:57:19.802528800-05:00[America/Chicago]")));
    x.__subscribeUpdated(dt, () -> new NtDateTime(ZonedDateTime.parse("2023-04-24T17:57:19.802528800-05:00[America/Chicago]")));
    Assert.assertEquals(dt.get().toString(), "2021-04-24T17:57:19.802528800-05:00[America/Chicago]");
    x.__raiseInvalid();
    Assert.assertEquals(dt.get().toString(), "2023-04-24T17:57:19.802528800-05:00[America/Chicago]");
  }

  @Test
  public void subBump() {
    final var x = new MockRecord(null);
    RxInt32 c = new RxInt32(null, 0);
    x.__subscribeBump(c);
    Assert.assertEquals(0, (int) c.get());
    x.__raiseInvalid();
    x.__raiseInvalid();
    x.__raiseInvalid();
    Assert.assertEquals(1, (int) c.get());
    x.__settle(null);
    x.__raiseInvalid();
    Assert.assertEquals(2, (int) c.get());
  }

  @Test
  public void cost_report() {
    MockRxParent parent = new MockRxParent();
    new MockRecord(parent).__cost(2421);
    Assert.assertEquals(2421, parent.cost);
  }

  @Test
  public void memory() {
    final var a = new MockRecord(null);
    Assert.assertEquals(42, a.__memory());
  }

  @Test
  public void sanity() {
    final var mr = new MockRecord(null);
    final var child = new MockRxChild();
    mr.__subscribe(child);
    mr.id = 123;
    mr.__id();
    mr.__getIndexColumns();
    mr.__getIndexValues();
    mr.__name();
    mr.__reindex();
    mr.__deindex();
    mr.__commit(null, new JsonStreamWriter(), new JsonStreamWriter());
    mr.__revert();
    mr.__raiseDirty();
    Assert.assertTrue(mr.__isDirty());
    mr.__lowerDirtyCommit();
    Assert.assertFalse(mr.__isDirty());
    mr.__raiseDirty();
    mr.__lowerDirtyRevert();
    Assert.assertFalse(mr.__isDirty());
    mr.__delete();
    Assert.assertTrue(mr.__isDirty());
    Assert.assertTrue(mr.__isDying());
    child.assertInvalidateCount(3);
    Assert.assertEquals(0, mr.compareTo(mr));
    Assert.assertEquals(123, mr.hashCode());
    Assert.assertTrue(mr.equals(mr));
    Assert.assertFalse(mr.equals(null));
  }

  @Test
  public void alive_without_parent() {
    final var mr = new MockRecord(null);
    Assert.assertTrue(mr.__isAlive());
    mr.__kill();
    Assert.assertFalse(mr.__isAlive());
  }

  @Test
  public void alive_with_parent() {
    MockRxParent parent = new MockRxParent();
    final var mr = new MockRecord(parent);
    Assert.assertTrue(mr.__isAlive());
    parent.alive = false;
    Assert.assertFalse(mr.__isAlive());
  }
}
