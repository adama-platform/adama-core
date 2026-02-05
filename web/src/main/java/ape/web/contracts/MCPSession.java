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
package ape.web.contracts;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Per-connection MCP (Model Context Protocol) session handler.
 * Handles all MCP method calls for a single WebSocket session.
 * The protocol layer (RouteMCP) manages JSON-RPC formatting while
 * implementations focus on business logic.
 */
public interface MCPSession {

  /** MCP error code for method not found */
  int ERROR_METHOD_NOT_FOUND = -32601;

  /** MCP error code for resource not found */
  int ERROR_RESOURCE_NOT_FOUND = -32002;

  /** MCP error code for invalid params */
  int ERROR_INVALID_PARAMS = -32602;

  /**
   * Server information returned during MCP initialization.
   */
  class ServerInfo {
    public final String name;
    public final String version;

    public ServerInfo(String name, String version) {
      this.name = name;
      this.version = version;
    }
  }

  /**
   * Tool definition for MCP tools/list response.
   */
  class ToolDefinition {
    public final String name;
    public final String description;
    public final ObjectNode inputSchema;

    public ToolDefinition(String name, String description, ObjectNode inputSchema) {
      this.name = name;
      this.description = description;
      this.inputSchema = inputSchema;
    }
  }

  /**
   * Result of a tool invocation.
   */
  class ToolResult {
    public final ArrayNode content;
    public final boolean isError;

    public ToolResult(ArrayNode content, boolean isError) {
      this.content = content;
      this.isError = isError;
    }
  }

  /**
   * Resource definition for MCP resources/list response.
   */
  class ResourceDefinition {
    public final String uri;
    public final String name;
    public final String description;
    public final String mimeType;

    public ResourceDefinition(String uri, String name, String description, String mimeType) {
      this.uri = uri;
      this.name = name;
      this.description = description;
      this.mimeType = mimeType;
    }
  }

  /**
   * Resource content returned from resources/read.
   */
  class ResourceContent {
    public final String uri;
    public final String mimeType;
    public final String text;

    public ResourceContent(String uri, String mimeType, String text) {
      this.uri = uri;
      this.mimeType = mimeType;
      this.text = text;
    }
  }

  /**
   * Prompt definition for MCP prompts/list response.
   */
  class PromptDefinition {
    public final String name;
    public final String description;
    public final ArrayNode arguments;

    public PromptDefinition(String name, String description, ArrayNode arguments) {
      this.name = name;
      this.description = description;
      this.arguments = arguments;
    }
  }

  /**
   * Prompt messages returned from prompts/get.
   */
  class PromptMessages {
    public final String description;
    public final ArrayNode messages;

    public PromptMessages(String description, ArrayNode messages) {
      this.description = description;
      this.messages = messages;
    }
  }

  /**
   * Get server information for the initialize response.
   */
  ServerInfo getServerInfo();

  /**
   * List available tools.
   */
  void listTools(Callback<ToolDefinition[]> callback);

  /**
   * Call a tool with the given name and arguments.
   */
  void callTool(String name, ObjectNode arguments, Callback<ToolResult> callback);

  /**
   * List available resources.
   */
  void listResources(Callback<ResourceDefinition[]> callback);

  /**
   * Read a resource by URI.
   */
  void readResource(String uri, Callback<ResourceContent[]> callback);

  /**
   * List available prompts.
   */
  void listPrompts(Callback<PromptDefinition[]> callback);

  /**
   * Get a prompt by name with optional arguments.
   */
  void getPrompt(String name, ObjectNode arguments, Callback<PromptMessages> callback);

  /**
   * Called when the session is closed.
   */
  void close();

  /**
   * A no-op session that returns empty results or appropriate errors.
   */
  MCPSession NOOP = new MCPSession() {
    private final ServerInfo serverInfo = new ServerInfo("noop", "1.0.0");

    @Override
    public ServerInfo getServerInfo() {
      return serverInfo;
    }

    @Override
    public void listTools(Callback<ToolDefinition[]> callback) {
      callback.success(new ToolDefinition[0]);
    }

    @Override
    public void callTool(String name, ObjectNode arguments, Callback<ToolResult> callback) {
      callback.failure(new ErrorCodeException(ERROR_METHOD_NOT_FOUND, "Tool not found: " + name));
    }

    @Override
    public void listResources(Callback<ResourceDefinition[]> callback) {
      callback.success(new ResourceDefinition[0]);
    }

    @Override
    public void readResource(String uri, Callback<ResourceContent[]> callback) {
      callback.failure(new ErrorCodeException(ERROR_RESOURCE_NOT_FOUND, "Resource not found: " + uri));
    }

    @Override
    public void listPrompts(Callback<PromptDefinition[]> callback) {
      callback.success(new PromptDefinition[0]);
    }

    @Override
    public void getPrompt(String name, ObjectNode arguments, Callback<PromptMessages> callback) {
      callback.failure(new ErrorCodeException(ERROR_METHOD_NOT_FOUND, "Prompt not found: " + name));
    }

    @Override
    public void close() {
    }
  };
}
