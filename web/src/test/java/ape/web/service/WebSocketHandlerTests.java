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

import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import ape.common.Platform;
import ape.common.metrics.NoOpMetricsFactory;
import ape.web.client.TestClientCallback;
import ape.web.client.TestClientRequestBuilder;
import ape.web.service.mocks.MockDomainFinder;
import ape.web.service.mocks.MockServiceBase;
import ape.web.service.mocks.NullCertificateFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebSocketHandlerTests {
  @Test
  public void flow() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      runnable.waitForReady(1000);
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/~s")
            .header("X-Forwarded-For", "4.3.2.1")
            .withWebSocket()
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .get("/~s")
            .header("Cookie", ClientCookieEncoder.STRICT.encode(new DefaultCookie("sak", "123")))
            .withWebSocket()
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");

        TestClientCallback.Mailbox box = callback.getOrCreate(500);
        CountDownLatch latch = box.latch(2);
        b.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":500,\"method\":\"cake\"}"));
        Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        box.assertData(0, "{\"deliver\":500,\"done\":false,\"response\":{\"boss\":1}}");
        box.assertData(1, "{\"deliver\":500,\"done\":true,\"response\":{\"boss\":2}}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");

        TestClientCallback.Mailbox box = callback.getOrCreate(500);
        CountDownLatch latch = box.latch(2);
        b.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":500,\"method\":\"cake2\"}"));
        Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        box.assertData(0, "{\"deliver\":500,\"done\":false,\"response\":{\"boss\":1}}");
        box.assertData(1, "{\"deliver\":500,\"done\":true}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");

        TestClientCallback.Mailbox box = callback.getOrCreate(500);
        CountDownLatch latch = box.latch(1);
        b.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":500,\"method\":\"ex\"}"));
        Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        box.assertData(0, "{\"failure\":500,\"reason\":1234,\"retry\":false}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");

        TestClientCallback.Mailbox box = callback.getOrCreate(500);
        CountDownLatch latch = box.latch(1);
        b.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":500,\"method\":\"kill\"}"));
        Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        box.assertData(0, "{\"deliver\":500,\"done\":false,\"response\":{\"death\":1}}");
        callback.awaitDisconnect();
        callback.assertData(
            "{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}{\"deliver\":500,\"done\":false,\"response\":{\"death\":1}}{\"status\":\"disconnected\",\"reason\":\"keepalive-failure\"}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");
        b.channel().writeAndFlush(new TextWebSocketFrame("{}"));
        callback.awaitDisconnect();
        callback.assertData(
            "{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}{\"status\":\"disconnected\",\"reason\":233120}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");
        b.channel().writeAndFlush(new TextWebSocketFrame("{\"pong\":42}"));
        callback.awaitDisconnect();
        callback.assertData(
            "{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}{\"status\":\"disconnected\",\"reason\":295116}");
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");
        b.channel().writeAndFlush(new TextWebSocketFrame("{\"pong\":42,\"ping\":80}"));
        b.channel().writeAndFlush(new TextWebSocketFrame("{\"id\":500,\"method\":\"kill\"}"));
        callback.awaitDisconnect();
      }

      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder b =
            TestClientRequestBuilder.start(group)
                .server("localhost", webConfig.port)
                .get("/~s")
                .withWebSocket();
        b.execute(callback);
        callback.awaitFirst();
        callback.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");
        callback.awaitPing();
        callback.assertDataPrefix(1, "{\"ping\":");
        b.channel()
            .writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(new byte[] {0x42})));
        callback.awaitDisconnect();
        callback.assertData(
            "{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}{\"status\":\"disconnected\",\"reason\":213711}");
      }
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }
}
