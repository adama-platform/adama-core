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
package ape.web.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import org.junit.Assert;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** HTTP/2 client request builder for tests. Negotiates h2 via ALPN over TLS. */
public class TestHttp2ClientRequestBuilder {
  private final EventLoopGroup workerGroup;
  private final SslContext sslContext;
  private final DefaultHttpHeaders headers;
  private final int maxContentLength;
  private String host;
  private int port;
  private HttpMethod method;
  private String uri;
  private String postBody;

  private TestHttp2ClientRequestBuilder(EventLoopGroup workerGroup, SslContext sslContext) {
    this.workerGroup = workerGroup;
    this.sslContext = sslContext;
    this.host = "localhost";
    this.port = 8080;
    this.uri = "/";
    this.method = HttpMethod.GET;
    this.postBody = null;
    this.headers = new DefaultHttpHeaders();
    this.maxContentLength = 1048576;
  }

  public static TestHttp2ClientRequestBuilder start(EventLoopGroup workerGroup, SslContext sslContext) {
    return new TestHttp2ClientRequestBuilder(workerGroup, sslContext);
  }

  public TestHttp2ClientRequestBuilder server(String host, int port) {
    this.host = host;
    this.port = port;
    headers.add("Host", host);
    return this;
  }

  public TestHttp2ClientRequestBuilder get(String uri) {
    this.method = HttpMethod.GET;
    this.uri = uri;
    return this;
  }

  public TestHttp2ClientRequestBuilder post(String uri, String data) {
    this.method = HttpMethod.POST;
    this.uri = uri;
    this.postBody = data;
    return this;
  }

  public TestHttp2ClientRequestBuilder put(String uri, String data) {
    this.method = HttpMethod.PUT;
    this.uri = uri;
    this.postBody = data;
    return this;
  }

  public TestHttp2ClientRequestBuilder options(String uri) {
    this.method = HttpMethod.OPTIONS;
    this.uri = uri;
    return this;
  }

  public TestHttp2ClientRequestBuilder header(String name, String value) {
    headers.add(name, value);
    return this;
  }

  public void execute(TestClientCallback callback) throws Exception {
    final CountDownLatch alpnLatch = new CountDownLatch(1);
    final AtomicReference<Channel> parentRef = new AtomicReference<>();
    final AtomicReference<Throwable> alpnError = new AtomicReference<>();

    Bootstrap b = new Bootstrap();
    b.group(workerGroup);
    b.channel(NioSocketChannel.class);
    b.handler(new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), host, port));
        ch.pipeline().addLast(new ApplicationProtocolNegotiationHandler("") {
          @Override
          protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
            if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
              ctx.pipeline().addLast(Http2FrameCodecBuilder.forClient().build());
              ctx.pipeline().addLast(new Http2MultiplexHandler(new ChannelInboundHandlerAdapter()));
              parentRef.set(ctx.channel());
              alpnLatch.countDown();
            } else {
              alpnError.set(new Exception("Expected h2 but negotiated: " + protocol));
              alpnLatch.countDown();
              ctx.close();
            }
          }

          @Override
          protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause) {
            alpnError.set(cause);
            alpnLatch.countDown();
            ctx.close();
          }
        });
      }
    });

    ChannelFuture cf = b.connect(host, port);
    cf.addListener((ChannelFuture f) -> {
      if (!f.isSuccess()) {
        alpnError.set(f.cause());
        alpnLatch.countDown();
      }
    });
    cf.sync();

    Assert.assertTrue("ALPN negotiation timed out", alpnLatch.await(5, TimeUnit.SECONDS));
    Throwable error = alpnError.get();
    if (error != null) {
      callback.failed(error);
      return;
    }

    Channel parentChannel = parentRef.get();
    Http2StreamChannelBootstrap streamBootstrap = new Http2StreamChannelBootstrap(parentChannel);
    streamBootstrap.handler(new ChannelInitializer<Channel>() {
      @Override
      protected void initChannel(Channel ch) {
        ch.pipeline().addLast(new Http2StreamFrameToHttpObjectCodec(false));
        ch.pipeline().addLast(new HttpObjectAggregator(maxContentLength));
        ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
          @Override
          protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
            for (Map.Entry<String, String> hdr : msg.headers().entries()) {
              callback.headers.put(hdr.getKey().toLowerCase(Locale.ROOT), hdr.getValue());
            }
            callback.successfulResponse(msg.content().toString(CharsetUtil.UTF_8));
          }

          @Override
          public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            callback.failed(cause);
          }
        });
      }
    });

    Http2StreamChannel streamChannel = (Http2StreamChannel) streamBootstrap.open().sync().getNow();

    FullHttpRequest request;
    if (method == HttpMethod.POST || method == HttpMethod.PUT) {
      request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri,
          Unpooled.copiedBuffer(postBody, CharsetUtil.UTF_8));
      request.headers().add(headers);
      request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
    } else {
      request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, Unpooled.EMPTY_BUFFER);
      request.headers().add(headers);
    }

    streamChannel.writeAndFlush(request);
  }
}
