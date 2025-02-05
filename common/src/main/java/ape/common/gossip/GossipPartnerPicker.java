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

import java.util.*;

/** pick a partner to gossip with */
public class GossipPartnerPicker {
  private final String self;
  private final InstanceSetChain chain;
  private final HashSet<String> initial;
  private final ArrayList<String> peers;
  private final Random rng;
  private final HashMap<String, Integer> counts;
  private String cachedPeersHash;

  public GossipPartnerPicker(String self, InstanceSetChain chain, HashSet<String> initial, Random rng) {
    this.self = self;
    this.chain = chain;
    this.initial = initial;
    this.rng = rng;
    this.peers = new ArrayList<>();
    this.counts = new HashMap<>();
    this.cachedPeersHash = "";
  }

  public String pick() {
    if (!cachedPeersHash.equals(chain.current().hash())) {
      peers.clear();
      TreeSet<String> set = new TreeSet<>(initial);
      set.addAll(chain.current().targetsFor("gossip"));
      peers.addAll(set);
      cachedPeersHash = chain.current().hash();
    }
    if (peers.size() > 0) {
      String a = randomPeerNotSelf();
      String b = randomPeerNotSelf();
      if (a == null || b == null) {
        return null;
      }
      int x = countOf(a);
      int y = countOf(b);
      if (x < y) {
        counts.put(a, x + 1);
        return a;
      } else {
        counts.put(b, y + 1);
        return b;
      }
    } else {
      return null;
    }
  }

  private String randomPeerNotSelf() {
    int attempts = 0;
    while (attempts < 4) {
      String x = peers.get(rng.nextInt(peers.size()));
      if (!self.equals(x)) {
        return x;
      }
      attempts++;
    }
    return null;
  }

  private int countOf(String target) {
    Integer count = counts.get(target);
    if (count == null) {
      return 0;
    }
    return count;
  }
}
