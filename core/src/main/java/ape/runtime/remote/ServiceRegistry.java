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

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.keys.PrivateKeyBundle;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.remote.client.GenericClient;
import ape.runtime.remote.client.GenericClientBase;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/** a service registry maps service names to services */
public class ServiceRegistry {
  public static TreeMap<String, ServiceConstructor> REGISTRY = new TreeMap<>();
  public static GenericClientBase GENERIC_CLIENT_BASE = new GenericClientBase() {
    @Override
    public void execute(String httpMethod, TreeMap<String, String> headers, String url, NtToDynamic body, Callback<NtToDynamic> callback) {
      callback.failure(new ErrorCodeException(ErrorCodes.GENERIC_CLIENT_NOT_AVAILABLE));
    }
  };

  public static ServiceRegistry NOT_READY = new ServiceRegistry("n/a") {
    @Override
    public Service find(String name) {
      return Service.NOT_READY;
    }

    @Override
    public void resolve(HashMap<String, HashMap<String, Object>> servicesConfig, TreeMap<Integer, PrivateKeyBundle> keys) {
    }
  };
  private static final TreeMap<String, Class<?>> INCLUDED_SERVICES = new TreeMap<>();
  private final String spaceName;
  private final TreeMap<String, Service> services;
  private final TreeMap<String, GenericClient> clients;

  public ServiceRegistry(String spaceName) {
    this.spaceName = spaceName;
    this.services = new TreeMap<>();
    this.clients = new TreeMap<>();
  }

  public static void add(String name, Class<?> clazz, ServiceConstructor cons) {
    INCLUDED_SERVICES.put(name, clazz);
    REGISTRY.put(name, cons);
  }

  public void add(String name, GenericClient client) {
    this.clients.put(name, client);
  }

  public static String getLinkDefinition(String name, int autoId, String params, HashSet<String> names, Consumer<String> error) {
    Class<?> clazz = INCLUDED_SERVICES.get(name);
    if (clazz == null) {
      return null;
    }
    try {
      Method method = clazz.getMethod("definition", int.class, String.class, HashSet.class, Consumer.class);
      return (String) method.invoke(null, autoId, params, names, error);
    } catch (Exception ex) {
      return null;
    }
  }

  /** find a service */
  public Service find(String name) {
    Service local = services.get(name);
    if (local == null) {
      return Service.FAILURE;
    }
    return local;
  }

  public boolean contains(String name) {
    return services.containsKey(name);
  }

  public void resolve(HashMap<String, HashMap<String, Object>> servicesConfig, TreeMap<Integer, PrivateKeyBundle> keys) {
    for (Map.Entry<String, HashMap<String, Object>> entry : servicesConfig.entrySet()) {
      Service resolved = resolveService(entry.getValue(), keys);
      if (resolved == null) {
        resolved = Service.FAILURE;
      }
      services.put(entry.getKey(), resolved);
    }
  }

  private Service resolveService(HashMap<String, Object> config, TreeMap<Integer, PrivateKeyBundle> keys) {
    Object clazz = config.get("class");
    try {
      if (clazz != null && clazz instanceof String) {
        ServiceConstructor cons = REGISTRY.get((String) clazz);
        if (cons != null) {
          return cons.cons(spaceName, config, keys);
        }
      }
    } catch (Exception ex) { // ignore it
    }
    return null;
  }

  public GenericClient makeGenericClient() {
    return new GenericClient(GENERIC_CLIENT_BASE);
  }

  public GenericClient getClient(String name) {
    return clients.get(name);
  }
}
