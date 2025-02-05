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
package ape.runtime.sys;

import ape.common.AwaitHelper;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.FinderService;
import ape.runtime.data.Key;
import ape.runtime.deploy.Deploy;
import ape.runtime.sys.capacity.CapacityInstance;
import ape.runtime.sys.capacity.CapacityOverseer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/** a block to help a service get booted */
public class ServiceBoot {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceBoot.class);

  /** [sync] pull down what capacity has been bound to this machine, and then deploy the plans */
  public static void initializeWithDeployments(String region, String machine, CapacityOverseer overseer, Deploy deploy, int deployBootTimeMilliseconds) {
    CountDownLatch latch = new CountDownLatch(1);
    overseer.listAllOnMachine(region, machine, new Callback<>() {
      @Override
      public void success(List<CapacityInstance> instances) {
        CountDownLatch deployed = new CountDownLatch(instances.size());
        for (CapacityInstance instance : instances) {
          deploy.deploy(instance.space, new Callback<Void>() {
            private final String space = instance.space;

            @Override
            public void success(Void value) {
              deployed.countDown();
            }

            @Override
            public void failure(ErrorCodeException ex) {
              LOG.error("failed-deploy:" + space, ex);
              deployed.countDown();
            }
          });
        }
        AwaitHelper.block(deployed, deployBootTimeMilliseconds);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        LOG.error("failed-capacity-listing", ex);
        latch.countDown();
      }
    });
    AwaitHelper.block(latch, deployBootTimeMilliseconds + 250);
  }

  /** [async-optimistic] initialize documents on the host */
  public static void startup(FinderService finder, CoreService service) {
    finder.list(new Callback<List<Key>>() {
      @Override
      public void success(List<Key> keys) {
        for (Key key : keys) {
          service.startupLoad(key);
        }
      }

      @Override
      public void failure(ErrorCodeException ex) {
        System.exit(-1);
      }
    });
    finder.listDeleted(new Callback<List<Key>>() {
      @Override
      public void success(List<Key> value) {
        // TODO: re-issue a delete
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
  }
}
