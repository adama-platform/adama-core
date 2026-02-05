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

import ape.ErrorTable;
import ape.common.ErrorCodeException;
import ape.web.contracts.GenericWebSocketRouteSession;
import ape.web.contracts.ServiceBase;
import ape.web.contracts.ServiceConnection;
import ape.web.contracts.WebSocketHandlerRoute;
import ape.web.io.ConnectionContext;
import ape.web.io.JsonRequest;
import ape.web.io.JsonResponder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Adama JSON-RPC WebSocket protocol implementation for /~s endpoint.
 * Handles document subscriptions, API calls, and real-time updates.
 * Protocol: {id, method, ...params} -> {deliver:id, done:bool, response}
 * Supports ping/pong for latency measurement and keepalive health checks.
 */
public class RouteAdama implements WebSocketHandlerRoute {
  private final ServiceBase base;

  public RouteAdama(final ServiceBase base) {
    this.base = base;
  }

  @Override
  public GenericWebSocketRouteSession establish(String path, AtomicLong latency, ConnectionContext context) {
    final long created = System.currentTimeMillis();
    ServiceConnection connection = base.establishServiceConnection(context);
    return new GenericWebSocketRouteSession() {
      @Override
      public boolean sendPing(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new TextWebSocketFrame("{\"ping\":" + (System.currentTimeMillis() - created) + ",\"latency\":\"" + latency.get() + "\"}"));
        return true;
      }

      @Override
      public boolean sendKeepAliveDisconnect(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"disconnected\",\"reason\":\"keepalive-failure\"}"));
        return true;
      }

      @Override
      public boolean enableKeepAlive() {
        return true;
      }

      @Override
      public void kill() {
        connection.kill();
      }

      @Override
      public boolean keepalive() {
        return connection.keepalive();
      }

      @Override
      public void handle(ObjectNode requestNode, ChannelHandlerContext ctx) throws ErrorCodeException {
        if (requestNode.has("pong")) {
          latency.set(System.currentTimeMillis() - created - requestNode.get("ping").asLong());
          return;
        }
        JsonRequest request = new JsonRequest(requestNode, context);
        final var id = request.id();
        // tie a responder to the request
        final JsonResponder responder = new JsonResponder() {
          @Override
          public void stream(String json) {
            ctx.writeAndFlush(new TextWebSocketFrame("{\"deliver\":" + id + ",\"done\":false,\"response\":" + json + "}"));
          }

          @Override
          public void finish(String json) {
            if (json == null) {
              ctx.writeAndFlush(new TextWebSocketFrame("{\"deliver\":" + id + ",\"done\":true}"));
            } else {
              ctx.writeAndFlush(new TextWebSocketFrame("{\"deliver\":" + id + ",\"done\":true,\"response\":" + json + "}"));
            }
          }

          @Override
          public void error(ErrorCodeException ex) {
            boolean retry = ErrorTable.INSTANCE.shouldRetry(ex.code);
            ctx.writeAndFlush(new TextWebSocketFrame("{\"failure\":" + id + ",\"reason\":" + ex.code + ",\"retry\":" + (retry ? "true" : "false") + "}"));
          }
        };
        connection.execute(request, responder);
      }
    };
  }
}
