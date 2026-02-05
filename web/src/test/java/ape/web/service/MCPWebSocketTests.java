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

import ape.common.Json;
import ape.common.metrics.NoOpMetricsFactory;
import ape.web.client.TestClientCallback;
import ape.web.client.TestClientRequestBuilder;
import ape.web.contracts.MCPSession;
import ape.web.service.mocks.MockDomainFinder;
import ape.web.service.mocks.MockMCPHandler;
import ape.web.service.mocks.MockServiceBaseWithMCP;
import ape.web.service.mocks.NullCertificateFinder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.Assert;
import org.junit.Test;

public class MCPWebSocketTests {

  @Test
  public void testMCPInitialize() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockMCPHandler mcpHandler = new MockMCPHandler("test-server", "1.0.0");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(mcpHandler);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);

      // Send initialize request (MCP doesn't send a "connected" status)
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-11-25\",\"clientInfo\":{\"name\":\"test-client\",\"version\":\"1.0.0\"}}}"
      ));

      callback.awaitFirst();

      // Verify we got a response with server info
      String data = callback.writes.get(0);
      ObjectNode response = Json.parseJsonObject(data);
      Assert.assertEquals("2.0", response.get("jsonrpc").asText());
      Assert.assertEquals(1, response.get("id").asInt());
      Assert.assertTrue(response.has("result"));

      ObjectNode result = (ObjectNode) response.get("result");
      Assert.assertEquals("2025-11-25", result.get("protocolVersion").asText());
      Assert.assertTrue(result.has("capabilities"));
      Assert.assertTrue(result.has("serverInfo"));

      ObjectNode serverInfo = (ObjectNode) result.get("serverInfo");
      Assert.assertEquals("test-server", serverInfo.get("name").asText());
      Assert.assertEquals("1.0.0", serverInfo.get("version").asText());
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void testMCPToolsList() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockMCPHandler mcpHandler = new MockMCPHandler()
        .addSimpleTool("hello", "Says hello", "Hello, world!")
        .addEchoTool("echo", "Echoes input");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(mcpHandler);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);

      // Send tools/list request
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}"
      ));

      callback.awaitFirst();

      String data = callback.writes.get(0);
      ObjectNode response = Json.parseJsonObject(data);
      Assert.assertEquals(1, response.get("id").asInt());
      Assert.assertTrue(response.has("result"));

      ObjectNode result = (ObjectNode) response.get("result");
      Assert.assertTrue(result.has("tools"));
      Assert.assertEquals(2, result.get("tools").size());

      JsonNode tools = result.get("tools");
      Assert.assertEquals("hello", tools.get(0).get("name").asText());
      Assert.assertEquals("echo", tools.get(1).get("name").asText());
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void testMCPToolsCall() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockMCPHandler mcpHandler = new MockMCPHandler()
        .addSimpleTool("hello", "Says hello", "Hello, world!");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(mcpHandler);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);
      b.awaitActivation();

      // Send tools/call request
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"hello\",\"arguments\":{}}}"
      ));

      callback.awaitFirst();

      String data = callback.writes.get(0);
      ObjectNode response = Json.parseJsonObject(data);
      Assert.assertEquals(1, response.get("id").asInt());
      Assert.assertTrue(response.has("result"));

      ObjectNode result = (ObjectNode) response.get("result");
      Assert.assertTrue(result.has("content"));
      Assert.assertEquals(1, result.get("content").size());
      Assert.assertEquals("Hello, world!", result.get("content").get(0).get("text").asText());
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void testMCPToolsCallError() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockMCPHandler mcpHandler = new MockMCPHandler()
        .addFailingTool("fail", "Always fails", -32603, "Internal error");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(mcpHandler);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);

      b.awaitActivation();

      // Send tools/call request for failing tool
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"fail\",\"arguments\":{}}}"
      ));

      callback.awaitFirst();

      String data = callback.writes.get(0);
      ObjectNode response = Json.parseJsonObject(data);
      Assert.assertEquals(1, response.get("id").asInt());
      Assert.assertTrue(response.has("error"));

      ObjectNode error = (ObjectNode) response.get("error");
      Assert.assertEquals(-32603, error.get("code").asInt());
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void testMCPPing() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockMCPHandler mcpHandler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(mcpHandler);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);

      // Send ping request
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\",\"params\":{}}"
      ));

      callback.awaitFirst();

      String data = callback.writes.get(0);
      ObjectNode response = Json.parseJsonObject(data);
      Assert.assertEquals(1, response.get("id").asInt());
      Assert.assertTrue(response.has("result"));
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void testMCPMethodNotFound() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockMCPHandler mcpHandler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(mcpHandler);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);

      // Send unknown method
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"unknown/method\",\"params\":{}}"
      ));

      callback.awaitFirst();

      String data = callback.writes.get(0);
      ObjectNode response = Json.parseJsonObject(data);
      Assert.assertEquals(1, response.get("id").asInt());
      Assert.assertTrue(response.has("error"));

      ObjectNode error = (ObjectNode) response.get("error");
      Assert.assertEquals(-32601, error.get("code").asInt());
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void testNoOpMCPSession() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    // Use NOOP session
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(ctx -> MCPSession.NOOP);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);

      // Test initialize
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}"
      ));
      callback.awaitFirst();
      ObjectNode initResponse = Json.parseJsonObject(callback.writes.get(0));
      Assert.assertTrue(initResponse.has("result"));
      Assert.assertEquals("noop", initResponse.get("result").get("serverInfo").get("name").asText());

      // Test tools/list returns empty
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}"
      ));
      Thread.sleep(200);
      ObjectNode toolsResponse = Json.parseJsonObject(callback.writes.get(1));
      Assert.assertEquals(0, toolsResponse.get("result").get("tools").size());

      // Test tools/call returns error
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"any\"}}"
      ));
      Thread.sleep(200);
      ObjectNode callResponse = Json.parseJsonObject(callback.writes.get(2));
      Assert.assertTrue(callResponse.has("error"));
      Assert.assertEquals(-32601, callResponse.get("error").get("code").asInt());
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }

  @Test
  public void testMCPSessionClose() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.DevScope);
    MockMCPHandler mcpHandler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(mcpHandler);
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      TestClientCallback callback = new TestClientCallback();
      TestClientRequestBuilder b = TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s/mcp")
          .withWebSocket();
      b.execute(callback);

      // Send a ping to verify session was established
      b.channel().writeAndFlush(new TextWebSocketFrame(
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\",\"params\":{}}"
      ));
      callback.awaitFirst();

      Assert.assertEquals(1, mcpHandler.getSessionCount());
      Assert.assertEquals(0, mcpHandler.getCloseCount());

      // Close the channel
      b.channel().close();
      Thread.sleep(500);

      // Verify session was closed
      Assert.assertEquals(1, mcpHandler.getCloseCount());
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }
}
