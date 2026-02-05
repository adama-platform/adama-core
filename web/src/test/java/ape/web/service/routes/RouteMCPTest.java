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
package ape.web.service.routes;

import ape.common.Json;
import ape.web.contracts.GenericWebSocketRouteSession;
import ape.web.contracts.MCPSession;
import ape.web.io.ConnectionContext;
import ape.web.service.mocks.MockMCPHandler;
import ape.web.service.mocks.MockServiceBaseWithMCP;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class RouteMCPTest {

  private ConnectionContext testContext() {
    return new ConnectionContext("test-origin", "127.0.0.1", "test-agent", null);
  }

  private ChannelHandlerContext getContext() {
    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    return channel.pipeline().context(ChannelInboundHandlerAdapter.class);
  }

  @Test
  public void testProtocolVersion() {
    Assert.assertEquals("2025-11-25", RouteMCP.PROTOCOL_VERSION);
  }

  @Test
  public void testErrorCodes() {
    Assert.assertEquals(-32700, RouteMCP.ERROR_PARSE);
    Assert.assertEquals(-32600, RouteMCP.ERROR_INVALID_REQUEST);
    Assert.assertEquals(-32601, RouteMCP.ERROR_METHOD_NOT_FOUND);
    Assert.assertEquals(-32602, RouteMCP.ERROR_INVALID_PARAMS);
    Assert.assertEquals(-32603, RouteMCP.ERROR_INTERNAL);
    Assert.assertEquals(-32002, RouteMCP.ERROR_RESOURCE_NOT_FOUND);
  }

  @Test
  public void testEstablishSession() {
    MockMCPHandler handler = new MockMCPHandler("test-server", "1.0.0");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());
    Assert.assertNotNull(session);

    // MCP doesn't use keepalive
    Assert.assertFalse(session.enableKeepAlive());
    Assert.assertTrue(session.keepalive());

    // Verify ping/pong do nothing
    ChannelHandlerContext ctx = getContext();
    Assert.assertFalse(session.sendPing(ctx));
    Assert.assertFalse(session.sendKeepAliveDisconnect(ctx));
  }

  @Test
  public void testInitializeRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler("test-server", "2.0.0");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "initialize");
    ObjectNode params = request.putObject("params");
    params.put("protocolVersion", "2025-11-25");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    Assert.assertNotNull(response);

    ObjectNode responseNode = Json.parseJsonObject(response.text());
    Assert.assertEquals("2.0", responseNode.get("jsonrpc").asText());
    Assert.assertEquals(1, responseNode.get("id").asInt());
    Assert.assertTrue(responseNode.has("result"));

    ObjectNode result = (ObjectNode) responseNode.get("result");
    Assert.assertEquals("2025-11-25", result.get("protocolVersion").asText());
    Assert.assertEquals("test-server", result.get("serverInfo").get("name").asText());
    Assert.assertEquals("2.0.0", result.get("serverInfo").get("version").asText());

    // Verify capabilities
    ObjectNode capabilities = (ObjectNode) result.get("capabilities");
    Assert.assertTrue(capabilities.has("tools"));
    Assert.assertTrue(capabilities.has("resources"));
    Assert.assertTrue(capabilities.has("prompts"));
  }

  @Test
  public void testToolsListRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimpleTool("tool1", "Tool 1", "result1")
        .addSimpleTool("tool2", "Tool 2", "result2");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "tools/list");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
    Assert.assertEquals(2, responseNode.get("result").get("tools").size());
  }

  @Test
  public void testToolsCallRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimpleTool("greet", "Greets", "Hello!");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "tools/call");
    ObjectNode params = request.putObject("params");
    params.put("name", "greet");
    params.putObject("arguments");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
    Assert.assertEquals("Hello!", responseNode.get("result").get("content").get(0).get("text").asText());
  }

  @Test
  public void testToolsCallMissingName() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "tools/call");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("error"));
    Assert.assertEquals(-32602, responseNode.get("error").get("code").asInt());
  }

  @Test
  public void testResourcesListRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimpleResource("file:///a.txt", "a.txt", "File A", "content");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "resources/list");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
    Assert.assertEquals(1, responseNode.get("result").get("resources").size());
    Assert.assertEquals("file:///a.txt", responseNode.get("result").get("resources").get(0).get("uri").asText());
  }

  @Test
  public void testResourcesReadRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimpleResource("file:///a.txt", "a.txt", "File A", "Hello content");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "resources/read");
    ObjectNode params = request.putObject("params");
    params.put("uri", "file:///a.txt");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
    Assert.assertEquals("Hello content", responseNode.get("result").get("contents").get(0).get("text").asText());
  }

  @Test
  public void testResourcesReadMissingUri() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "resources/read");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("error"));
    Assert.assertEquals(-32602, responseNode.get("error").get("code").asInt());
  }

  @Test
  public void testPromptsListRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimplePrompt("greet", "Greeting", "Hello!");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "prompts/list");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
    Assert.assertEquals(1, responseNode.get("result").get("prompts").size());
    Assert.assertEquals("greet", responseNode.get("result").get("prompts").get(0).get("name").asText());
  }

  @Test
  public void testPromptsGetRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimplePrompt("greet", "Greeting", "Hello there!");
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "prompts/get");
    ObjectNode params = request.putObject("params");
    params.put("name", "greet");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
    Assert.assertTrue(responseNode.get("result").has("messages"));
  }

  @Test
  public void testPromptsGetMissingName() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "prompts/get");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("error"));
    Assert.assertEquals(-32602, responseNode.get("error").get("code").asInt());
  }

  @Test
  public void testPingRequest() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "ping");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
    Assert.assertEquals(1, responseNode.get("id").asInt());
  }

  @Test
  public void testMethodNotFound() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "unknown/method");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("error"));
    Assert.assertEquals(-32601, responseNode.get("error").get("code").asInt());
    Assert.assertTrue(responseNode.get("error").get("message").asText().contains("unknown/method"));
  }

  @Test
  public void testNotificationIgnored() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    // Notification without id
    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("method", "notifications/initialized");

    session.handle(request, channel.pipeline().firstContext());

    // No response should be sent for notifications
    TextWebSocketFrame response = channel.readOutbound();
    Assert.assertNull(response);
  }

  @Test
  public void testMissingMethodReturnsError() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    // No method field

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("error"));
    Assert.assertEquals(-32600, responseNode.get("error").get("code").asInt());
  }

  @Test
  public void testSessionKill() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    Assert.assertEquals(0, handler.getCloseCount());
    session.kill();
    Assert.assertEquals(1, handler.getCloseCount());
  }

  @Test
  public void testWithNoOpSession() throws Exception {
    // Use MCPSession.NOOP directly via a lambda
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(ctx -> MCPSession.NOOP);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    // Test tools/list returns empty
    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "tools/list");
    request.putObject("params");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());
    Assert.assertEquals(0, responseNode.get("result").get("tools").size());
  }

  @Test
  public void testParamsOptional() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MockServiceBaseWithMCP base = new MockServiceBaseWithMCP(handler);
    RouteMCP route = new RouteMCP(base);

    EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
    GenericWebSocketRouteSession session = route.establish("/", new AtomicLong(), testContext());

    // Request without params field
    ObjectNode request = Json.newJsonObject();
    request.put("jsonrpc", "2.0");
    request.put("id", 1);
    request.put("method", "tools/list");

    session.handle(request, channel.pipeline().firstContext());

    TextWebSocketFrame response = channel.readOutbound();
    ObjectNode responseNode = Json.parseJsonObject(response.text());

    Assert.assertTrue(responseNode.has("result"));
  }
}
