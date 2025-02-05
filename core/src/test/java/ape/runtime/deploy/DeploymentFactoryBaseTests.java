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
