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
import ape.runtime.sys.CoreService;
import ape.runtime.sys.TriggerDeployment;

import java.util.ArrayList;
import java.util.function.BiConsumer;

/** the cyclic reference for the binding of deployments to deployment */
public class DelayedDeploy implements Deploy {
  private Deploy actual;
  private CoreService service;
  private ArrayList<BiConsumer<Deploy, CoreService>> delayed;

  public DelayedDeploy() {
    this.actual = null;
    this.delayed = new ArrayList<>();
  }

  @Override
  public synchronized void deploy(String space, Callback<Void> callback) {
    if (this.actual == null) {
      delayed.add((d, s) -> d.deploy(space, new TriggerDeployment(service, callback)));
    } else {
      this.actual.deploy(space, new TriggerDeployment(service, callback));
    }
  }

  public synchronized void set(Deploy deploy, CoreService service) {
    this.actual = deploy;
    this.service = service;
    for (BiConsumer<Deploy, CoreService> c : delayed) {
      c.accept(actual, service);
    }
    delayed = null;
  }
}
