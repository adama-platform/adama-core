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
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtDynamic;
import ape.runtime.sys.LivingDocument;
import ape.runtime.sys.web.*;
import ape.runtime.sys.web.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WebLivingDocumentTests {

  private static WebGet simple(String uri) {
    return new WebGet(new WebContext(NtPrincipal.NO_ONE, "Origin", "1.2.3.4"), uri, new TreeMap<>(), new NtDynamic("{}"));
  }

  private static WebResponse get_now(LivingDocument doc, WebGet get) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<WebResponse> value = new AtomicReference<>();
    doc.__web_get(get, new Callback<WebResponse>() {
      @Override
      public void success(WebResponse response) {
        value.set(response);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
    return value.get();
  }

  @Test
  public void big_router() throws Exception {
    final var setup = new RealDocumentSetup("@web get / {\n" + "  return {html:\"root\"};\n" + "}\n" + "\n" + "@web get /fixed {\n" + "  return {html:\"fixed path\"};\n" + "}\n" + "\n" + "@web get /path0/$x:int {\n" + "  return {html:\"path integer:\" + x};\n" + "}\n" + "\n" + "@web get /path1/$x:double {\n" + "  return {html:\"path double:\" + x};\n" + "}\n" + "\n" + "@web get /path2/$x:long {\n" + "  return {html:\"path long without child:\" + x};\n" + "}\n" + "\n" + "@web get /path2/$x:long/child {\n" + "  return {html:\"path long with child: \" + x + \"!\"};\n" + "}\n" + "\n" + "@web get /path3/$a* {\n" + "  return {html:\"tail:\" + a};\n" + "}\n" + "\n" + "@web get /path3/$a:string/child {\n" + "  return {html:\"abort tail and go with direct child:\" + a};\n" + "}");
    {
      WebResponse response = get_now(setup.document.document(), simple("/"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("root", response.body);
    }
    {
      WebResponse response = get_now(setup.document.document(), simple("/fixed"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("fixed path", response.body);
    }
    {
      WebResponse response = get_now(setup.document.document(), simple("/path0/42"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("path integer:42", response.body);
    }
    {
      WebResponse response = get_now(setup.document.document(), simple("/path1/3.14"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("path double:3.14", response.body);
    }
    {
      WebResponse response = get_now(setup.document.document(), simple("/path2/342424222"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("path long without child:342424222", response.body);
    }
    {
      WebResponse response = get_now(setup.document.document(), simple("/path2/4242/child"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("path long with child: 4242!", response.body);
    }
    {
      WebResponse response = get_now(setup.document.document(), simple("/path3/yo/yo/yo/yo"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("tail:yo/yo/yo/yo", response.body);
    }
    {
      WebResponse response = get_now(setup.document.document(), simple("/path3/something/child"));
      Assert.assertNotNull(response);
      Assert.assertEquals("text/html; charset=utf-8", response.contentType);
      Assert.assertEquals("abort tail and go with direct child:something", response.body);
    }
  }

  @Test
  public void put() throws Exception {
    final var setup = new RealDocumentSetup("public int x; message M { int x; } @web put / (M m) { x = m.x; return {html:\"Got it! \" + m.x}; }");
    WebPut put = new WebPut(new WebContext(NtPrincipal.NO_ONE, "origin", "ip"), "/", new TreeMap<>(), new NtDynamic("{}"), "{\"x\":42}");
    setup.document.webPut(put, new Callback<WebResponse>() {
      @Override
      public void success(WebResponse value) {

        Assert.assertEquals("Got it! 42", value.body);
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
  }

  @Test
  public void delete() throws Exception {
    final var setup = new RealDocumentSetup("message M { int x; } @web delete / { return {html:\"Deleted\"}; }");
    WebDelete delete = new WebDelete(new WebContext(NtPrincipal.NO_ONE, "origin", "ip"), "/", new TreeMap<>(), new NtDynamic("{}"));
    setup.document.webDelete(delete, new Callback<>() {
      @Override
      public void success(WebResponse value) {
        Assert.assertEquals("Deleted", value.body);
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    });
  }
}
