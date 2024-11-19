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
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class DeploymentFactoryBaseTests {
  @Test
  public void coverage_dumb() {
    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    base.fetch(
        new Key("space", "key"),
        new Callback<LivingDocumentFactory>() {
          @Override
          public void success(LivingDocumentFactory value) {
            Assert.fail();
          }

          @Override
          public void failure(ErrorCodeException ex) {
            Assert.assertEquals(134214, ex.code);
          }
        });
    Assert.assertEquals(0, base.spacesAvailable().size());
    Assert.assertNull(base.hashOf("space"));
    base.attachDeliverer(Deliverer.FAILURE);
    base.deliver(NtPrincipal.NO_ONE, new Key("space", "key"), 400, null, true, Callback.DONT_CARE_INTEGER);
    base.undeploy("space");
    base.account(new HashMap<>());
  }
}
