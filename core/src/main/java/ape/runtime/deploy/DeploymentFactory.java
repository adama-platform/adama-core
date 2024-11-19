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
