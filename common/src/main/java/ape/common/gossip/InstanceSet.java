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

import ape.common.Hashing;
import ape.common.gossip.codec.GossipProtocol;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/** a collection of instances */
public class InstanceSet {
  public final ArrayList<Instance> instances;
  public final TreeSet<String> ids;
  public final String hash;

  public InstanceSet(TreeSet<Instance> instances, long now) {
    this.instances = new ArrayList<>(instances);
    this.ids = new TreeSet<>();
    MessageDigest digest = Hashing.md5();
    for (Instance instance : instances) {
      ids.add(instance.id);
      digest.update(instance.id.getBytes(StandardCharsets.UTF_8));
    }
    this.hash = Hashing.finishAndEncode(digest);
  }

  public TreeSet<Instance> clone() {
    return new TreeSet<>(instances);
  }

  public int[] counters() {
    int[] counters = new int[instances.size()];
    int at = 0;
    for (Instance instance : instances) {
      counters[at] = instance.counter();
      at++;
    }
    return counters;
  }

  public GossipProtocol.Endpoint[] toEndpoints() {
    ArrayList<GossipProtocol.Endpoint> endpoints = new ArrayList<>();
    for (Instance instance : instances) {
      endpoints.add(instance.toEndpoint());
    }
    return endpoints.toArray(new GossipProtocol.Endpoint[endpoints.size()]);
  }

  public GossipProtocol.Endpoint[] missing(InstanceSet prior) {
    ArrayList<GossipProtocol.Endpoint> eps = new ArrayList<>();
    for (Instance local : instances) {
      if (!prior.ids.contains(local.id)) {
        eps.add(local.toEndpoint());
      }
    }
    return eps.toArray(new GossipProtocol.Endpoint[eps.size()]);
  }

  public void ingest(int[] counters, long now) {
    if (instances.size() == counters.length) {
      Iterator<Instance> instanceIt = instances.iterator();
      int at = 0;
      while (instanceIt.hasNext()) {
        instanceIt.next().absorb(counters[at], now);
        at++;
      }
    }
  }

  public String hash() {
    return hash;
  }

  public ArrayList<String> targetsFor(String role) {
    TreeSet<String> targets = new TreeSet<>();
    for (Instance instance : instances) {
      if (role.equals(instance.role())) {
        targets.add(instance.target());
      }
    }
    return new ArrayList<>(targets);
  }
}
