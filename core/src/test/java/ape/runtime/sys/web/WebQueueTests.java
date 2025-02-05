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
package ape.runtime.sys.web;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.runtime.async.EphemeralFuture;
import ape.runtime.async.IdHistoryLog;
import ape.runtime.data.Key;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockDeliverer;
import ape.runtime.mocks.MockLivingDocument;
import ape.runtime.mocks.MockMessage;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.reactives.RxInt32;
import ape.runtime.contracts.DelayParent;
import ape.runtime.remote.RemoteResult;
import ape.runtime.remote.RxCache;
import ape.runtime.remote.ServiceRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class WebQueueTests {
  @Test
  public void coverage_cancel() {
    MockLivingDocument doc = new MockLivingDocument();
    MockDeliverer deliverer = new MockDeliverer();
    ServiceRegistry registry = new ServiceRegistry("space");
    doc.__lateBind("space", "key", deliverer, registry);
    DelayParent delay = new DelayParent();
    RxCache cache = new RxCache(doc, delay);
    ArrayList<Runnable> tasks = new ArrayList<>();
    BiConsumer<Integer, String> service = (id, s) -> tasks.add(() -> {
      deliverer.deliver(NtPrincipal.NO_ONE, new Key("space", "key"), id, new RemoteResult(s, null, null), true, Callback.DONT_CARE_INTEGER);
    });
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    EphemeralFuture<WebResponse> fut = new EphemeralFuture<>();
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
    }

    queue.queue(context, new WebPut(context, "/uri", new TreeMap<>(), new NtDynamic("{}"), "{\"body\":123}"), fut, cache, new IdHistoryLog(), delay);
    queue.dirty();

    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
    }

    AtomicBoolean saw_failure = new AtomicBoolean(false);
    fut.attach(new Callback<WebResponse>() {
      @Override
      public void success(WebResponse value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        saw_failure.set(true);
      }
    });
    queue.cancel();
    Assert.assertTrue(saw_failure.get());
  }

  @Test
  public void coverage_delivery_put() {
    MockLivingDocument doc = new MockLivingDocument();
    MockDeliverer deliverer = new MockDeliverer();
    ServiceRegistry registry = new ServiceRegistry("space");
    doc.__lateBind("space", "key", deliverer, registry);
    DelayParent delay = new DelayParent();
    RxCache cache = new RxCache(doc, delay);
    ArrayList<Runnable> tasks = new ArrayList<>();
    BiFunction<Integer, String, RemoteResult> service = (id, s) -> {
      tasks.add(() -> {
        deliverer.deliver(NtPrincipal.NO_ONE, new Key("space", "key"), id, new RemoteResult(s, null, null), true, Callback.DONT_CARE_INTEGER);
      });
      return null;
    };
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    EphemeralFuture<WebResponse> fut = new EphemeralFuture<>();
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    queue.queue(context, new WebPut(context, "/uri", new TreeMap<>(), new NtDynamic("{}"), "{\"body\":123}"), fut, cache, new IdHistoryLog(), delay);
    queue.dirty();
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
    }

    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":null}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    cache.answer("service", "method", NtPrincipal.NO_ONE, new MockMessage(123, 42), (str) -> new MockMessage(new JsonStreamReader(str)), service);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":null}}}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    for (Runnable task : tasks) {
      task.run();
    }
    deliverer.deliverAllTo(cache);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":{\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":{\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
  }

  @Test
  public void coverage_delivery_instant() {
    MockLivingDocument doc = new MockLivingDocument();
    MockDeliverer deliverer = new MockDeliverer();
    ServiceRegistry registry = new ServiceRegistry("space");
    doc.__lateBind("space", "key", deliverer, registry);
    DelayParent delay = new DelayParent();
    RxCache cache = new RxCache(doc, delay);
    ArrayList<Runnable> tasks = new ArrayList<>();
    BiFunction<Integer, String, RemoteResult> service = (id, s) -> {
      return new RemoteResult(s, null, null);
    };
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    EphemeralFuture<WebResponse> fut = new EphemeralFuture<>();
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    queue.queue(context, new WebPut(context, "/uri", new TreeMap<>(), new NtDynamic("{}"), "{\"body\":123}"), fut, cache, new IdHistoryLog(), delay);
    queue.dirty();
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
    }

    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":null}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    cache.answer("service", "method", NtPrincipal.NO_ONE, new MockMessage(123, 42), (str) -> new MockMessage(new JsonStreamReader(str)), service);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":null}}}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
  }

  @Test
  public void coverage_delivery_delete() {
    MockLivingDocument doc = new MockLivingDocument();
    MockDeliverer deliverer = new MockDeliverer();
    ServiceRegistry registry = new ServiceRegistry("space");
    doc.__lateBind("space", "key", deliverer, registry);
    DelayParent delay = new DelayParent();
    RxCache cache = new RxCache(doc, delay);
    ArrayList<Runnable> tasks = new ArrayList<>();
    BiFunction<Integer, String, RemoteResult> service = (id, s) -> {
      tasks.add(() -> {
        deliverer.deliver(NtPrincipal.NO_ONE, new Key("space", "key"), id, new RemoteResult(s, null, null), true, Callback.DONT_CARE_INTEGER);
      });
      return null;
    };
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    EphemeralFuture<WebResponse> fut = new EphemeralFuture<>();
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    queue.queue(context, new WebDelete(context, "/uri", new TreeMap<>(), new NtDynamic("{}")), fut, cache, new IdHistoryLog(), delay);
    queue.dirty();
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
    }

    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":null}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    cache.answer("service", "method", NtPrincipal.NO_ONE, new MockMessage(123, 42), (str) -> new MockMessage(new JsonStreamReader(str)), service);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":null}}}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
    for (Runnable task : tasks) {
      task.run();
    }
    deliverer.deliverAllTo(cache);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      queue.commit(forward, reverse);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":{\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}", forward.toString());
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"cache\":{\"1\":{\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", reverse.toString());
      Json.parseJsonObject("{" + forward + "}");
      Json.parseJsonObject("{" + reverse + "}");
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
      System.err.println("{" + writer.toString() + "}");
    }
  }

  @Test
  public void hydrate_empty() {
    MockLivingDocument doc = new MockLivingDocument();
    JsonStreamReader reader = new JsonStreamReader("{}");
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    queue.hydrate(reader, doc);
    JsonStreamWriter writer = new JsonStreamWriter();
    queue.dump(writer);
    Assert.assertEquals("\"__webqueue\":{}", writer.toString());
    Json.parseJsonObject("{" + writer.toString() + "}");
  }

  @Test
  public void hydrate_setup_put() {
    MockLivingDocument doc = new MockLivingDocument();
    JsonStreamReader reader = new JsonStreamReader("{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}");
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    queue.hydrate(reader, doc);
    JsonStreamWriter writer = new JsonStreamWriter();
    queue.dump(writer);
    Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{}}}", writer.toString());
    Json.parseJsonObject("{" + writer.toString() + "}");
  }


  @Test
  public void hydrate_setup_delete() {
    MockLivingDocument doc = new MockLivingDocument();
    JsonStreamReader reader = new JsonStreamReader("{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{}}}");
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    queue.hydrate(reader, doc);
    JsonStreamWriter writer = new JsonStreamWriter();
    queue.dump(writer);
    Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{}}}", writer.toString());
    Json.parseJsonObject("{" + writer.toString() + "}");
  }

  @Test
  public void hydrate_answer_put() {
    MockLivingDocument doc = new MockLivingDocument();
    JsonStreamReader reader = new JsonStreamReader("{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}");
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    queue.hydrate(reader, doc);
    JsonStreamWriter writer = new JsonStreamWriter();
    queue.dump(writer);
    Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
    Json.parseJsonObject("{" + writer.toString() + "}");
  }

  @Test
  public void hydrate_answer_delete() {
    MockLivingDocument doc = new MockLivingDocument();
    JsonStreamReader reader = new JsonStreamReader("{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"junk\":[],\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}");
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    queue.hydrate(reader, doc);
    JsonStreamWriter writer = new JsonStreamWriter();
    queue.dump(writer);
    Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"delete\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
    Json.parseJsonObject("{" + writer.toString() + "}");
  }

  @Test
  public void hydrate_full() {
    MockLivingDocument doc = new MockLivingDocument();
    JsonStreamReader reader = new JsonStreamReader("{\"7\":null,\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}");
    RxInt32 taskId = new RxInt32(doc, 1);
    WebQueue queue = new WebQueue(taskId);
    queue.hydrate(reader, doc);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{\"2\":{\"context\":{\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"origin\":\"origin\",\"ip\":\"ip\"},\"item\":{\"put\":{\"uri\":\"/uri\",\"headers\":{},\"parameters\":{},\"bodyJson\":{\"body\":123}}},\"cache\":{\"1\":{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"x\":123,\"y\":42}},\"result\":{\"result\":{\"x\":123,\"y\":42},\"failure\":null,\"failure_code\":null}}}}}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
    }
    queue.hydrate(new JsonStreamReader("null"), doc);
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      queue.dump(writer);
      Assert.assertEquals("\"__webqueue\":{}", writer.toString());
      Json.parseJsonObject("{" + writer.toString() + "}");
    }
  }
}
