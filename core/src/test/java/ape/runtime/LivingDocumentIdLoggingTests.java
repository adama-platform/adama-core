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
package ape.runtime;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.Perspective;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.web.WebContext;
import ape.runtime.sys.web.WebDelete;
import ape.runtime.sys.web.WebPut;
import ape.runtime.sys.web.WebResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LivingDocumentIdLoggingTests {
  private static NtPrincipal A = new NtPrincipal("A", "TEST");

  ArrayList<String> bindList(NtPrincipal who, RealDocumentSetup setup) {
    RealDocumentSetup.GotView gv = new RealDocumentSetup.GotView();
    ArrayList<String> list = new ArrayList<>();
    Perspective linked =
        new Perspective() {
          @Override
          public void data(String data) {
            list.add(data);
          }

          @Override
          public void disconnect() {}
        };
    setup.document.createPrivateView(who, linked, new JsonStreamReader("{}"), gv);
    return list;
  }

  @Test
  public void simple() throws Exception{
    RealDocumentSetup setup = new RealDocumentSetup(
        "@connected { return true; }" + //
            "message N { int n; }" + //
            "record R { public int id; public int n; }" + //
            "table<R> tbl;" +
            "public formula t = iterate tbl order by id;" + //
            "channel foo(N n) { tbl <- n; }", //
        null);
    ArrayList<String> data = bindList(A, setup);
    setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(3));
    setup.document.send(ContextSupport.WRAP(A), null, null, "foo", "{\"n\":123}", new RealDocumentSetup.AssertInt(5));
    setup.document.send(ContextSupport.WRAP(A), null, null, "foo", "{\"n\":13}", new RealDocumentSetup.AssertInt(6));
    Assert.assertEquals(4, data.size());
    Assert.assertEquals("{\"seq\":2}", data.get(0));
    Assert.assertEquals("{\"seq\":4}", data.get(1));
    Assert.assertEquals("{\"data\":{\"t\":{\"1\":{\"id\":1,\"n\":123},\"@o\":[1]}},\"seq\":5}", data.get(2));
    Assert.assertEquals("{\"data\":{\"t\":{\"2\":{\"id\":2,\"n\":13},\"@o\":[1,2]}},\"seq\":6}", data.get(3));
  }

  @Test
  public void blocked_inserts() throws Exception {
    RealDocumentSetup setup = new RealDocumentSetup(
        "@connected { return true; }" + //
            "message N { int n; }" + //
            "channel<N> b;" + //
            "record R { public int id; public int n; }" + //
            "table<R> tbl;" +
            "public formula t = iterate tbl order by id;" + //
            "channel foo(N n) { tbl <- n; b.fetch(@who).await(); }", //
        null);
    setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
    ArrayList<String> data = bindList(A, setup);
    setup.document.send(ContextSupport.WRAP(A), null, null, "foo", "{\"n\":123}", new RealDocumentSetup.AssertInt(5));
    setup.document.send(ContextSupport.WRAP(A), null, null, "foo", "{\"n\":13}", new RealDocumentSetup.AssertInt(7));
    setup.document.send(ContextSupport.WRAP(A), null, null, "foo", "{\"n\":69}", new RealDocumentSetup.AssertInt(9));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(11));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(14));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(17));
    Assert.assertEquals(7, data.size());
    Assert.assertEquals("{\"seq\":4}", data.get(0));
    Assert.assertEquals("{\"data\":{\"t\":{\"1\":{\"id\":1,\"n\":123},\"@o\":[1]}},\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":5}", data.get(1));
    Assert.assertEquals("{\"seq\":7}", data.get(2));
    Assert.assertEquals("{\"seq\":9}", data.get(3));
    Assert.assertEquals("{\"data\":{\"t\":{\"2\":{\"id\":2,\"n\":13},\"@o\":[1,2]}},\"outstanding\":[{\"id\":2,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":12}", data.get(4));
    Assert.assertEquals("{\"data\":{\"t\":{\"3\":{\"id\":3,\"n\":69},\"@o\":[1,2,3]}},\"outstanding\":[{\"id\":3,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":15}", data.get(5));
    Assert.assertEquals("{\"outstanding\":[],\"blockers\":[],\"seq\":19}", data.get(6));
  }

  public Runnable put(RealDocumentSetup setup, NtPrincipal who, String uri, String body) throws Exception {
    WebPut put = new WebPut(new WebContext(who, "origin", "ip"), uri, new TreeMap<>(), new NtDynamic("{}"), body);
    CountDownLatch gotResponse = new CountDownLatch(1);
    setup.document.webPut(put, new Callback<WebResponse>() {
      @Override
      public void success(WebResponse value) {
        System.err.println("PUT RESULT:" + value.body);
        gotResponse.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        System.err.println("PUT FAIL:" + ex.code);
      }
    });
    return () -> {
      try {
        Assert.assertTrue(gotResponse.await(5000, TimeUnit.MILLISECONDS));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    };
  }

  public Runnable delete(RealDocumentSetup setup, NtPrincipal who, String uri) throws Exception {
    WebDelete del = new WebDelete(new WebContext(who, "origin", "ip"), uri, new TreeMap<>(), new NtDynamic("{}"));
    CountDownLatch gotResponse = new CountDownLatch(1);
    setup.document.webDelete(del, new Callback<WebResponse>() {
      @Override
      public void success(WebResponse value) {
        System.err.println("DELETE RESULT:" + value.body);
        gotResponse.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        System.err.println("DELETE FAIL:" + ex.code);
      }
    });
    return () -> {
      try {
        Assert.assertTrue(gotResponse.await(5000, TimeUnit.MILLISECONDS));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    };
  }

  @Test
  public void blocked_puts() throws Exception {
    RealDocumentSetup setup = new RealDocumentSetup(
        "@connected { return true; }" + //
            "message N { int n; }" + //
            "channel<N> b;" + //
            "record R { public int id; public int n; }" + //
            "table<R> tbl;" +
            "public formula t = iterate tbl order by id;" + //
            "@web put /data (N n) { tbl <- n; b.fetch(@who).await(); return {html:\"ok\"}; }", //
        null);
    setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
    ArrayList<String> data = bindList(A, setup);
    Runnable p123 = put(setup, A, "/data", "{\"n\":123}");
    Runnable p13 = put(setup, A, "/data", "{\"n\":13}");
    Runnable p69 = put(setup, A, "/data", "{\"n\":69}");
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(8));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(10));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(12));
    p123.run();
    p13.run();
    p69.run();
    Assert.assertEquals(7, data.size());
    Assert.assertEquals("{\"seq\":4}", data.get(0));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":5}", data.get(1));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false},{\"id\":2,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":6}", data.get(2));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false},{\"id\":2,\"channel\":\"b\",\"array\":false},{\"id\":3,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":7}", data.get(3));
    Assert.assertEquals("{\"data\":{\"t\":{\"1\":{\"id\":1,\"n\":123},\"@o\":[1]}},\"outstanding\":[],\"blockers\":[],\"seq\":9}", data.get(4));
    Assert.assertEquals("{\"data\":{\"t\":{\"2\":{\"id\":2,\"n\":13},\"@o\":[1,2]}},\"seq\":11}", data.get(5));
    Assert.assertEquals("{\"data\":{\"t\":{\"3\":{\"id\":3,\"n\":69},\"@o\":[1,2,3]}},\"seq\":13}", data.get(6));
  }

  @Test
  public void blocked_deletes() throws Exception {
    RealDocumentSetup setup = new RealDocumentSetup(
        "@connected { return true; }" + //
            "message N { int n; }" + //
            "channel<N> b;" + //
            "record R { public int id; public int n; }" + //
            "table<R> tbl;" +
            "public formula t = iterate tbl order by id;" + //
            "@web delete /data { tbl <- {n:1}; b.fetch(@who).await(); return {html:\"ok\"}; }", //
        null);
    setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
    ArrayList<String> data = bindList(A, setup);
    Runnable d1 = delete(setup, A, "/data");
    Runnable d2 = delete(setup, A, "/data");
    Runnable d3 = delete(setup, A, "/data");
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(8));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(10));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(12));
    d1.run();
    d2.run();
    d3.run();
    Assert.assertEquals(7, data.size());
    for (String ln : data) {
      System.err.println(ln);
    }
    Assert.assertEquals("{\"seq\":4}", data.get(0));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":5}", data.get(1));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false},{\"id\":2,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":6}", data.get(2));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false},{\"id\":2,\"channel\":\"b\",\"array\":false},{\"id\":3,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":7}", data.get(3));
    Assert.assertEquals("{\"data\":{\"t\":{\"1\":{\"id\":1,\"n\":1},\"@o\":[1]}},\"outstanding\":[],\"blockers\":[],\"seq\":9}", data.get(4));
    Assert.assertEquals("{\"data\":{\"t\":{\"2\":{\"id\":2,\"n\":1},\"@o\":[1,2]}},\"seq\":11}", data.get(5));
    Assert.assertEquals("{\"data\":{\"t\":{\"3\":{\"id\":3,\"n\":1},\"@o\":[1,2,3]}},\"seq\":13}", data.get(6));
  }

  @Test
  public void interweave_puts() throws Exception {
    RealDocumentSetup setup = new RealDocumentSetup(
        "@connected { return true; }" + //
            "message N { int n; }" + //
            "channel<N> b;" + //
            "record R { public int id; public int n; }" + //
            "table<R> tbl;" +
            "public formula t = iterate tbl order by id;" + //
            "@web put /data (N n) { tbl <- n; b.fetch(@who).await(); tbl <- n; b.fetch(@who).await(); return {html:\"ok\"}; }", //
        null);
    setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));

    ArrayList<String> data = bindList(A, setup);
    Runnable p123 = put(setup, A, "/data", "{\"n\":123}");
    Runnable p13 = put(setup, A, "/data", "{\"n\":13}");

    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(7));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(9));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(11));
    setup.document.send(ContextSupport.WRAP(A), null, null, "b", "{\"n\":0}", new RealDocumentSetup.AssertInt(13));

    p123.run();
    p13.run();

    System.err.println(data.size());
    for (String ln : data) {
      System.err.println(ln);
    }

    Assert.assertEquals(7, data.size());
    Assert.assertEquals("{\"seq\":4}", data.get(0));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":5}", data.get(1));
    Assert.assertEquals("{\"outstanding\":[{\"id\":1,\"channel\":\"b\",\"array\":false},{\"id\":2,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":6}", data.get(2));
    Assert.assertEquals("{\"outstanding\":[{\"id\":2,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":7}", data.get(3));
    Assert.assertEquals("{\"data\":{\"t\":{\"1\":{\"id\":1,\"n\":123},\"3\":{\"id\":3,\"n\":123},\"@o\":[1,3]}},\"outstanding\":[],\"blockers\":[],\"seq\":10}", data.get(4));
    Assert.assertEquals("{\"outstanding\":[{\"id\":4,\"channel\":\"b\",\"array\":false}],\"blockers\":[{\"agent\":\"A\",\"authority\":\"TEST\"}],\"seq\":11}", data.get(5));
    Assert.assertEquals("{\"data\":{\"t\":{\"2\":{\"id\":2,\"n\":13},\"4\":{\"id\":4,\"n\":13},\"@o\":[1,2,3,4]}},\"outstanding\":[],\"blockers\":[],\"seq\":14}", data.get(6));
  }

  @Test
  public void state_machine() throws Exception {
    RealDocumentSetup setup = new RealDocumentSetup(
        "@connected { return true; }" + //
            "message N { int n; }" + //
            "channel<N> b;" + //
            "public int x = 100;" +
            "record R { public int id; public int n; }" + //
            "table<R> tbl;" +
            "@construct { transition #sm; }" +
            "public formula t = iterate tbl order by id;" + //
            "#sm { tbl <- {n:x}; x+= b.fetch(@no_one).await().n; tbl <- {n:x}; x+= b.fetch(@no_one).await().n; tbl <- {n:x}; }", //
        null);
    setup.document.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), new RealDocumentSetup.AssertInt(2));
    ArrayList<String> data = bindList(A, setup);

    setup.document.send(ContextSupport.WRAP(NtPrincipal.NO_ONE), null, null, "b", "{\"n\":1000}", new RealDocumentSetup.AssertInt(5));
    setup.document.send(ContextSupport.WRAP(NtPrincipal.NO_ONE), null, null, "b", "{\"n\":10000}", new RealDocumentSetup.AssertInt(7));
    setup.document.send(ContextSupport.WRAP(NtPrincipal.NO_ONE), null, null, "b", "{\"n\":100000}", new RealDocumentSetup.AssertInt(9));

    Assert.assertEquals(4, data.size());
    Assert.assertEquals("{\"data\":{\"x\":100,\"t\":{\"1\":{\"id\":1,\"n\":100},\"@o\":[1]}},\"outstanding\":[],\"blockers\":[{\"agent\":\"?\",\"authority\":\"?\"}],\"seq\":3}", data.get(0));
    Assert.assertEquals("{\"data\":{\"x\":1100,\"t\":{\"2\":{\"id\":2,\"n\":1100},\"@o\":[1,2]}},\"seq\":5}", data.get(1));
    Assert.assertEquals("{\"data\":{\"x\":11100,\"t\":{\"3\":{\"id\":3,\"n\":11100},\"@o\":[1,2,3]}},\"outstanding\":[],\"blockers\":[],\"seq\":8}", data.get(2));
    Assert.assertEquals("{\"seq\":10}", data.get(3));
  }
}
