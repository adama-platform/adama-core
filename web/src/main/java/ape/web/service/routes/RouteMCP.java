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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.web.contracts.GenericWebSocketRouteSession;
import ape.web.contracts.MCPSession;
import ape.web.contracts.MCPSession.*;
import ape.web.contracts.ServiceBase;
import ape.web.contracts.WebSocketHandlerRoute;
import ape.web.io.ConnectionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.atomic.AtomicLong;

/**
 * MCP (Model Context Protocol) WebSocket route handler for /~mcp endpoint.
 * Implements JSON-RPC 2.0 message format for MCP protocol communication.
 * Handles initialization, tool calls, resource access, and prompt retrieval.
 * Protocol version: 2025-11-25
 */
public class RouteMCP implements WebSocketHandlerRoute {

  /** MCP protocol version */
  public static final String PROTOCOL_VERSION = "2025-11-25";

  /** JSON-RPC error codes */
  public static final int ERROR_PARSE = -32700;
  public static final int ERROR_INVALID_REQUEST = -32600;
  public static final int ERROR_METHOD_NOT_FOUND = -32601;
  public static final int ERROR_INVALID_PARAMS = -32602;
  public static final int ERROR_INTERNAL = -32603;
  public static final int ERROR_RESOURCE_NOT_FOUND = -32002;

  private final ServiceBase base;

  public RouteMCP(ServiceBase base) {
    this.base = base;
  }

  @Override
  public GenericWebSocketRouteSession establish(String path, AtomicLong latency, ConnectionContext context) {
    MCPSession session = base.establishMCP(path, context);
    return new MCPRouteSession(session);
  }

  /**
   * MCP session that wraps the handler session and manages JSON-RPC protocol.
   */
  private static class MCPRouteSession implements GenericWebSocketRouteSession {
    private final MCPSession session;
    private boolean initialized;

    MCPRouteSession(MCPSession session) {
      this.session = session;
      this.initialized = false;
    }

    @Override
    public boolean enableKeepAlive() {
      return false;  // MCP doesn't need keepalive pings
    }

    @Override
    public boolean sendKeepAliveDisconnect(ChannelHandlerContext ctx) {
      return false;
    }

    @Override
    public boolean sendPing(ChannelHandlerContext ctx) {
      return false;
    }

    @Override
    public void kill() {
      session.close();
    }

    @Override
    public boolean keepalive() {
      return true;
    }

    @Override
    public void handle(ObjectNode requestNode, ChannelHandlerContext ctx) throws ErrorCodeException {
      // Extract JSON-RPC fields
      JsonNode idNode = requestNode.get("id");
      JsonNode methodNode = requestNode.get("method");

      // Handle notifications (no id) - just ignore them
      if (idNode == null) {
        String method = methodNode != null ? methodNode.asText() : "";
        if ("notifications/initialized".equals(method)) {
          // Client confirms initialization complete
          return;
        }
        // Ignore other notifications
        return;
      }

      // Validate method
      if (methodNode == null || !methodNode.isTextual()) {
        sendError(ctx, idNode, ERROR_INVALID_REQUEST, "Missing or invalid method");
        return;
      }

      String method = methodNode.asText();
      ObjectNode params = getParams(requestNode);

      switch (method) {
        case "initialize":
          handleInitialize(ctx, idNode, params);
          break;
        case "tools/list":
          handleToolsList(ctx, idNode);
          break;
        case "tools/call":
          handleToolsCall(ctx, idNode, params);
          break;
        case "resources/list":
          handleResourcesList(ctx, idNode);
          break;
        case "resources/read":
          handleResourcesRead(ctx, idNode, params);
          break;
        case "prompts/list":
          handlePromptsList(ctx, idNode);
          break;
        case "prompts/get":
          handlePromptsGet(ctx, idNode, params);
          break;
        case "ping":
          handlePing(ctx, idNode);
          break;
        default:
          sendError(ctx, idNode, ERROR_METHOD_NOT_FOUND, "Method not found: " + method);
      }
    }

    private ObjectNode getParams(ObjectNode requestNode) {
      JsonNode paramsNode = requestNode.get("params");
      if (paramsNode != null && paramsNode.isObject()) {
        return (ObjectNode) paramsNode;
      }
      return Json.newJsonObject();
    }

    private void handleInitialize(ChannelHandlerContext ctx, JsonNode id, ObjectNode params) {
      initialized = true;
      ServerInfo serverInfo = session.getServerInfo();

      ObjectNode result = Json.newJsonObject();
      result.put("protocolVersion", PROTOCOL_VERSION);

      // Server capabilities
      ObjectNode capabilities = result.putObject("capabilities");

      // Declare tool support
      ObjectNode tools = capabilities.putObject("tools");
      tools.put("listChanged", false);

      // Declare resource support
      ObjectNode resources = capabilities.putObject("resources");
      resources.put("subscribe", false);
      resources.put("listChanged", false);

      // Declare prompt support
      ObjectNode prompts = capabilities.putObject("prompts");
      prompts.put("listChanged", false);

      // Server info
      ObjectNode serverInfoNode = result.putObject("serverInfo");
      serverInfoNode.put("name", serverInfo.name);
      serverInfoNode.put("version", serverInfo.version);

      sendResult(ctx, id, result);
    }

    private void handleToolsList(ChannelHandlerContext ctx, JsonNode id) {
      session.listTools(new Callback<ToolDefinition[]>() {
        @Override
        public void success(ToolDefinition[] tools) {
          ObjectNode result = Json.newJsonObject();
          ArrayNode toolsArray = result.putArray("tools");
          for (ToolDefinition tool : tools) {
            ObjectNode toolNode = toolsArray.addObject();
            toolNode.put("name", tool.name);
            toolNode.put("description", tool.description);
            if (tool.inputSchema != null) {
              toolNode.set("inputSchema", tool.inputSchema);
            }
          }
          sendResult(ctx, id, result);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendError(ctx, id, ERROR_INTERNAL, ex.getMessage());
        }
      });
    }

