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
package ape.runtime.sys.readonly;

import ape.common.ErrorCodeException;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.contracts.DeploymentMonitor;
import ape.runtime.mocks.MockTime;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.ServiceShield;
import ape.runtime.sys.mocks.MockInstantLivingDocumentFactoryFactory;
import ape.runtime.sys.mocks.MockReplicationInitiator;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

public class ReadOnlyServiceTests {
  @Test
  public void shed_flow() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(ReadOnlyReplicaThreadBaseTests.SIMPLE_CODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory = new MockInstantLivingDocumentFactoryFactory(factory);
    ReplicationInitiator seed = new MockReplicationInitiator("{\"x\":123}", "{\"x\":42}");
    ReadOnlyService readonly = new ReadOnlyService(ReadOnlyReplicaThreadBaseTests.METRICS, new ServiceShield(), factoryFactory, seed, new MockTime(), 3);
    try {
      MockReadOnlyStream stream = new MockReadOnlyStream();
      Runnable latched1 = stream.latchAt(3);
      Runnable latched2 = stream.latchAt(4);
      Runnable latched3 = stream.latchAt(5);
      readonly.obverse(ContextSupport.WRAP(ReadOnlyReplicaThreadBaseTests.WHO), ReadOnlyReplicaThreadBaseTests.KEY,"{\"echo\":420}", stream);
      stream.await_began();
      latched1.run();
      Assert.assertEquals("{\"view-state-filter\":[\"echo\"]}", stream.get(0));
      Assert.assertEquals("{\"data\":{\"x\":123,\"foo\":420},\"seq\":0}", stream.get(1));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":0}", stream.get(2));
      readonly.shed((key) -> false);
      stream.get().update("{\"echo\":111}");
      latched2.run();
      Assert.assertEquals("{\"data\":{\"foo\":111},\"seq\":0}", stream.get(3));
      readonly.shed((key) -> true);
      latched3.run();
      Assert.assertEquals("CLOSED", stream.get(4));
    } finally {
      readonly.shutdown();
    }
  }

  @Test
  public void deployment_flow() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(ReadOnlyReplicaThreadBaseTests.SIMPLE_CODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory = new MockInstantLivingDocumentFactoryFactory(factory);
    ReplicationInitiator seed = new MockReplicationInitiator("{\"x\":123}", "{\"x\":42}");
    ReadOnlyService readonly = new ReadOnlyService(ReadOnlyReplicaThreadBaseTests.METRICS, new ServiceShield(), factoryFactory, seed, new MockTime(), 3);
    try {
      MockReadOnlyStream stream = new MockReadOnlyStream();
      Runnable latched1 = stream.latchAt(3);
      Runnable latched2 = stream.latchAt(4);
      Runnable latched3 = stream.latchAt(5);
      readonly.obverse(ContextSupport.WRAP(ReadOnlyReplicaThreadBaseTests.WHO), ReadOnlyReplicaThreadBaseTests.KEY,"{\"echo\":420}", stream);
      stream.await_began();
      latched1.run();
      Assert.assertEquals("{\"data\":{\"x\":123,\"foo\":420},\"seq\":0}", stream.get(1));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":0}", stream.get(2));
      readonly.shed((key) -> false);
      stream.get().update("{\"echo\":111}");
      latched2.run();
      Assert.assertEquals("{\"data\":{\"foo\":111},\"seq\":0}", stream.get(3));
      factoryFactory.set(LivingDocumentTests.compile(ReadOnlyReplicaThreadBaseTests.SIMPLE_CODE_DEPLOYED, Deliverer.FAILURE));
      readonly.deploy(new DeploymentMonitor() {
        @Override
        public void bumpDocument(boolean changed) {

        }

        @Override
        public void witnessException(ErrorCodeException ex) {

        }

        @Override
        public void finished(int ms) {

        }
      });
      latched3.run();
      Assert.assertEquals("CLOSED", stream.get(4));
    } finally {
      readonly.shutdown();
    }
  }
}
