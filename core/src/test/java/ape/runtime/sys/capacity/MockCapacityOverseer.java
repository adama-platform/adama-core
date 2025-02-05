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
package ape.runtime.sys.capacity;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MockCapacityOverseer implements CapacityOverseer {
  private final ArrayList<CapacityInstance> capacity;
  private final ArrayList<String> hosts;

  public MockCapacityOverseer() {
    this.capacity = new ArrayList<>();
    this.hosts = new ArrayList<>();
  }

  @Override
  public void listAllSpace(String space, Callback<List<CapacityInstance>> callback) {
    ArrayList<CapacityInstance> result = new ArrayList<>();
    Iterator<CapacityInstance> it = capacity.iterator();
    while (it.hasNext()) {
      CapacityInstance instance = it.next();
      if (instance.space.equals(space)) {
        result.add(instance);
      }
    }
    callback.success(result);
  }

  @Override
  public void listWithinRegion(String space, String region, Callback<List<CapacityInstance>> callback) {
    ArrayList<CapacityInstance> result = new ArrayList<>();
    Iterator<CapacityInstance> it = capacity.iterator();
    while (it.hasNext()) {
      CapacityInstance instance = it.next();
      if (instance.space.equals(space) && instance.region.equals(region)) {
        result.add(instance);
      }
    }
    callback.success(result);
  }

  @Override
  public void listAllOnMachine(String region, String machine, Callback<List<CapacityInstance>> callback) {
    ArrayList<CapacityInstance> result = new ArrayList<>();
    Iterator<CapacityInstance> it = capacity.iterator();
    while (it.hasNext()) {
      CapacityInstance instance = it.next();
      if (instance.machine.equals(machine) && instance.region.equals(region)) {
        result.add(instance);
      }
    }
    callback.success(result);
  }

  @Override
  public void add(String space, String region, String machine, Callback<Void> callback) {
    Iterator<CapacityInstance> it = capacity.iterator();
    while (it.hasNext()) {
      CapacityInstance instance = it.next();
      if (instance.machine.equals(machine) && instance.region.equals(region) && instance.space.equals(space)) {
        callback.failure(new ErrorCodeException(10000));
        return;
      }
    }
    capacity.add(new CapacityInstance(space, region, machine, false));
  }

  @Override
  public void remove(String space, String region, String machine, Callback<Void> callback) {
    Iterator<CapacityInstance> it = capacity.iterator();
    while (it.hasNext()) {
      CapacityInstance instance = it.next();
      if (instance.machine.equals(machine) && instance.region.equals(region) && instance.space.equals(space)) {
        it.remove();
      }
    }
    callback.success(null);
  }

  @Override
  public void nuke(String space, Callback<Void> callback) {
    Iterator<CapacityInstance> it = capacity.iterator();
    while (it.hasNext()) {
      CapacityInstance instance = it.next();
      if (instance.space.equals(space)) {
        it.remove();
      }
    }
    callback.success(null);
  }

  public void addHost(String host) {
    hosts.add(host);
  }

  private static String pick(List<String> hosts, String space, String region) {
    if (hosts.size() == 0) {
      return null;
    }
    String winner = null;
    String winningHash = null;
    for (String host : hosts) {
      MessageDigest digest = Hashing.md5();
      digest.update((space + ":" + region + ":" + host).getBytes(StandardCharsets.UTF_8));
      String currentHash = Hashing.finishAndEncode(digest);
      if (winner == null || currentHash.compareTo(winningHash) > 0) {
        winner = host;
        winningHash = currentHash;
      }
    }
    return winner;
  }

  @Override
  public void pickStableHostForSpace(String space, String region, Callback<String> callback) {
    String result = pick(hosts, space, region);
    if (result == null) {
      callback.failure(new ErrorCodeException(1234));
    } else {
      callback.success(result);
    }
  }

  private boolean _found(String space, String region, String machine) {
    Iterator<CapacityInstance> it = capacity.iterator();
    while (it.hasNext()) {
      CapacityInstance instance = it.next();
      if (instance.machine.equals(machine) && instance.region.equals(region) && instance.space.equals(space)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void pickNewHostForSpace(String space, String region, Callback<String> callback) {
    ArrayList<String> avail = new ArrayList<>();
    for (String host : hosts) {
      if (!_found(space, region, host)) {
        avail.add(host);
      }
    }
    String result = pick(avail, space, region);
    if (result == null) {
      callback.failure(new ErrorCodeException(23));
    } else {
      callback.success(result);
    }
  }
}
