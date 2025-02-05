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
package ape.runtime.remote.replication;

import ape.common.SimpleExecutor;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockLivingDocument;
import ape.runtime.mocks.MockRxParent;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.reactives.RxInt64;
import ape.runtime.reactives.RxLazy;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class ReplicationEngineTests {
  @Test
  public void instant_single() {
    MockLivingDocument doc = new MockLivingDocument();
    ReplicationEngine re = new ReplicationEngine(doc);
    {
      MockRxParent parent = new MockRxParent();
      RxReplicationStatus status = new RxReplicationStatus(parent, new RxInt64(null, 10000), "rservice", "method");
      AtomicReference<NtToDynamic> value = new AtomicReference<>(MockReplicationService.SIMPLE_KEY_OBJECT("key2use"));
      RxLazy<NtToDynamic> lazy = new RxLazy<>(null, () -> value.get(), null);
      lazy.__subscribe(status);
      re.link(status, lazy);
      {
        JsonStreamWriter forward = new JsonStreamWriter();
        JsonStreamWriter reverse = new JsonStreamWriter();
        re.commit(forward, reverse);
        Assert.assertEquals("", forward.toString());
        Assert.assertEquals("", reverse.toString());
        JsonStreamWriter dump = new JsonStreamWriter();
        re.dump(dump);
        Assert.assertEquals("{}", dump.toString());
      }
      re.signalDurableAndExecute(SimpleExecutor.NOW);
      {
        JsonStreamWriter forward = new JsonStreamWriter();
        JsonStreamWriter reverse = new JsonStreamWriter();
        re.commit(forward, reverse);
        Assert.assertEquals("", forward.toString());
        Assert.assertEquals("", reverse.toString());
        JsonStreamWriter dump = new JsonStreamWriter();
        re.dump(dump);
        Assert.assertEquals("{}", dump.toString());
      }
      parent.alive = false;

      {
        JsonStreamWriter forward = new JsonStreamWriter();
        JsonStreamWriter reverse = new JsonStreamWriter();
        re.commit(forward, reverse);
        Assert.assertEquals("\"__replication\":{\"5TtJHVK2lBxrADSFYheExQ==\":{\"s\":\"rservice\",\"m\":\"method\",\"k\":\"key2use\"}}", forward.toString());
        Assert.assertEquals("\"__replication\":{}", reverse.toString());
        JsonStreamWriter dump = new JsonStreamWriter();
        re.dump(dump);
        Assert.assertEquals("{\"5TtJHVK2lBxrADSFYheExQ==\":{\"s\":\"rservice\",\"m\":\"method\",\"k\":\"key2use\"}}", dump.toString());
      }
      re.signalDurableAndExecute(SimpleExecutor.NOW);
      {
        JsonStreamWriter forward = new JsonStreamWriter();
        JsonStreamWriter reverse = new JsonStreamWriter();
        re.commit(forward, reverse);
        Assert.assertEquals("\"__replication\":{\"5TtJHVK2lBxrADSFYheExQ==\":null}", forward.toString());
        Assert.assertEquals("\"__replication\":{}", reverse.toString());
        JsonStreamWriter dump = new JsonStreamWriter();
        re.dump(dump);
        Assert.assertEquals("{}", dump.toString());
      }
      Assert.assertEquals("BEGIN[method]:key2use", doc.rservice.at(0));
      Assert.assertEquals("ASK[method]:key2use", doc.rservice.at(1));
      Assert.assertEquals("SUCCESS[method]:key2use", doc.rservice.at(2));
      Assert.assertEquals("BEGIN[method]:key2use", doc.rservice.at(3));
      Assert.assertEquals("ASK[method]:key2use", doc.rservice.at(4));
      Assert.assertEquals("DELETED[method]:key2use", doc.rservice.at(5));
    }
  }

  @Test
  public void instant_load_resurrect_tombstones() {
    MockLivingDocument doc = new MockLivingDocument();
    ReplicationEngine re = new ReplicationEngine(doc);
    RxInt64 time = new RxInt64(null, 10000);
    re.load(new JsonStreamReader("{\"5TtJHVK2lBxrADSFYheExQ==\":{\"s\":\"rservice\",\"m\":\"method\",\"k\":\"key2use\"}}"), time);
    {
      {
        JsonStreamWriter forward = new JsonStreamWriter();
        JsonStreamWriter reverse = new JsonStreamWriter();
        re.commit(forward, reverse);
        Assert.assertEquals("", forward.toString());
        Assert.assertEquals("", reverse.toString());
        JsonStreamWriter dump = new JsonStreamWriter();
        re.dump(dump);
        Assert.assertEquals("{\"5TtJHVK2lBxrADSFYheExQ==\":{\"s\":\"rservice\",\"m\":\"method\",\"k\":\"key2use\"}}", dump.toString());
      }
      re.signalDurableAndExecute(SimpleExecutor.NOW);
      {
        JsonStreamWriter forward = new JsonStreamWriter();
        JsonStreamWriter reverse = new JsonStreamWriter();
        re.commit(forward, reverse);
        Assert.assertEquals("\"__replication\":{\"5TtJHVK2lBxrADSFYheExQ==\":null}", forward.toString());
        Assert.assertEquals("\"__replication\":{}", reverse.toString());
        JsonStreamWriter dump = new JsonStreamWriter();
        re.dump(dump);
        Assert.assertEquals("{}", dump.toString());
      }
      Assert.assertEquals("DELETED[method]:key2use", doc.rservice.at(0));
    }
  }
}
