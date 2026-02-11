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
package ape.web.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;
import ape.common.Json;
import ape.common.metrics.NoOpMetricsFactory;
import ape.web.client.TestClientCallback;
import ape.web.client.TestClientRequestBuilder;
import ape.web.service.mocks.MockDomainFinder;
import ape.web.service.mocks.MockServiceBase;
import ape.web.service.mocks.NullCertificateFinder;
import org.junit.Assert;
import org.junit.Test;

public class WebHandlerTests {
  @Test
  public void flow() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.ProdScope);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
          .server("localhost", 52000)
          .get("/x")
          .execute(callback);
        callback.awaitFailedToConnect();
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/x")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("<html><head><title>Bad Request; Not Found</title></head><body>Sorry, the request was not found within our handler space.</body></html>");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/301")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
        Assert.assertEquals("/loc1", callback.headers.get("location"));
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/302")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
        Assert.assertEquals("/loc2", callback.headers.get("location"));
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .put("/~stash/fooyo", "{\"name\":\"def\",\"identity\":\"id\",\"max-age\":100000}")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("<html><head><title>Bad Request; Failed to set cookie</title></head><body>Sorry, the request was incomplete.</body></html>");
      }

      {
        ObjectNode request = Json.newJsonObject();
        request.put("method", "document/authorization");
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .put("/~adama/once", request.toString())
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"auth\":true}");
      }

      { // validate identity token from Authorization header
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer TOKEN")
            .get("/inject")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("TOKEN:{}");
      }

      {
        ObjectNode request = Json.newJsonObject();
        request.put("method", "document/authorization");
        request.put("failed", true);
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .put("/~adama/once", request.toString())
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("111");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("origin", "https://Oooooo.com")
            .put("/~stash/fooyo", "{\"name\":\"def\",\"identity\":\"id\",\"max-age\":100000}")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("OK");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/crash")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("error:-1");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        callback.keepPings = true; // Hack since the libadama.js HAS "ping" in it
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/libadama.js")
            .execute(callback);
        callback.awaitFirst();
        callback.assertDataPrefix("function AdamaTree(");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .post("/crash", "{}")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("error:-1");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .post("/body", "{\"x\":{}}")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("body:{\"x\":{}}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .options("/nope")
            .header("Origin", "my-origin")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
        System.err.println("HERE");
        System.err.println(callback.headers);
        Assert.assertNull(callback.headers.get("access-control-allow-origin"));
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .options("/ok-cors")
            .header("Origin", "my-origin")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
        Assert.assertEquals("my-origin", callback.headers.get("access-control-allow-origin"));
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/foo")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("goo");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get(webConfig.healthCheckPath)
            .execute(callback);
        callback.awaitFirst();
        callback.assertDataPrefix("HEALTHY:");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("origin", "http://localhost")
            .junk()
            .get("/demo.html")
            .execute(callback);
        callback.awaitFailure();
      }

    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void svgSecurityHeaders() {
    DefaultHttpResponse svgResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    svgResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/svg+xml");
    WebHandler.addSecurityHeaders(svgResponse);
    Assert.assertEquals("script-src 'none'; frame-ancestors 'self'", svgResponse.headers().get("Content-Security-Policy"));

    DefaultHttpResponse htmlResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    htmlResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
    WebHandler.addSecurityHeaders(htmlResponse);
    Assert.assertEquals("frame-ancestors 'self'", htmlResponse.headers().get("Content-Security-Policy"));

    DefaultHttpResponse noTypeResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    WebHandler.addSecurityHeaders(noTypeResponse);
    Assert.assertEquals("frame-ancestors 'self'", noTypeResponse.headers().get("Content-Security-Policy"));
  }
}
