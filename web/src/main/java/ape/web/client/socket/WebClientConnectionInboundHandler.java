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
package ape.web.client.socket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import ape.ErrorCodes;
import ape.common.Json;
import ape.web.contracts.WebJsonStream;
import ape.web.contracts.WebLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/** internal: class for wrapping the netty handler into a nice and neat package */
public class WebClientConnectionInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebClientConnectionInboundHandler.class);
  private final WebLifecycle lifecycle;
  private final ConcurrentHashMap<Integer, WebJsonStream> streams;
  private WebClientConnection connection;
  private boolean closed;

  public WebClientConnectionInboundHandler(WebLifecycle lifecycle) {
    this.lifecycle = lifecycle;
    this.streams = new ConcurrentHashMap<>();
    this.connection = null;
    this.closed = false;
  }

  public boolean registerWhileInExecutor(int id, WebJsonStream stream) {
    if (closed) {
      stream.failure(ErrorCodes.WEBBASE_CONNECTION_CLOSE);
      return false;
    }
    streams.put(id, stream);
    return true;
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame frame) throws Exception {
    ObjectNode node = Json.parseJsonObject(frame.text());
    if (node.has("ping")) {
      int latency = node.get("latency").asInt();
      if (latency > 0) {
        lifecycle.ping(latency);
      }
      node.put("pong", true);
      ctx.channel().writeAndFlush(new TextWebSocketFrame(node.toString()));
      return;
    }

    if (node.has("status")) {
      if ("connected".equals(node.get("status").textValue())) {
        String version = "?";
        if (node.has("version")) {
          version = node.get("version").textValue();
        }
        lifecycle.connected(connection, version);
      }
      return;
    }

    if (node.has("failure")) {
      int id = node.get("failure").asInt();
      int reason = node.get("reason").asInt();
      WebJsonStream streamback = streams.remove(id);
      if (streamback != null) {
        streamback.failure(reason);
      }
    } else if (node.has("deliver")) {
      int id = node.get("deliver").asInt();
      boolean done = node.get("done").asBoolean();
      WebJsonStream streamback = done ? streams.remove(id) : streams.get(id);
      if (streamback != null) {
        ObjectNode response = (ObjectNode) node.get("response");
        if (response != null && !response.isEmpty()) {
          streamback.data(id, response);
        }
        if (done) {
          streamback.complete();
        }
      }
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    connection = new WebClientConnection(ctx, this, () -> {
      end(ctx);
    });
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    end(ctx);
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
    LOGGER.error("web-client-fail:", cause);
    lifecycle.failure(cause);
    end(ctx);
  }

  private boolean end(ChannelHandlerContext ctx) {
    if (closed) {
      return false;
    }
    closed = true;
    HashSet<WebJsonStream> copy = new HashSet<>(streams.values());
    streams.clear();
    for (WebJsonStream stream : copy) {
      stream.failure(ErrorCodes.WEBBASE_LOST_CONNECTION);
    }
    lifecycle.disconnected();
    ctx.close();
    return true;
  }
}
