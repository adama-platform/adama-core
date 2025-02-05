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

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

public class CommonTest {


  public static Instance A() {
    return new Instance(newBuilder().setCounter(100).setId("id-a").setIp("ip1").setRole("proxy").setPort(123).setMonitoringPort(200).build(), 0, true);
  }

  public static EndpointBuilder newBuilder() {
    return new EndpointBuilder();
  }

  public static Instance B() {
    return new Instance(newBuilder().setCounter(200).setId("id-b").setIp("ip2").setRole("proxy").setPort(234).setMonitoringPort(201).build(), 0, false);
  }

  public static Instance C() {
    return new Instance(newBuilder().setCounter(300).setId("id-c").setIp("ip1").setRole("proxy").setPort(345).setMonitoringPort(202).build(), 0, false);
  }

  public static Instance D() {
    return new Instance(newBuilder().setCounter(400).setId("id-d").setIp("ip2").setRole("proxy").setPort(456).setMonitoringPort(203).build(), 0, false);
  }

  public static TreeSet<Instance> INSTANCES(Instance... instances) {
    TreeSet<Instance> set = new TreeSet<>();
    Collections.addAll(set, instances);
    return set;
  }

  public static GossipProtocol.Endpoint[] ENDPOINTS(Instance... instances) {
    ArrayList<GossipProtocol.Endpoint> set = new ArrayList<>();
    for (Instance instance : instances) {
      set.add(instance.toEndpoint());
    }
    return set.toArray(new GossipProtocol.Endpoint[set.size()]);
  }

  public int[] counters(int... values) {
    return values;
  }

  public static class EndpointBuilder {
    public GossipProtocol.Endpoint endpoint;

    public EndpointBuilder() {
      this.endpoint = new GossipProtocol.Endpoint();
    }

    public EndpointBuilder setCounter(int c) {
      this.endpoint.counter = c;
      return this;
    }

    public EndpointBuilder setId(String id) {
      this.endpoint.id = id;
      return this;
    }

    public EndpointBuilder setIp(String ip) {
      this.endpoint.ip = ip;
      return this;
    }

    public EndpointBuilder setRole(String role) {
      this.endpoint.role = role;
      return this;
    }

    public EndpointBuilder setPort(int port) {
      this.endpoint.port = port;
      return this;
    }

    public EndpointBuilder setMonitoringPort(int monitoringPort) {
      this.endpoint.monitoringPort = monitoringPort;
      return this;
    }

    public GossipProtocol.Endpoint build() {
      return endpoint;
    }
  }
}
