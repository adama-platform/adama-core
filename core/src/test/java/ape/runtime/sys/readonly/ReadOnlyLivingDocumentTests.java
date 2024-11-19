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
