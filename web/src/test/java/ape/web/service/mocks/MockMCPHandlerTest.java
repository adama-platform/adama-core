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
package ape.web.service.mocks;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.web.contracts.MCPSession;
import ape.web.contracts.MCPSession.*;
import ape.web.io.ConnectionContext;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MockMCPHandlerTest {

  private ConnectionContext testContext() {
    return new ConnectionContext("test-origin", "127.0.0.1", "test-agent", null);
  }

  @Test
  public void testServerInfo() {
    MockMCPHandler handler = new MockMCPHandler("test-server", "2.0.0");
    MCPSession session = handler.establish(testContext());
    ServerInfo info = session.getServerInfo();
    Assert.assertEquals("test-server", info.name);
    Assert.assertEquals("2.0.0", info.version);
  }

  @Test
  public void testDefaultServerInfo() {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());
    ServerInfo info = session.getServerInfo();
    Assert.assertEquals("mock-mcp-server", info.name);
    Assert.assertEquals("1.0.0-test", info.version);
  }

  @Test
  public void testEstablishSession() {
    MockMCPHandler handler = new MockMCPHandler();
    Assert.assertEquals(0, handler.getSessionCount());

    MCPSession session = handler.establish(testContext());
    Assert.assertNotNull(session);
    Assert.assertEquals(1, handler.getSessionCount());

    MCPSession session2 = handler.establish(testContext());
    Assert.assertEquals(2, handler.getSessionCount());

    List<String> events = handler.getEvents();
    Assert.assertEquals(2, events.size());
    Assert.assertTrue(events.get(0).startsWith("ESTABLISH:1:"));
    Assert.assertTrue(events.get(1).startsWith("ESTABLISH:2:"));
  }

  @Test
  public void testSessionClose() {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());
    Assert.assertEquals(0, handler.getCloseCount());

    session.close();
    Assert.assertEquals(1, handler.getCloseCount());

    List<String> events = handler.getEvents();
    Assert.assertTrue(events.contains("CLOSE:1"));
  }

  @Test
  public void testListToolsEmpty() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ToolDefinition[]> result = new AtomicReference<>();

    session.listTools(new Callback<ToolDefinition[]>() {
      @Override
      public void success(ToolDefinition[] value) {
        result.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(0, result.get().length);
    Assert.assertTrue(handler.getEvents().contains("LIST_TOOLS:1"));
  }

  @Test
  public void testAddSimpleTool() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimpleTool("hello", "Says hello", "Hello, world!");

    MCPSession session = handler.establish(testContext());

    // Test list tools
    CountDownLatch listLatch = new CountDownLatch(1);
    AtomicReference<ToolDefinition[]> tools = new AtomicReference<>();

    session.listTools(new Callback<ToolDefinition[]>() {
      @Override
      public void success(ToolDefinition[] value) {
        tools.set(value);
        listLatch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        listLatch.countDown();
      }
    });

    Assert.assertTrue(listLatch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(1, tools.get().length);
    Assert.assertEquals("hello", tools.get()[0].name);
    Assert.assertEquals("Says hello", tools.get()[0].description);

    // Test call tool
    CountDownLatch callLatch = new CountDownLatch(1);
    AtomicReference<ToolResult> result = new AtomicReference<>();

    session.callTool("hello", Json.newJsonObject(), new Callback<ToolResult>() {
      @Override
      public void success(ToolResult value) {
        result.set(value);
        callLatch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        callLatch.countDown();
      }
    });

    Assert.assertTrue(callLatch.await(1, TimeUnit.SECONDS));
    Assert.assertFalse(result.get().isError);
    Assert.assertEquals(1, result.get().content.size());
    Assert.assertEquals("Hello, world!", result.get().content.get(0).get("text").asText());
  }

  @Test
  public void testAddEchoTool() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addEchoTool("echo", "Echoes input");

    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ToolResult> result = new AtomicReference<>();

    ObjectNode args = Json.newJsonObject();
    args.put("message", "test input");

    session.callTool("echo", args, new Callback<ToolResult>() {
      @Override
      public void success(ToolResult value) {
        result.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertFalse(result.get().isError);
    String text = result.get().content.get(0).get("text").asText();
    Assert.assertTrue(text.contains("message"));
    Assert.assertTrue(text.contains("test input"));
  }

  @Test
  public void testAddFailingTool() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addFailingTool("fail", "Always fails", 500, "Tool error");

    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.callTool("fail", Json.newJsonObject(), new Callback<ToolResult>() {
      @Override
      public void success(ToolResult value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(500, error.get().code);
  }

  @Test
  public void testCallNonExistentTool() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.callTool("nonexistent", Json.newJsonObject(), new Callback<ToolResult>() {
      @Override
      public void success(ToolResult value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(-32601, error.get().code);
  }

  @Test
  public void testListResourcesEmpty() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ResourceDefinition[]> result = new AtomicReference<>();

    session.listResources(new Callback<ResourceDefinition[]>() {
      @Override
      public void success(ResourceDefinition[] value) {
        result.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(0, result.get().length);
  }

  @Test
  public void testAddSimpleResource() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimpleResource("file:///test.txt", "test.txt", "A test file", "Hello from resource");

    MCPSession session = handler.establish(testContext());

    // Test list resources
    CountDownLatch listLatch = new CountDownLatch(1);
    AtomicReference<ResourceDefinition[]> resources = new AtomicReference<>();

    session.listResources(new Callback<ResourceDefinition[]>() {
      @Override
      public void success(ResourceDefinition[] value) {
        resources.set(value);
        listLatch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        listLatch.countDown();
      }
    });

    Assert.assertTrue(listLatch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(1, resources.get().length);
    Assert.assertEquals("file:///test.txt", resources.get()[0].uri);
    Assert.assertEquals("test.txt", resources.get()[0].name);

    // Test read resource
    CountDownLatch readLatch = new CountDownLatch(1);
    AtomicReference<ResourceContent[]> contents = new AtomicReference<>();

    session.readResource("file:///test.txt", new Callback<ResourceContent[]>() {
      @Override
      public void success(ResourceContent[] value) {
        contents.set(value);
        readLatch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        readLatch.countDown();
      }
    });

    Assert.assertTrue(readLatch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(1, contents.get().length);
    Assert.assertEquals("Hello from resource", contents.get()[0].text);
  }

  @Test
  public void testAddFailingResource() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addFailingResource("file:///fail.txt", "fail.txt", "Fails", 404, "Not found");

    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.readResource("file:///fail.txt", new Callback<ResourceContent[]>() {
      @Override
      public void success(ResourceContent[] value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(404, error.get().code);
  }

  @Test
  public void testReadNonExistentResource() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.readResource("file:///nonexistent.txt", new Callback<ResourceContent[]>() {
      @Override
      public void success(ResourceContent[] value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(-32002, error.get().code);
  }

  @Test
  public void testListPromptsEmpty() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PromptDefinition[]> result = new AtomicReference<>();

    session.listPrompts(new Callback<PromptDefinition[]>() {
      @Override
      public void success(PromptDefinition[] value) {
        result.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(0, result.get().length);
  }

  @Test
  public void testAddSimplePrompt() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimplePrompt("greet", "A greeting prompt", "Hello! How can I help you?");

    MCPSession session = handler.establish(testContext());

    // Test list prompts
    CountDownLatch listLatch = new CountDownLatch(1);
    AtomicReference<PromptDefinition[]> prompts = new AtomicReference<>();

    session.listPrompts(new Callback<PromptDefinition[]>() {
      @Override
      public void success(PromptDefinition[] value) {
        prompts.set(value);
        listLatch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        listLatch.countDown();
      }
    });

    Assert.assertTrue(listLatch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(1, prompts.get().length);
    Assert.assertEquals("greet", prompts.get()[0].name);

    // Test get prompt
    CountDownLatch getLatch = new CountDownLatch(1);
    AtomicReference<PromptMessages> messages = new AtomicReference<>();

    session.getPrompt("greet", Json.newJsonObject(), new Callback<PromptMessages>() {
      @Override
      public void success(PromptMessages value) {
        messages.set(value);
        getLatch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        getLatch.countDown();
      }
    });

    Assert.assertTrue(getLatch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(1, messages.get().messages.size());
  }

  @Test
  public void testAddFailingPrompt() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addFailingPrompt("fail", "Fails", 500, "Prompt error");

    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.getPrompt("fail", Json.newJsonObject(), new Callback<PromptMessages>() {
      @Override
      public void success(PromptMessages value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(500, error.get().code);
  }

  @Test
  public void testGetNonExistentPrompt() throws Exception {
    MockMCPHandler handler = new MockMCPHandler();
    MCPSession session = handler.establish(testContext());

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.getPrompt("nonexistent", Json.newJsonObject(), new Callback<PromptMessages>() {
      @Override
      public void success(PromptMessages value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertEquals(-32601, error.get().code);
  }

  @Test
  public void testClearEvents() {
    MockMCPHandler handler = new MockMCPHandler();
    handler.establish(testContext());
    Assert.assertEquals(1, handler.getEvents().size());

    handler.clearEvents();
    Assert.assertEquals(0, handler.getEvents().size());
  }

  @Test
  public void testEventTracking() throws Exception {
    MockMCPHandler handler = new MockMCPHandler()
        .addSimpleTool("tool1", "Tool 1", "result1")
        .addSimpleResource("file:///res.txt", "res.txt", "Resource", "content")
        .addSimplePrompt("prompt1", "Prompt 1", "message");

    MCPSession session = handler.establish(testContext());

    // Perform various operations
    CountDownLatch latch = new CountDownLatch(6);

    session.listTools(new Callback<ToolDefinition[]>() {
      @Override
      public void success(ToolDefinition[] value) { latch.countDown(); }
      @Override
      public void failure(ErrorCodeException ex) { latch.countDown(); }
    });

    session.callTool("tool1", Json.newJsonObject(), new Callback<ToolResult>() {
      @Override
      public void success(ToolResult value) { latch.countDown(); }
      @Override
      public void failure(ErrorCodeException ex) { latch.countDown(); }
    });

    session.listResources(new Callback<ResourceDefinition[]>() {
      @Override
      public void success(ResourceDefinition[] value) { latch.countDown(); }
      @Override
      public void failure(ErrorCodeException ex) { latch.countDown(); }
    });

    session.readResource("file:///res.txt", new Callback<ResourceContent[]>() {
      @Override
      public void success(ResourceContent[] value) { latch.countDown(); }
      @Override
      public void failure(ErrorCodeException ex) { latch.countDown(); }
    });

    session.listPrompts(new Callback<PromptDefinition[]>() {
      @Override
      public void success(PromptDefinition[] value) { latch.countDown(); }
      @Override
      public void failure(ErrorCodeException ex) { latch.countDown(); }
    });

    session.getPrompt("prompt1", Json.newJsonObject(), new Callback<PromptMessages>() {
      @Override
      public void success(PromptMessages value) { latch.countDown(); }
      @Override
      public void failure(ErrorCodeException ex) { latch.countDown(); }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));

    session.close();

    List<String> events = handler.getEvents();
    Assert.assertEquals(8, events.size()); // 1 establish + 6 operations + 1 close
    Assert.assertTrue(events.contains("LIST_TOOLS:1"));
    Assert.assertTrue(events.stream().anyMatch(e -> e.startsWith("CALL_TOOL:1:tool1:")));
    Assert.assertTrue(events.contains("LIST_RESOURCES:1"));
    Assert.assertTrue(events.contains("READ_RESOURCE:1:file:///res.txt"));
    Assert.assertTrue(events.contains("LIST_PROMPTS:1"));
    Assert.assertTrue(events.stream().anyMatch(e -> e.startsWith("GET_PROMPT:1:prompt1:")));
    Assert.assertTrue(events.contains("CLOSE:1"));
  }
}
