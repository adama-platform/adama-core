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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Mock MCP session factory for testing MCP protocol implementation.
 * Provides configurable tools, resources, and prompts for test scenarios.
 * Tracks all method invocations for assertion verification.
 */
public class MockMCPHandler {

  private final ServerInfo serverInfo;
  private final List<ToolDefinition> tools;
  private final Map<String, BiConsumer<ObjectNode, Callback<ToolResult>>> toolHandlers;
  private final List<ResourceDefinition> resources;
  private final Map<String, Consumer<Callback<ResourceContent[]>>> resourceHandlers;
  private final List<PromptDefinition> prompts;
  private final Map<String, BiConsumer<ObjectNode, Callback<PromptMessages>>> promptHandlers;
  private final List<String> events;
  private final AtomicInteger sessionCount;
  private final AtomicInteger closeCount;

  public MockMCPHandler() {
    this("mock-mcp-server", "1.0.0-test");
  }

  public MockMCPHandler(String name, String version) {
    this.serverInfo = new ServerInfo(name, version);
    this.tools = new ArrayList<>();
    this.toolHandlers = new HashMap<>();
    this.resources = new ArrayList<>();
    this.resourceHandlers = new HashMap<>();
    this.prompts = new ArrayList<>();
    this.promptHandlers = new HashMap<>();
    this.events = new ArrayList<>();
    this.sessionCount = new AtomicInteger(0);
    this.closeCount = new AtomicInteger(0);
  }

  /**
   * Add a tool definition with a handler.
   */
  public MockMCPHandler addTool(String name, String description, ObjectNode inputSchema,
                                 BiConsumer<ObjectNode, Callback<ToolResult>> handler) {
    tools.add(new ToolDefinition(name, description, inputSchema));
    toolHandlers.put(name, handler);
    return this;
  }

  /**
   * Add a simple tool that returns a text result.
   */
  public MockMCPHandler addSimpleTool(String name, String description, String resultText) {
    ObjectNode schema = Json.newJsonObject();
    schema.put("type", "object");
    return addTool(name, description, schema, (args, callback) -> {
      ArrayNode content = Json.newJsonArray();
      ObjectNode textContent = content.addObject();
      textContent.put("type", "text");
      textContent.put("text", resultText);
      callback.success(new ToolResult(content, false));
    });
  }

  /**
   * Add a tool that echoes its arguments.
   */
  public MockMCPHandler addEchoTool(String name, String description) {
    ObjectNode schema = Json.newJsonObject();
    schema.put("type", "object");
    return addTool(name, description, schema, (args, callback) -> {
      ArrayNode content = Json.newJsonArray();
      ObjectNode textContent = content.addObject();
      textContent.put("type", "text");
      textContent.put("text", args.toString());
      callback.success(new ToolResult(content, false));
    });
  }

  /**
   * Add a tool that always fails.
   */
  public MockMCPHandler addFailingTool(String name, String description, int errorCode, String errorMessage) {
    ObjectNode schema = Json.newJsonObject();
    schema.put("type", "object");
    return addTool(name, description, schema, (args, callback) -> {
      callback.failure(new ErrorCodeException(errorCode, errorMessage));
    });
  }

  /**
   * Add a resource definition with a handler.
   */
  public MockMCPHandler addResource(String uri, String name, String description, String mimeType,
                                     Consumer<Callback<ResourceContent[]>> handler) {
    resources.add(new ResourceDefinition(uri, name, description, mimeType));
    resourceHandlers.put(uri, handler);
    return this;
  }

  /**
   * Add a simple text resource.
   */
  public MockMCPHandler addSimpleResource(String uri, String name, String description, String text) {
    return addResource(uri, name, description, "text/plain", callback -> {
      callback.success(new ResourceContent[]{new ResourceContent(uri, "text/plain", text)});
    });
  }

  /**
   * Add a resource that always fails.
   */
  public MockMCPHandler addFailingResource(String uri, String name, String description, int errorCode, String errorMessage) {
    return addResource(uri, name, description, "text/plain", callback -> {
      callback.failure(new ErrorCodeException(errorCode, errorMessage));
    });
  }

