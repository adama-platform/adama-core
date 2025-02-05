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
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.data.Key;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.jvm.LivingDocumentFactory;

import java.util.*;

/**
 * converts a DeploymentPlan into a LivingDocumentFactoryFactory; if this can be created, then it is
 * in good order
 */
public class DeploymentFactory implements LivingDocumentFactoryFactory {
  public final String name;
  public final DeploymentPlan plan;
  public final long memoryUsed;
  public final HashMap<String, LivingDocumentFactory> factories;

  public DeploymentFactory(String name, DeploymentPlan plan, long memoryUsed, HashMap<String, LivingDocumentFactory> factories) {
    this.name = name;
    this.plan = plan;
    this.memoryUsed = memoryUsed;
    this.factories = factories;
  }

  @Override
  public void fetch(Key key, Callback<LivingDocumentFactory> callback) {
    String versionToUse = plan.pickVersion(key.key);
    callback.success(factories.get(versionToUse));
  }

  @Override
  public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {
    PredictiveInventory.MeteringSample prior = sample.get(name);
    if (prior == null) {
      sample.put(name, PredictiveInventory.MeteringSample.justMemory(memoryUsed));
    } else {
      sample.put(name, PredictiveInventory.MeteringSample.add(prior, PredictiveInventory.MeteringSample.justMemory(memoryUsed)));
    }
  }

  @Override
  public Collection<String> spacesAvailable() {
    return Collections.singleton(name);
  }
}
