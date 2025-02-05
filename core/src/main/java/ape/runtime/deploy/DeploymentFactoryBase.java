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
package ape.runtime.deploy;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.keys.PrivateKeyBundle;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.data.Key;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.remote.RemoteResult;
import ape.runtime.sys.PerfTracker;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.jvm.LivingDocumentFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/** this is the base for all spaces to resolve against */
public class DeploymentFactoryBase implements LivingDocumentFactoryFactory, Deliverer, Undeploy {
  private final AsyncByteCodeCache cache;
  private final ConcurrentHashMap<String, DeploymentFactory> spaces;
  private final RuntimeEnvironment runtime;
  private Deliverer deliverer;

  public DeploymentFactoryBase(AsyncByteCodeCache cache, RuntimeEnvironment runtime) {
    this.cache = cache;
    this.spaces = new ConcurrentHashMap<>();
    this.deliverer = Deliverer.FAILURE;
    this.runtime = runtime;
  }

  public void attachDeliverer(Deliverer deliverer) {
    this.deliverer = deliverer;
  }

  public String hashOf(String space) {
    DeploymentFactory factory = this.spaces.get(space);
    if (factory != null) {
      return factory.plan.hash;
    }
    return null;
  }

  @Override
  public void deliver(NtPrincipal agent, Key key, int id, RemoteResult result, boolean firstParty, Callback<Integer> callback) {
    deliverer.deliver(agent, key, id, result, firstParty, callback);
  }

  public void deploy(String space, DeploymentPlan plan, TreeMap<Integer, PrivateKeyBundle> keys, Callback<Void> callback){
    long started = System.currentTimeMillis();
    AsyncCompiler.forge(runtime, space, spaces.get(space), plan, this, keys, cache, new Callback<DeploymentFactory>() {
      @Override
      public void success(DeploymentFactory factory) {
        spaces.put(space, factory);
        PerfTracker.writeDeploymentTime(space, System.currentTimeMillis() - started, true);
        callback.success(null);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        PerfTracker.writeDeploymentTime(space, System.currentTimeMillis() - started, false);
        callback.failure(ex);
      }
    });
  }

  public boolean contains(String space) {
    return spaces.containsKey(space);
  }

  @Override
  public void undeploy(String space) {
    spaces.remove(space);
  }

  @Override
  public void fetch(Key key, Callback<LivingDocumentFactory> callback) {
    DeploymentFactory factory = spaces.get(key.space);
    if (factory == null) {
      callback.failure(new ErrorCodeException(ErrorCodes.DEPLOYMENT_FACTORY_CANT_FIND_SPACE));
      return;
    }
    factory.fetch(key, callback);
  }

  @Override
  public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {
    for (DeploymentFactory factory : spaces.values()) {
      factory.account(sample);
    }
  }

  @Override
  public Collection<String> spacesAvailable() {
    return spaces.keySet();
  }
}
