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

import ape.common.gossip.codec.GossipProtocol;

import java.util.Objects;

/**
 * Represents a single application instance in the gossip cluster.
 * Tracks identity (id, ip, port, role), health (counter, witness timestamp),
 * and lifecycle (created, local flag). Counter increments with each heartbeat;
 * stale instances are candidates for deletion based on witness age.
 */
public class Instance implements Comparable<Instance> {
  public final String id;
  public final int monitoringPort;
  public final String ip;
  public final int port;
  public final String role;
  public final long created;
  private int counter;
  private long witness;
  public final boolean local;

  public Instance(GossipProtocol.Endpoint endpoint, long now, boolean local) {
    this.id = endpoint.id;
    this.ip = endpoint.ip;
    this.port = endpoint.port;
    this.monitoringPort = endpoint.monitoringPort;
    this.role = endpoint.role;
    this.counter = endpoint.counter;
    this.witness = now;
    this.created = endpoint.created;
    this.local = local;
  }

  public static int humanizeCompare(Instance x, Instance y) {
    int delta = x.ip.compareTo(y.ip);
    if (delta == 0) {
      return x.role.compareTo(y.role);
    }
    return delta;
  }

  public String role() {
    return role;
  }

  public String target() {
    return ip + ":" + port;
  }

  public int counter() {
    return this.counter;
  }

  public long witnessed() {
    return this.witness;
  }

  public GossipProtocol.Endpoint toEndpoint() {
    GossipProtocol.Endpoint endpoint = new GossipProtocol.Endpoint();
    endpoint.id = id;
    endpoint.ip = ip;
    endpoint.port = port;
    endpoint.monitoringPort = monitoringPort;
    endpoint.role = role;
    endpoint.created = created;
    endpoint.counter = counter;
    return endpoint;
  }

  /**
   * the application should call this every second locally.
   *
   * <p>This means each app's counter will wrap around every 2 billion seconds.
   *
   * <p>At 1 bump per second, we will bump 86400 times per day. This means we will have 2147483648 /
   * 86400 = 24855.13 days until the counter wraps around. This software then will fail after
   * 24855.13 / 366 = 67.91 years.
   *
   * <p>A built-in assumption is that an instance, upon process restart, will reset the counter back
   * to 0. This means we just need a cron job to wrong every 50 years to restart the process to not
   * worry about negative numbers.
   */
  public void bump(long now) {
    counter++;
    witness = now;
  }

  public void absorb(int incCounter, long now) {
    if (incCounter > counter) {
      this.counter = incCounter;
      this.witness = now;
    }
  }

  public boolean canDelete(long now) {
    return (now - witness) > Constants.MILLISECONDS_FOR_DELETION_CANDIDATE;
  }

  public boolean tooOldMustDelete(long now) {
    return (now - witness) > Constants.MILLISECONDS_FOR_RECOMMEND_DELETION_CANDIDATE;
  }

  @Override
  public int compareTo(Instance o) {
    return id.compareTo(o.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, ip, port, role);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Instance) {
      Instance instance = (Instance) o;
      return id.equals(instance.id);
    }
    return false;
  }
}