  /**
   * Add a prompt definition with a handler.
   */
  public MockMCPHandler addPrompt(String name, String description, ArrayNode arguments,
                                   BiConsumer<ObjectNode, Callback<PromptMessages>> handler) {
    prompts.add(new PromptDefinition(name, description, arguments));
    promptHandlers.put(name, handler);
    return this;
  }

  /**
   * Add a simple prompt that returns a single message.
   */
  public MockMCPHandler addSimplePrompt(String name, String description, String messageContent) {
    return addPrompt(name, description, null, (args, callback) -> {
      ArrayNode messages = Json.newJsonArray();
      ObjectNode message = messages.addObject();
      message.put("role", "user");
      ObjectNode content = message.putObject("content");
      content.put("type", "text");
      content.put("text", messageContent);
      callback.success(new PromptMessages(description, messages));
    });
  }

  /**
   * Add a prompt that always fails.
   */
  public MockMCPHandler addFailingPrompt(String name, String description, int errorCode, String errorMessage) {
    return addPrompt(name, description, null, (args, callback) -> {
      callback.failure(new ErrorCodeException(errorCode, errorMessage));
    });
  }

  /**
   * Get all recorded events for test assertions.
   */
  public List<String> getEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Get the number of sessions established.
   */
  public int getSessionCount() {
    return sessionCount.get();
  }

  /**
   * Get the number of sessions closed.
   */
  public int getCloseCount() {
    return closeCount.get();
  }

  /**
   * Clear all recorded events.
   */
  public void clearEvents() {
    events.clear();
  }

  /**
   * Establish a new MCP session.
   */
  public MCPSession establish(ConnectionContext context) {
    int session = sessionCount.incrementAndGet();
    events.add("ESTABLISH:" + session + ":" + context.origin);
    return new MockMCPSession(session);
  }

  private class MockMCPSession implements MCPSession {
    private final int sessionId;

    MockMCPSession(int sessionId) {
      this.sessionId = sessionId;
    }

    @Override
    public ServerInfo getServerInfo() {
      return serverInfo;
    }

    @Override
    public void listTools(Callback<ToolDefinition[]> callback) {
      events.add("LIST_TOOLS:" + sessionId);
      callback.success(tools.toArray(new ToolDefinition[0]));
    }

    @Override
    public void callTool(String name, ObjectNode arguments, Callback<ToolResult> callback) {
      events.add("CALL_TOOL:" + sessionId + ":" + name + ":" + arguments.toString());
      BiConsumer<ObjectNode, Callback<ToolResult>> handler = toolHandlers.get(name);
      if (handler != null) {
        handler.accept(arguments, callback);
      } else {
        callback.failure(new ErrorCodeException(ERROR_METHOD_NOT_FOUND, "Tool not found: " + name));
      }
    }

    @Override
    public void listResources(Callback<ResourceDefinition[]> callback) {
      events.add("LIST_RESOURCES:" + sessionId);
      callback.success(resources.toArray(new ResourceDefinition[0]));
    }

    @Override
    public void readResource(String uri, Callback<ResourceContent[]> callback) {
      events.add("READ_RESOURCE:" + sessionId + ":" + uri);
      Consumer<Callback<ResourceContent[]>> handler = resourceHandlers.get(uri);
      if (handler != null) {
        handler.accept(callback);
      } else {
        callback.failure(new ErrorCodeException(ERROR_RESOURCE_NOT_FOUND, "Resource not found: " + uri));
      }
    }

    @Override
    public void listPrompts(Callback<PromptDefinition[]> callback) {
      events.add("LIST_PROMPTS:" + sessionId);
      callback.success(prompts.toArray(new PromptDefinition[0]));
    }

    @Override
    public void getPrompt(String name, ObjectNode arguments, Callback<PromptMessages> callback) {
      events.add("GET_PROMPT:" + sessionId + ":" + name + ":" + arguments.toString());
      BiConsumer<ObjectNode, Callback<PromptMessages>> handler = promptHandlers.get(name);
      if (handler != null) {
        handler.accept(arguments, callback);
      } else {
        callback.failure(new ErrorCodeException(ERROR_METHOD_NOT_FOUND, "Prompt not found: " + name));
      }
    }

    @Override
    public void close() {
      events.add("CLOSE:" + sessionId);
      closeCount.incrementAndGet();
    }
  }
}
