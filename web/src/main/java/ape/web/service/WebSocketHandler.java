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

import ape.web.contracts.CertificateFinder;
import ape.web.contracts.GenericWebSocketRouteSession;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutException;
import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.Json;
import ape.common.Platform;
import ape.common.TimeSource;
import ape.common.rate.AsyncTokenLimiter;
import ape.common.rate.MessageRateLimiter;
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
 * Validates WebSocket Origin header via CertificateFinder before establishing routes.
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
  private static final ConnectionContext DEFAULT_CONTEXT = new ConnectionContext("unknown", "unknown", "unknown", null);
  private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);
  private static final ExceptionLogger LOGGER = ExceptionLogger.FOR(LOG);
  private final WebConfig webConfig;
  private final WebMetrics metrics;
  private final CertificateFinder certificateFinder;
  private final AtomicLong latency;
  private GenericWebSocketRouteSession route;
  private ScheduledFuture<?> future;
  private boolean closed;
  private ConnectionContext context;
  private final WebSocketRouteTable table;
  private final AsyncTokenLimiter websocketRateLimiter;
  private final MessageRateLimiter messageRateLimiter;

  public WebSocketHandler(final WebConfig webConfig, WebMetrics metrics, WebSocketRouteTable table, CertificateFinder certificateFinder, AsyncTokenLimiter websocketRateLimiter) {
    this.webConfig = webConfig;
    this.metrics = metrics;
    this.table = table;
    this.certificateFinder = certificateFinder;
    this.websocketRateLimiter = websocketRateLimiter;
    this.messageRateLimiter = new MessageRateLimiter(webConfig.messageRateLimitPerSecond, webConfig.messageRateLimitMaxErrors, TimeSource.REAL_TIME);
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

  private void rejectOrigin(ChannelHandlerContext ctx) {
    metrics.websockets_origin_rejected.run();
    ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"disconnected\",\"reason\":\"origin-rejected\"}"));
    end(ctx);
  }

  private void rejectRateLimited(ChannelHandlerContext ctx) {
    metrics.websockets_rate_limited.run();
    ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"disconnected\",\"reason\":\"rate-limited\"}"));
    end(ctx);
  }

  private void rejectMcpDisabled(ChannelHandlerContext ctx) {
    metrics.websockets_origin_rejected.run();
    ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"disconnected\",\"reason\":\"mcp-disabled\"}"));
    end(ctx);
  }

  private void rateLimitedEstablishRoute(WebSocketServerProtocolHandler.HandshakeComplete complete, ChannelHandlerContext ctx, boolean isDevBox) {
    if (complete.requestUri().startsWith("/~s/mcp")) {
      // Gate MCP based on configured mode: off = never, devbox = only on localhost, live = always
      switch (webConfig.mcpMode) {
        case off:
          rejectMcpDisabled(ctx);
          return;
        case devbox:
          // Devbox mode is for personal development machines isolated from real traffic.
          // Only allow MCP on localhost; establish synchronously since the risk is minimal.
          if (!isDevBox) {
            rejectMcpDisabled(ctx);
            return;
          }
          establishRoute(complete, ctx);
          return;
        case live:
          // In live mode, MCP goes through the rate limiter like any other connection.
          break;
      }
    }
    websocketRateLimiter.execute(webConfig.websocketRateLimitMaxAttempts, webConfig.websocketRateLimitDelay, webConfig.websocketRateLimitJitter, new Callback<Void>() {
      @Override
      public void success(Void value) {
        ctx.executor().execute(() -> establishRoute(complete, ctx));
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ctx.executor().execute(() -> rejectRateLimited(ctx));
      }
    });
  }

  /** establish the route and start heartbeats after origin validation passes */
  private void establishRoute(WebSocketServerProtocolHandler.HandshakeComplete complete, ChannelHandlerContext ctx) {
    if (closed) {
      return;
    }
    if (complete.requestUri().startsWith("/~s/mcp")) {
      route = table.mcp.establish(complete.requestUri().substring(7), latency, context);
    } else {
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
      future = ctx.executor().scheduleAtFixedRate(heartbeatLoop, webConfig.heartbeatTimeMilliseconds, webConfig.heartbeatTimeMilliseconds, TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
    if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete && !closed) {
      WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
      HttpHeaders headers = complete.requestHeaders();
      context = ConnectionContextFactory.of(ctx, headers, webConfig.useXForwardedFor);

      // Validate the Origin header via CertificateFinder (mirrors HTTP CORS validation)
      String origin = headers.get("origin");
      String host = headers.get("host");
      boolean isDevBox = WebHandler.computeIsDevBox(host != null ? host : "");

      if (origin != null && !isDevBox) {
        String hostname = WebHandler.extractHostname(origin);
        if (hostname == null || hostname.isEmpty()) {
          rejectOrigin(ctx);
          return;
        }
        if (webConfig.specialDomains.contains(hostname)) {
          rateLimitedEstablishRoute(complete, ctx, isDevBox);
          return;
        }
        certificateFinder.fetch(hostname, new Callback<SslContext>() {
          @Override
          public void success(SslContext value) {
            ctx.executor().execute(() -> rateLimitedEstablishRoute(complete, ctx, isDevBox));
          }

          @Override
          public void failure(ErrorCodeException ex) {
            ctx.executor().execute(() -> rejectOrigin(ctx));
          }
        });
      } else {
        rateLimitedEstablishRoute(complete, ctx, isDevBox);
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
      if (route == null) {
        // Origin validation still pending or route not established
        ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"disconnected\",\"reason\":\"not-ready\"}"));
        end(ctx);
        return;
      }
      MessageRateLimiter.Result rateResult = messageRateLimiter.check();
      if (rateResult == MessageRateLimiter.Result.REJECT_WITH_ERROR) {
        metrics.websockets_message_rate_limited.run();
        ctx.writeAndFlush(new TextWebSocketFrame("{\"status\":\"error\",\"reason\":\"message-rate-limited\",\"code\":" + ErrorCodes.WEBSOCKET_MESSAGE_RATE_LIMITED + "}"));
        return;
      } else if (rateResult == MessageRateLimiter.Result.REJECT_SILENT) {
        metrics.websockets_message_rate_limited.run();
        return;
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
