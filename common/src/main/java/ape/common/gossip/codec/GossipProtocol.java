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
package ape.common.gossip.codec;

import ape.common.codec.FieldOrder;
import ape.common.codec.Flow;
import ape.common.codec.TypeId;

/**
 * client picks a random known host
 * (1) sends a BeginGossip with its most recent hash of its endpoint set along with a handful of recent endpoints learned about
 * (2) server seeing BeginGossip will
 * (a) integrate the recent endpoints
 * (b) look the provided hash within the HashSetChain
 * (i) if the Set was found within the server's HashSetChain
 * (x) send client a HashFound with the counters and a recent set of endpoints  (completed exchange)
 * (y) the client will then integrate the counters for the Set used
 * (z) the client will integrate the recent endpoints if there are any
 * (w) the client will then send a QuickGossip to complete the exchange
 * (ii) if the Set was not found
 * (x) send client a HashNotFoundReverseConversation with the most recent hash serer knows about along with recent endpoints
 * (y) the client will integrate the recent endpoints if there are any
 * (z) the client will search its HashSetChain for the hash
 * (u) if the Set was found within the client's HashSetChain, then send a ReverseHashFound (completed exchange)
 * (v) if the Set was not found, then send a SlowGossip and stop (breaking asymmetry)
 */
public class GossipProtocol {
  @Flow("Raw")
  @TypeId(30)
  public static class Endpoint {
    @FieldOrder(1)
    public String id;
    @FieldOrder(2)
    public String ip;
    @FieldOrder(3)
    public int port;
    @FieldOrder(4)
    public int monitoringPort;
    @FieldOrder(5)
    public int counter;
    @FieldOrder(6)
    public String role;
    @FieldOrder(7)
    public long created;
  }

  // client initiates gossip by sending its hash along with an optimistic list of endpoints
  @Flow("ChatterFromClient")
  @TypeId(31)
  public static class BeginGossip {
    @FieldOrder(1)
    public String hash;
    @FieldOrder(2)
    public Endpoint[] recent_endpoints;
    @FieldOrder(3)
    public String[] recent_deletes;
  }

  // server found the hash and knows how to respond to a quick gossip
  @Flow("ChatterFromServer")
  @TypeId(33)
  public static class HashFoundRequestForwardQuickGossip {
    @FieldOrder(1)
    public int[] counters;
    @FieldOrder(2)
    public Endpoint[] recent_endpoints;
    @FieldOrder(3)
    public String[] recent_deletes;
  }

  // server found the hash or client found the reverse hash
  @Flow("ChatterFromClient")
  @TypeId(34)
  public static class ForwardQuickGossip {
    @FieldOrder(1)
    public int[] counters;
    @FieldOrder(2)
    public Endpoint[] recent_endpoints;
    @FieldOrder(3)
    public String[] recent_deletes;
  }

  // server couldn't find hash, so it sends its recent endpoints along with its hash
  @Flow("ChatterFromServer")
  @TypeId(35)
  public static class HashNotFoundReverseConversation {
    @FieldOrder(1)
    public String hash;
    @FieldOrder(2)
    public Endpoint[] recent_endpoints;
    @FieldOrder(3)
    public String[] recent_deletes;
  }

  // client is learning that a hash wasn't found, but it found the related hash
  @Flow("ChatterFromClient")
  @TypeId(36)
  public static class ReverseHashFound {
    @FieldOrder(1)
    public int[] counters;
    @FieldOrder(2)
    public Endpoint[] missing_endpoints;
    @FieldOrder(3)
    public String[] recent_deletes;
  }

  // client is learning that a hash wasn't found, but it found the related hash
  @Flow("ChatterFromServer")
  @TypeId(37)
  public static class ReverseQuickGossip {
    @FieldOrder(1)
    public int[] counters;
    @FieldOrder(2)
    public Endpoint[] missing_endpoints;
    @FieldOrder(3)
    public String[] recent_deletes;
  }

  // client didn't find the reverse hash, send everything
  @Flow("ChatterFromClient")
  @TypeId(38)
  public static class ForwardSlowGossip {
    @FieldOrder(1)
    public Endpoint[] all_endpoints;
    @FieldOrder(2)
    public String[] recent_deletes;
  }

  // server responses to a slow gossip with a slow gossip
  @Flow("ChatterFromServer")
  @TypeId(39)
  public static class ReverseSlowGossip {
    @FieldOrder(1)
    public Endpoint[] all_endpoints;
    @FieldOrder(2)
    public String[] recent_deletes;
  }
}
