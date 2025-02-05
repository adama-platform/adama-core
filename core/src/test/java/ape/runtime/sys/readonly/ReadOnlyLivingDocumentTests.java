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

import ape.runtime.LivingDocumentTests;
import ape.runtime.mocks.MockTime;
import ape.runtime.remote.Deliverer;
import ape.runtime.remote.replication.SequencedTestExecutor;
import ape.runtime.sys.LivingDocument;
import ape.runtime.sys.mocks.MockInstantLivingDocumentFactoryFactory;
import ape.runtime.sys.mocks.MockReplicationInitiator;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReadOnlyLivingDocumentTests {
  @Test
  public void esoteric_cancel_kill_race() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(ReadOnlyReplicaThreadBaseTests.SIMPLE_CODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory = new MockInstantLivingDocumentFactoryFactory(factory);
    SequencedTestExecutor executor = new SequencedTestExecutor();
    ReplicationInitiator seed = new MockReplicationInitiator("{\"x\":123}", "{\"x\":42}");
    ReadOnlyReplicaThreadBase base = ReadOnlyReplicaThreadBaseTests.baseOf(executor, seed, factoryFactory);
    LivingDocument real = factory.create(null);
    ReadOnlyLivingDocument document = new ReadOnlyLivingDocument(base, ReadOnlyReplicaThreadBaseTests.KEY, real, factory);
    document.kill();
    AtomicBoolean setInstant = new AtomicBoolean(false);
    document.setCancel(() -> {
      setInstant.set(true);
    });
    Assert.assertTrue(setInstant.get());
  }

  @Test
  public void silly() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(ReadOnlyReplicaThreadBaseTests.SIMPLE_CODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory = new MockInstantLivingDocumentFactoryFactory(factory);
    SequencedTestExecutor executor = new SequencedTestExecutor();
    ReplicationInitiator seed = new MockReplicationInitiator("{\"x\":123}", "{\"x\":42}");
    ReadOnlyReplicaThreadBase base = ReadOnlyReplicaThreadBaseTests.baseOf(executor, seed, factoryFactory);
    try {
      LivingDocument real = factory.create(null);
      ReadOnlyLivingDocument document = new ReadOnlyLivingDocument(base, ReadOnlyReplicaThreadBaseTests.KEY, real, factory);
      document.getCodeCost();
      document.getCpuMilliseconds();
      document.getConnectionsCount();
      document.getMemoryBytes();
      document.zeroOutCodeCost();
      Assert.assertFalse(document.testInactive());
      ((MockTime) base.time).set(10000000);
      Assert.assertTrue(document.testInactive());
    } finally {
      base.close();
    }
  }
}
