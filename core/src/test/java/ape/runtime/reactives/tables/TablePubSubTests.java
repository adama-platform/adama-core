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
package ape.runtime.reactives.tables;

import ape.runtime.mocks.MockRxParent;
import org.junit.Assert;
import org.junit.Test;

public class TablePubSubTests {
  @Test
  public void flow() {
    MockRxParent par = new MockRxParent();
    TablePubSub pubsub = new TablePubSub(par);
    MockTableSubscription one = new MockTableSubscription();
    MockTableSubscription two = new MockTableSubscription();
    pubsub.subscribe(one);
    pubsub.subscribe(two);
    Assert.assertEquals(2, pubsub.count());
    pubsub.gc();
    pubsub.primary(123);
    pubsub.index(13, 69);
    for (int k = 0; k < 100; k++) { // dedupe!
      pubsub.primary(123);
      pubsub.index(13, 69);
    }
    Assert.assertEquals(2, pubsub.count());
    one.alive = false;
    pubsub.gc();
    Assert.assertEquals(1, pubsub.count());
    pubsub.primary(125);
    pubsub.index( 14, 69);
    Assert.assertTrue(pubsub.alive());
    par.alive = false;
    Assert.assertFalse(pubsub.alive());
    Assert.assertEquals(2, one.publishes.size());
    Assert.assertEquals(4, two.publishes.size());
    Assert.assertEquals("PKEY:123", one.publishes.get(0));
    Assert.assertEquals("IDX:13=69", one.publishes.get(1));
    Assert.assertEquals("PKEY:123", two.publishes.get(0));
    Assert.assertEquals("IDX:13=69", two.publishes.get(1));
    Assert.assertEquals("PKEY:125", two.publishes.get(2));
    Assert.assertEquals("IDX:14=69", two.publishes.get(3));
    pubsub.__memory();
  }

  @Test
  public void trivial_alive() {
    Assert.assertTrue(new TablePubSub(null).alive());
  }
}
