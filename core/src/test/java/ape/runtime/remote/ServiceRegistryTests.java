/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    ServiceRegistry registry = new ServiceRegistry();
    Assert.assertFalse(registry.contains("xyz"));
    HashMap<String, HashMap<String, Object>> config = new HashMap<>();
    config.put("xyz", new HashMap<>());
    registry.resolve("space", config, new TreeMap<>());
    Assert.assertTrue(registry.contains("xyz"));
    Assert.assertTrue(registry.find("nooop") == Service.FAILURE);
    Assert.assertFalse(ServiceRegistry.NOT_READY.contains("x"));
    ServiceRegistry.NOT_READY.resolve("x", null, null);
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
