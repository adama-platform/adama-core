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

import ape.web.contracts.GenericWebSocketRouteSession;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.Json;
import ape.common.Platform;
import ape.web.io.ConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty WebSocket frame handler for JSON-RPC style real-time communication.
 * Manages connection lifecycle, periodic heartbeats for health checking,
 * and routes incoming text frames to appropriate service handlers via
 * WebSocketRouteTable. Only accepts text frames; binary frames are rejected.
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
  private static final ConnectionContext DEFAULT_CONTEXT = new ConnectionContext("unknown", "unknown", "unknown", null);
  private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);
  private static final ExceptionLogger LOGGER = ExceptionLogger.FOR(LOG);
  private final WebConfig webConfig;
  private final WebMetrics metrics;
  private final AtomicLong latency;
  private GenericWebSocketRouteSession route;
  private ScheduledFuture<?> future;
  private boolean closed;
  private ConnectionContext context;
  private final WebSocketRouteTable table;

  public WebSocketHandler(final WebConfig webConfig, WebMetrics metrics, WebSocketRouteTable table) {
    this.webConfig = webConfig;
    this.metrics = metrics;
    this.table = table;
    this.route = null;
    this.future = null;
    this.latency = new AtomicLong();
    this.closed = false;
    this.context = DEFAULT_CONTEXT;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    metrics.websockets_active.up();
    metrics.websockets_start.run();
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
    metrics.websockets_active.down();
    metrics.websockets_end.run();
    end(ctx);
    super.channelInactive(ctx);
  }

  private synchronized void end(ChannelHandlerContext ctx) {
    try {
      if (closed) {
        return;
      }
      closed = true;
      if (future != null) {
        future.cancel(false);
        future = null;
      }
      if (route != null) {
        metrics.websockets_active_child_connections.down();
        route.kill();
        route = null;
      }
      ctx.close();
    } catch (Exception ex) {
      LOG.error("end-exception", ex);
      metrics.websockets_end_exception.run();
    }
  }

  @Override
  public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
    if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete && !closed) {
      WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
      HttpHeaders headers = complete.requestHeaders();
      context = ConnectionContextFactory.of(ctx, headers);
      // establish the service by routing directly via the URI
      if (complete.requestUri().startsWith("/~s/mcp")) {
        route = table.mcp.establish(complete.requestUri().substring(7), latency, context);
      } else {
        // tell Adama client all is ok
        ObjectNode connected = Json.newJsonObject();
        connected.put("status", "connected");
        connected.put("version", Platform.VERSION);
        if (context.identities != null) {
          ObjectNode idents = connected.putObject("identities");
          for (String ident : context.identities.keySet()) {
            idents.put(ident, true);
          }
        }
        ctx.writeAndFlush(new TextWebSocketFrame(connected.toString()));
        route = table.adama.establish("/", latency, context);
      }
      metrics.websockets_active_child_connections.up();
      if (route.enableKeepAlive()) {
        // start the heartbeat loop
        Runnable heartbeatLoop = () -> {
          if (route != null && !route.keepalive()) {
            metrics.websockets_heartbeat_failure.run();
            route.sendKeepAliveDisconnect(ctx);
            end(ctx);
          } else {
            if (route.sendPing(ctx)) {
              metrics.websockets_send_heartbeat.run();

            }
          }
        };
        // schedule the heartbeat loop
        future = ctx.executor().scheduleAtFixedRate(heartbeatLoop, webConfig.heartbeatTimeMilliseconds, webConfig.heartbeatTimeMilliseconds, TimeUnit.MILLISECONDS);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    try {
      super.exceptionCaught(ctx, cause);
      if (cause instanceof IOException && "Connection timed out".equals(cause.getMessage()) || cause instanceof ReadTimeoutException) {
        metrics.websockets_timed_out.run();
      } else if (cause instanceof SocketException) {
        metrics.websockets_socket_exception.run();
      } else if (cause instanceof DecoderException) {
        metrics.websockets_decode_exception.run();
      } else {
        metrics.websockets_uncaught_exception.run();
        LOG.error("exception", cause);
      }
    } finally {
      end(ctx);
    }
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final WebSocketFrame frame) throws Exception {
    try {
      if (!(frame instanceof TextWebSocketFrame)) {
        throw new ErrorCodeException(ErrorCodes.ONLY_ACCEPTS_TEXT_FRAMES);
      }
      // parse the request
      final var requestNode = Json.parseJsonObject(((TextWebSocketFrame) frame).text());
      route.handle(requestNode, ctx);
    } catch (Exception ex) {
      ErrorCodeException codedException = ErrorCodeException.detectOrWrap(ErrorCodes.UNCAUGHT_EXCEPTION_WEB_SOCKET, ex, LOGGER);
      ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"disconnected\",\"reason\":" + codedException.code + "}"));
      end(ctx);
    }
  }
}
