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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.web.contracts.WellKnownHandler;

import java.nio.charset.StandardCharsets;

public class RedirectHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final byte[] EMPTY_RESPONSE = new byte[0];
  private final WebConfig webConfig;
  private final WellKnownHandler wellKnownHandler;

  public RedirectHandler(WebConfig webConfig, WellKnownHandler wellKnownHandler) {
    this.webConfig = webConfig;
    this.wellKnownHandler = wellKnownHandler;
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
    if (webConfig.healthCheckPath.equals(req.uri())) { // health checks
      sendImmediate(req, ctx, HttpResponseStatus.OK, ("HEALTHY:" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8), "text/plain; charset=UTF-8", true);
      return;
    }
    if (req.uri().equals("/.are.you.adama")) {
      sendImmediate(req, ctx, HttpResponseStatus.OK, ("YES").getBytes(StandardCharsets.UTF_8), "text/plain; charset=UTF-8", true);
      return;
    }
    if (req.uri().startsWith("/.well-known/") && !WellKnownHandler.DONT_HANDLE(req.uri())) {
      wellKnownHandler.handle(req.uri(), new Callback<String>() {
        @Override
        public void success(String response) {
          sendImmediate(req, ctx, HttpResponseStatus.OK, (response).getBytes(StandardCharsets.UTF_8), "text/plain", true);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendImmediate(req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, EMPTY_RESPONSE, "text/plain", true);
        }
      });
      return;
    }
    final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.PERMANENT_REDIRECT, Unpooled.wrappedBuffer(EMPTY_RESPONSE));
    res.headers().set(HttpHeaderNames.LOCATION, "https://" + req.headers().get(HttpHeaderNames.HOST) + req.uri());
    final var responseStatus = res.status();
    final var keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
    HttpUtil.setKeepAlive(res, keepAlive);
    final var future = ctx.writeAndFlush(res);
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
  }

  /** send an immediate data result */
  private void sendImmediate(FullHttpRequest req, final ChannelHandlerContext ctx, HttpResponseStatus status, byte[] content, String contentType, boolean cors) {
    final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), status, Unpooled.wrappedBuffer(content));
    HttpUtil.setContentLength(res, content.length);
    if (contentType != null) {
      res.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }
    final var responseStatus = res.status();
    final var keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
    HttpUtil.setKeepAlive(res, keepAlive);
    final var future = ctx.writeAndFlush(res);
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
  }
}
