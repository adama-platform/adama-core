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

import ape.common.metrics.Inflight;
import ape.common.metrics.MetricsFactory;

public class GossipMetrics {
  public final Inflight gossip_active_clients;
  public final Runnable gossip_wake;
  public final Runnable gossip_send_begin;
  public final Runnable gossip_read_reverse_slow_gossip;
  public final Runnable gossip_read_reverse_quick_gossip;
  public final Runnable gossip_read_hash_not_found;
  public final Runnable gossip_send_reverse_hash_found;
  public final Runnable gossip_send_forward_slow_gossip;
  public final Runnable gossip_read_hash_found_forward_quick_gossip;
  public final Runnable gossip_read_forward_slow_gossip;
  public final Runnable gossip_read_reverse_hash_found;
  public final Runnable gossip_read_forward_quick_gossip;
  public final Runnable gossip_read_begin_gossip;
  public final Runnable gossip_send_hash_found;
  public final Runnable gossip_send_hash_not_found;
  public final Inflight gossip_inflight;

  public GossipMetrics(MetricsFactory factory) {
    gossip_active_clients = factory.inflight("gossip_active_clients");
    gossip_wake = factory.counter("gossip_wake");
    gossip_send_begin = factory.counter("gossip_send_begin");
    gossip_read_reverse_slow_gossip = factory.counter("gossip_read_reverse_slow_gossip");
    gossip_read_reverse_quick_gossip = factory.counter("gossip_read_reverse_quick_gossip");
    gossip_read_hash_not_found = factory.counter("gossip_read_hash_not_found");
    gossip_send_reverse_hash_found = factory.counter("gossip_send_reverse_hash_found");
    gossip_send_forward_slow_gossip = factory.counter("gossip_send_forward_slow_gossip");
    gossip_read_hash_found_forward_quick_gossip = factory.counter("gossip_read_hash_found_forward_quick_gossip");
    gossip_read_forward_slow_gossip = factory.counter("gossip_read_forward_slow_gossip");
    gossip_read_reverse_hash_found = factory.counter("gossip_read_reverse_hash_found");
    gossip_read_forward_quick_gossip = factory.counter("gossip_read_forward_quick_gossip");
    gossip_read_begin_gossip = factory.counter("gossip_read_begin_gossip");
    gossip_send_hash_found = factory.counter("gossip_send_hash_found");
    gossip_send_hash_not_found = factory.counter("gossip_send_hash_not_found");
    gossip_inflight = factory.inflight("gossip_inflight");
  }
}