    private void handleToolsCall(ChannelHandlerContext ctx, JsonNode id, ObjectNode params) {
      JsonNode nameNode = params.get("name");
      if (nameNode == null || !nameNode.isTextual()) {
        sendError(ctx, id, ERROR_INVALID_PARAMS, "Missing tool name");
        return;
      }

      String name = nameNode.asText();
      ObjectNode arguments = Json.newJsonObject();
      JsonNode argsNode = params.get("arguments");
      if (argsNode != null && argsNode.isObject()) {
        arguments = (ObjectNode) argsNode;
      }

      session.callTool(name, arguments, new Callback<ToolResult>() {
        @Override
        public void success(ToolResult result) {
          ObjectNode response = Json.newJsonObject();
          response.set("content", result.content);
          if (result.isError) {
            response.put("isError", true);
          }
          sendResult(ctx, id, response);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendError(ctx, id, ex.code, ex.getMessage());
        }
      });
    }

    private void handleResourcesList(ChannelHandlerContext ctx, JsonNode id) {
      session.listResources(new Callback<ResourceDefinition[]>() {
        @Override
        public void success(ResourceDefinition[] resources) {
          ObjectNode result = Json.newJsonObject();
          ArrayNode resourcesArray = result.putArray("resources");
          for (ResourceDefinition resource : resources) {
            ObjectNode resourceNode = resourcesArray.addObject();
            resourceNode.put("uri", resource.uri);
            resourceNode.put("name", resource.name);
            if (resource.description != null) {
              resourceNode.put("description", resource.description);
            }
            if (resource.mimeType != null) {
              resourceNode.put("mimeType", resource.mimeType);
            }
          }
          sendResult(ctx, id, result);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendError(ctx, id, ERROR_INTERNAL, ex.getMessage());
        }
      });
    }

    private void handleResourcesRead(ChannelHandlerContext ctx, JsonNode id, ObjectNode params) {
      JsonNode uriNode = params.get("uri");
      if (uriNode == null || !uriNode.isTextual()) {
        sendError(ctx, id, ERROR_INVALID_PARAMS, "Missing resource uri");
        return;
      }

      String uri = uriNode.asText();
      session.readResource(uri, new Callback<ResourceContent[]>() {
        @Override
        public void success(ResourceContent[] contents) {
          ObjectNode result = Json.newJsonObject();
          ArrayNode contentsArray = result.putArray("contents");
          for (ResourceContent content : contents) {
            ObjectNode contentNode = contentsArray.addObject();
            contentNode.put("uri", content.uri);
            if (content.mimeType != null) {
              contentNode.put("mimeType", content.mimeType);
            }
            contentNode.put("text", content.text);
          }
          sendResult(ctx, id, result);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendError(ctx, id, ex.code, ex.getMessage());
        }
      });
    }

    private void handlePromptsList(ChannelHandlerContext ctx, JsonNode id) {
      session.listPrompts(new Callback<PromptDefinition[]>() {
        @Override
        public void success(PromptDefinition[] prompts) {
          ObjectNode result = Json.newJsonObject();
          ArrayNode promptsArray = result.putArray("prompts");
          for (PromptDefinition prompt : prompts) {
            ObjectNode promptNode = promptsArray.addObject();
            promptNode.put("name", prompt.name);
            if (prompt.description != null) {
              promptNode.put("description", prompt.description);
            }
            if (prompt.arguments != null) {
              promptNode.set("arguments", prompt.arguments);
            }
          }
          sendResult(ctx, id, result);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendError(ctx, id, ERROR_INTERNAL, ex.getMessage());
        }
      });
    }

    private void handlePromptsGet(ChannelHandlerContext ctx, JsonNode id, ObjectNode params) {
      JsonNode nameNode = params.get("name");
      if (nameNode == null || !nameNode.isTextual()) {
        sendError(ctx, id, ERROR_INVALID_PARAMS, "Missing prompt name");
        return;
      }

      String name = nameNode.asText();
      ObjectNode arguments = Json.newJsonObject();
      JsonNode argsNode = params.get("arguments");
      if (argsNode != null && argsNode.isObject()) {
        arguments = (ObjectNode) argsNode;
      }

      session.getPrompt(name, arguments, new Callback<PromptMessages>() {
        @Override
        public void success(PromptMessages result) {
          ObjectNode response = Json.newJsonObject();
          if (result.description != null) {
            response.put("description", result.description);
          }
          response.set("messages", result.messages);
          sendResult(ctx, id, response);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendError(ctx, id, ex.code, ex.getMessage());
        }
      });
    }

    private void handlePing(ChannelHandlerContext ctx, JsonNode id) {
      sendResult(ctx, id, Json.newJsonObject());
    }

    private void sendResult(ChannelHandlerContext ctx, JsonNode id, ObjectNode result) {
      ObjectNode response = Json.newJsonObject();
      response.put("jsonrpc", "2.0");
      response.set("id", id);
      response.set("result", result);
      ctx.writeAndFlush(new TextWebSocketFrame(response.toString()));
    }

    private void sendError(ChannelHandlerContext ctx, JsonNode id, int code, String message) {
      ObjectNode response = Json.newJsonObject();
      response.put("jsonrpc", "2.0");
      response.set("id", id);
      ObjectNode error = response.putObject("error");
      error.put("code", code);
      error.put("message", message);
      ctx.writeAndFlush(new TextWebSocketFrame(response.toString()));
    }
  }
}
