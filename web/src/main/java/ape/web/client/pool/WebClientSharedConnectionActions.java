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
package ape.web.client.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.*;
import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.pool.PoolActions;
import ape.web.client.WebClientBaseMetrics;

import java.util.concurrent.TimeUnit;

/** how the shared connection pool is acted upon */
public class WebClientSharedConnectionActions implements PoolActions<WebEndpoint, WebClientSharedConnection> {
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(WebClientSharedConnectionActions.class);
  private final EventLoopGroup group;
  private final WebClientBaseMetrics metrics;

  public WebClientSharedConnectionActions(WebClientBaseMetrics metrics, EventLoopGroup group) {
    this.metrics = metrics;
    this.group = group;
  }

  @Override
  public void create(WebEndpoint request, Callback<WebClientSharedConnection> createdRaw) {
    Callback<WebClientSharedConnection> created = metrics.web_create_shared.wrap(createdRaw);
    WebClientSharedConnection connection = new WebClientSharedConnection(metrics, request, group);
    final var b = new Bootstrap();
    b.group(group);
    b.channel(NioSocketChannel.class);
    b.handler(new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(final SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
        ch.pipeline().addLast(new WriteTimeoutHandler(240, TimeUnit.SECONDS));
        if (request.secure) {
          ch.pipeline().addLast(SslContextBuilder.forClient().build().newHandler(ch.alloc(), request.host, request.port));
        }
        ch.pipeline().addLast(new HttpClientCodec());
        ch.pipeline().addLast(
          new SimpleChannelInboundHandler<HttpObject>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
              connection.handle(msg);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
              connection.failure(new ErrorCodeException(ErrorCodes.WEB_BASE_EXECUTE_INACTIVE));
            }

            @Override
            public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
              if (cause instanceof ReadTimeoutException || cause instanceof WriteTimeoutException) {
                connection.failure(new ErrorCodeException(ErrorCodes.WEB_BASE_EXECUTE_TIMEOUT));
              } else {
                connection.failure(ErrorCodeException.detectOrWrap(ErrorCodes.WEB_BASE_EXECUTE_FAILED_EXCEPTION_CAUGHT, cause, EXLOGGER));
              }
              ctx.close();
            }
          });
      }
    });

    b.connect(request.host, request.port).addListeners((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        connection.setChannel(future.channel());
        created.success(connection);
      } else {
        created.failure(new ErrorCodeException(ErrorCodes.WEB_BASE_EXECUTE_FAILED_CONNECT, "Failed to connect[" + request.host + ":" + request.port + "]"));
      }
    });
  }

  @Override
  public void destroy(WebClientSharedConnection item) {
    item.close();
  }
}
