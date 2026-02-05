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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.web.contracts.WebJsonStream;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Active WebSocket connection wrapper for JSON-RPC style communication.
 * Manages request ID generation, request registration for response routing,
 * and supports both streaming (execute) and request-response patterns.
 */
public class WebClientConnection {
  private final ChannelHandlerContext ctx;
  private final AtomicInteger idgen;
  private final WebClientConnectionInboundHandler handler;
  private final Runnable close;

  WebClientConnection(final ChannelHandlerContext ctx, WebClientConnectionInboundHandler handler, Runnable close) {
    this.ctx = ctx;
    this.idgen = new AtomicInteger(0);
    this.handler = handler;
    this.close = close;
  }

  public int execute(ObjectNode request, WebJsonStream streamback) {
    int id = idgen.incrementAndGet();
    ctx.executor().execute(() -> {
      request.put("id", id);
      if (handler.registerWhileInExecutor(id, streamback)) {
        ctx.writeAndFlush(new TextWebSocketFrame(request.toString()));
      }
    });
    return id;
  }

  public <T> void requestResponse(ObjectNode request, Function<ObjectNode, T> transform, Callback<T> callback) {
    execute(request, new WebJsonStream() {
      boolean sentSuccess;
      @Override
      public void data(int connection, ObjectNode node) {
        if (!sentSuccess) {
          callback.success(transform.apply(node));
          sentSuccess = true;
        }
      }

      @Override
      public void complete() {
        if (!sentSuccess) {
          callback.success(null);
          sentSuccess = true;
        }
      }

      @Override
      public void failure(int code) {
        callback.failure(new ErrorCodeException(code));
      }
    });
  }

  public void close() {
    this.close.run();
  }
}
