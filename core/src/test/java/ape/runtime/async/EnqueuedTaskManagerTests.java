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
package ape.runtime.async;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class EnqueuedTaskManagerTests {
  @Test
  public void flow() {
    EnqueuedTaskManager etm = new EnqueuedTaskManager();
    {
      etm.hydrate(new JsonStreamReader("{}"));
      etm.hydrate(new JsonStreamReader("null"));
      JsonStreamWriter writer = new JsonStreamWriter();
      etm.dump(writer);
      etm.commit(writer, writer);
      Assert.assertEquals("", writer.toString());
      Assert.assertEquals(0, etm.size());
    }
    Assert.assertEquals(0, etm.size());
    Assert.assertFalse(etm.readyForTransfer());
    etm.add(new EnqueuedTask(40, new NtPrincipal("agent", "auth"), "ch", -1, new NtDynamic("[1,2]")));
    Assert.assertFalse(etm.readyForTransfer());
    Assert.assertEquals(1, etm.size());
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      etm.commit(forward, reverse);
      Assert.assertEquals("\"__enqueued\":{\"40\":{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"channel\":\"ch\",\"message\":[1,2]}}", forward.toString());
      Assert.assertEquals("\"__enqueued\":{\"40\":null}", reverse.toString());
      JsonStreamWriter dump = new JsonStreamWriter();
      etm.dump(dump);
      Assert.assertEquals("\"__enqueued\":{\"40\":{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"channel\":\"ch\",\"message\":[1,2]}}", dump.toString());
    }
    Assert.assertTrue(etm.readyForTransfer());
    EnqueuedTask pulled = etm.transfer();
    Assert.assertNotNull(pulled);
    Assert.assertEquals(0, etm.size());
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      etm.commit(forward, reverse);
      Assert.assertEquals("\"__enqueued\":{\"40\":null}", forward.toString());
      Assert.assertEquals("\"__enqueued\":{\"40\":{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"channel\":\"ch\",\"message\":[1,2]}}", reverse.toString());
      JsonStreamWriter dump = new JsonStreamWriter();
      etm.dump(dump);
      Assert.assertEquals("", dump.toString());
    }
    {
      JsonStreamWriter dump = new JsonStreamWriter();
      etm.dump(dump);
      Assert.assertEquals("", dump.toString());
    }
  }

  @Test
  public void flow_w_view_id() {
    EnqueuedTaskManager etm = new EnqueuedTaskManager();
    {
      etm.hydrate(new JsonStreamReader("{}"));
      etm.hydrate(new JsonStreamReader("null"));
      JsonStreamWriter writer = new JsonStreamWriter();
      etm.dump(writer);
      etm.commit(writer, writer);
      Assert.assertEquals("", writer.toString());
      Assert.assertEquals(0, etm.size());
    }
    Assert.assertEquals(0, etm.size());
    Assert.assertFalse(etm.readyForTransfer());
    etm.add(new EnqueuedTask(40, new NtPrincipal("agent", "auth"), "ch", 42, new NtDynamic("[1,2]")));
    Assert.assertFalse(etm.readyForTransfer());
    Assert.assertEquals(1, etm.size());
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      etm.commit(forward, reverse);
      Assert.assertEquals("\"__enqueued\":{\"40\":{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"channel\":\"ch\",\"view_id\":42,\"message\":[1,2]}}", forward.toString());
      Assert.assertEquals("\"__enqueued\":{\"40\":null}", reverse.toString());
      JsonStreamWriter dump = new JsonStreamWriter();
      etm.dump(dump);
      Assert.assertEquals("\"__enqueued\":{\"40\":{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"channel\":\"ch\",\"view_id\":42,\"message\":[1,2]}}", dump.toString());
    }
    Assert.assertTrue(etm.readyForTransfer());
    EnqueuedTask pulled = etm.transfer();
    Assert.assertNotNull(pulled);
    Assert.assertEquals(0, etm.size());
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      etm.commit(forward, reverse);
      Assert.assertEquals("\"__enqueued\":{\"40\":null}", forward.toString());
      Assert.assertEquals("\"__enqueued\":{\"40\":{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"channel\":\"ch\",\"view_id\":42,\"message\":[1,2]}}", reverse.toString());
      JsonStreamWriter dump = new JsonStreamWriter();
      etm.dump(dump);
      Assert.assertEquals("", dump.toString());
    }
    {
      JsonStreamWriter dump = new JsonStreamWriter();
      etm.dump(dump);
      Assert.assertEquals("", dump.toString());
    }
  }

  @Test
  public void hydration() {
    EnqueuedTaskManager etm = new EnqueuedTaskManager();
    etm.hydrate(new JsonStreamReader("{\"1\":{\"channel\":\"ch\",\"who\":{\"agent\":\"a\",\"authority\":\"b\"},\"message\":{}}}}"));
    etm.hydrate(new JsonStreamReader("{\"1\":{\"x\":true}}"));
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      etm.dump(writer);
      Assert.assertEquals("\"__enqueued\":{\"1\":{\"who\":{\"agent\":\"a\",\"authority\":\"b\"},\"channel\":\"ch\",\"message\":{}}}", writer.toString());
    }
    Assert.assertTrue(etm.readyForTransfer());
    EnqueuedTask et = etm.transfer();
    Assert.assertEquals("ch", et.channel);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      etm.dump(writer);
      Assert.assertEquals("", writer.toString());
    }
  }

  @Test
  public void hydration_view_id() {
    EnqueuedTaskManager etm = new EnqueuedTaskManager();
    etm.hydrate(new JsonStreamReader("{\"1\":{\"channel\":\"ch\",\"who\":{\"agent\":\"a\",\"authority\":\"b\"},\"view_id\":400,\"message\":{}}}}"));
    etm.hydrate(new JsonStreamReader("{\"1\":{\"x\":true}}"));
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      etm.dump(writer);
      Assert.assertEquals("\"__enqueued\":{\"1\":{\"who\":{\"agent\":\"a\",\"authority\":\"b\"},\"channel\":\"ch\",\"view_id\":400,\"message\":{}}}", writer.toString());
    }
    Assert.assertTrue(etm.readyForTransfer());
    EnqueuedTask et = etm.transfer();
    Assert.assertEquals("ch", et.channel);
    Assert.assertEquals(400, et.viewId);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      etm.dump(writer);
      Assert.assertEquals("", writer.toString());
    }
  }

  @Test
  public void revert() {
    EnqueuedTaskManager etm = new EnqueuedTaskManager();
    etm.add(new EnqueuedTask(1, NtPrincipal.NO_ONE, "chan", 40, new NtDynamic("{}")));
    Assert.assertEquals(1, etm.size());
    etm.revert();
    Assert.assertEquals(0, etm.size());

  }
}
