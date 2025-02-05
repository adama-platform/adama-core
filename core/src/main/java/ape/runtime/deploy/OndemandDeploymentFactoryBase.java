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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.contracts.PlanFetcher;
import ape.runtime.data.Key;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.jvm.LivingDocumentFactory;

import java.util.Collection;
import java.util.HashMap;

/** ensures an instance is always alive by fetching plans... on demand  */
public class OndemandDeploymentFactoryBase implements LivingDocumentFactoryFactory, Undeploy, Deploy {
  private final DeploymentMetrics metrics;
  private final DeploymentFactoryBase base;
  private final PlanFetcher fetcher;
  private final DeploySync sync;

  public OndemandDeploymentFactoryBase(DeploymentMetrics metrics, DeploymentFactoryBase base, PlanFetcher fetcher, DeploySync sync) {
    this.metrics = metrics;
    this.base = base;
    this.fetcher = fetcher;
    this.sync = sync;
  }

  @Override
  public void fetch(Key key, Callback<LivingDocumentFactory> callback) {
    if (base.contains(key.space)) {
      metrics.deploy_cache_hit.run();
      base.fetch(key, callback);
    } else {
      metrics.deploy_cache_miss.run();
      fetcher.find(key.space, metrics.deploy_plan_fetch.wrap(new Callback<>() {
        @Override
        public void success(DeploymentBundle bundle) {
          base.deploy(key.space, bundle.plan, bundle.keys, new Callback<Void>() {
            @Override
            public void success(Void value) {
              sync.watch(key.space);
              base.fetch(key, callback);
            }

            @Override
            public void failure(ErrorCodeException ex) {
              callback.failure(ex);
            }
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          callback.failure(ex);
        }
      }));
    }
  }

  @Override
  public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {
    base.account(sample);
  }

  @Override
  public void deploy(String space, Callback<Void> callback) {
    fetcher.find(space, metrics.deploy_plan_push.wrap(new Callback<DeploymentBundle>() {
      @Override
      public void success(DeploymentBundle bundle) {
        base.deploy(space, bundle.plan, bundle.keys, new Callback<Void>() {
          @Override
          public void success(Void value) {
            sync.watch(space);
            callback.success(null);
          }

          @Override
          public void failure(ErrorCodeException ex) {
            callback.failure(ex);
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        callback.failure(ex);
      }
    }));
  }

  @Override
  public void undeploy(String space) {
    base.undeploy(space);
    metrics.deploy_undo.run();
    sync.unwatch(space);
  }

  @Override
  public Collection<String> spacesAvailable() {
    return base.spacesAvailable();
  }
}
