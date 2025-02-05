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
package ape.common.gossip;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class GossipPartnerPickerTests {
  @Test
  public void flow_empty() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    GossipPartnerPicker picker = new GossipPartnerPicker("127.0.0.1", chain, new HashSet<>(), new Random());
    Assert.assertNull(picker.pick());
  }

  @Test
  public void flow_only_self() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    HashSet<String> set = new HashSet<>();
    set.add("127.0.0.1");
    GossipPartnerPicker picker = new GossipPartnerPicker("127.0.0.1", chain, set, new Random());
    Assert.assertNull(picker.pick());
  }

  @Test
  public void flow_balance() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    HashSet<String> set = new HashSet<>();
    set.add("127.0.0.1");
    set.add("127.0.0.2");
    set.add("127.0.0.3");
    set.add("127.0.0.4");
    set.add("127.0.0.5");
    GossipPartnerPicker picker = new GossipPartnerPicker("127.0.0.1", chain, set, new Random());
    HashMap<String, Integer> counts = new HashMap<>();
    for (int k = 0; k < 100; k++) {
      String host = picker.pick();
      Integer prior = counts.get(host);
      if (prior == null) {
        counts.put(host, 1);
      } else {
        counts.put(host, prior + 1);
      }
    }
    int sum = 0;
    int max = -1;
    for (Integer val : counts.values()) {
      System.out.println(val);
      sum += val;
      max = Math.max(val, max);
    }
    Assert.assertEquals(sum, 100);
    Assert.assertTrue(max <= 40); // should be ~ 25
  }
}
