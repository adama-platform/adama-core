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
package ape.runtime.reactives.maps;

import ape.runtime.mocks.MockRxParent;
import org.junit.Assert;
import org.junit.Test;

public class MapPubSubTests {
  @Test
  public void flow() {
    MockRxParent par = new MockRxParent();
    MapPubSub<String> pubsub = new MapPubSub<>(par);
    MockMapSubscription one = new MockMapSubscription();
    MockMapSubscription two = new MockMapSubscription();
    pubsub.subscribe(one);
    pubsub.subscribe(two);
    pubsub.changed("X");
    pubsub.changed("X");
    pubsub.changed("X");
    pubsub.changed("X");
    pubsub.changed("X");
    pubsub.gc();
    pubsub.changed("Y");
    two.alive = false;
    Assert.assertEquals(2, pubsub.count());
    pubsub.gc();
    Assert.assertEquals(1, pubsub.count());
    pubsub.changed("Z");
    one.alive = false;
    pubsub.gc();
    Assert.assertEquals(0, pubsub.count());
    Assert.assertEquals(3, one.publishes.size());
    Assert.assertEquals(2, two.publishes.size());
    Assert.assertEquals("CHANGE:X", one.publishes.get(0));
    Assert.assertEquals("CHANGE:Y", one.publishes.get(1));
    Assert.assertEquals("CHANGE:Z", one.publishes.get(2));
    Assert.assertEquals("CHANGE:X", two.publishes.get(0));
    Assert.assertEquals("CHANGE:Y", two.publishes.get(1));
    Assert.assertTrue(pubsub.alive());
    par.alive = false;
    Assert.assertFalse(pubsub.alive());
    pubsub.__memory();
  }

  @Test
  public void repeats_settle() {
    MockRxParent par = new MockRxParent();
    MapPubSub<String> pubsub = new MapPubSub<>(par);
    MockMapSubscription sub = new MockMapSubscription();
    pubsub.subscribe(sub);
    pubsub.changed("X");
    pubsub.changed("X");
    pubsub.changed("X");
    pubsub.settle();
    pubsub.changed("Y");
    pubsub.changed("Y");
    pubsub.changed("Y");
    Assert.assertEquals(2, sub.publishes.size());
    Assert.assertEquals("CHANGE:X", sub.publishes.get(0));
    Assert.assertEquals("CHANGE:Y", sub.publishes.get(1));
    pubsub.__memory();
  }

  @Test
  public void trivial_alive() {
    Assert.assertTrue(new MapPubSub<String>(null).alive());
  }
}
