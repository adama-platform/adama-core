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
package ape.runtime.sys;

import ape.common.Callback;
import ape.runtime.deploy.MockDeploy;
import ape.runtime.sys.capacity.MockCapacityOverseer;
import org.junit.Assert;
import org.junit.Test;

public class ServiceBootTests {

  @Test
  public void coverage() {
    MockCapacityOverseer overseer = new MockCapacityOverseer();
    overseer.add("a", "region", "machine", Callback.DONT_CARE_VOID);
    overseer.add("b", "region", "machine", Callback.DONT_CARE_VOID);
    MockDeploy deploy = new MockDeploy();
    ServiceBoot.initializeWithDeployments("region", "machine", overseer, deploy, 5000);
    Assert.assertEquals(2, deploy.deployed.size());
  }
}
