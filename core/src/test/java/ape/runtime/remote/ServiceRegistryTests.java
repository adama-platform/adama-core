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
package ape.runtime.remote;

import ape.common.Callback;
import ape.runtime.contracts.Caller;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtResult;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.remote.replication.Replicator;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.function.Function;

public class ServiceRegistryTests {
  @Test
  public void flow() {
    ServiceRegistry registry = new ServiceRegistry("space");
    Assert.assertFalse(registry.contains("xyz"));
    HashMap<String, HashMap<String, Object>> config = new HashMap<>();
    config.put("xyz", new HashMap<>());
    registry.resolve(config, new TreeMap<>());
    Assert.assertTrue(registry.contains("xyz"));
    Assert.assertTrue(registry.find("nooop") == Service.FAILURE);
    Assert.assertFalse(ServiceRegistry.NOT_READY.contains("x"));
    ServiceRegistry.NOT_READY.resolve(null, null);
  }

  @Test
  public void static_reg() {
    ServiceRegistry.add("xyx", ServiceRegistryTests.class, null);
    Assert.assertNull(ServiceRegistry.getLinkDefinition("xyz", 12, null, null, null));
  }

  class DumbXYZ implements Service {
    @Override
    public <T> NtResult<T> invoke(Caller caller, String method, RxCache cache, NtPrincipal agent, NtToDynamic request, Function<String, T> result) {
      return null;
    }

    @Override
    public Replicator beginCreateReplica(String method, NtToDynamic body) {
      return null;
    }

    @Override
    public void deleteReplica(String method, String key, Callback<Void> callback) {

    }
  }

  @Test
  public void nulldef() {
    ServiceRegistry.add("xyz", DumbXYZ.class, (x, y, z) -> new DumbXYZ());
    Assert.assertNull(ServiceRegistry.getLinkDefinition("xyz", 1, "{}", new HashSet<>(), (err) -> {}));
  }
}
