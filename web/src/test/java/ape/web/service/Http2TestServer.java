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

import ape.common.TimeSource;
import ape.runtime.sys.domains.DomainFinder;
import ape.web.assets.cache.WebHandlerAssetCache;
import ape.web.assets.transforms.TransformQueue;
import ape.web.contracts.CertificateFinder;
import ape.web.contracts.ServiceBase;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.InputStream;

/** Test helper that boots a Netty server with a test TLS certificate and ALPN for HTTP/2 testing. */
public class Http2TestServer {
  private final SslContext serverSslContext;
  private final SslContext clientSslContext;
  private Channel channel;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private WebHandlerAssetCache cache;
  private TransformQueue transformQueue;

  public Http2TestServer() throws Exception {
    InputStream certStream = Http2TestServer.class.getClassLoader().getResourceAsStream("test-cert.pem");
    InputStream keyStream = Http2TestServer.class.getClassLoader().getResourceAsStream("test-key.pem");
    this.serverSslContext = SslContextBuilder.forServer(certStream, keyStream)
        .applicationProtocolConfig(new ApplicationProtocolConfig(
            ApplicationProtocolConfig.Protocol.ALPN,
            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
            ApplicationProtocolNames.HTTP_2,
            ApplicationProtocolNames.HTTP_1_1))
        .build();
    this.clientSslContext = SslContextBuilder.forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .applicationProtocolConfig(new ApplicationProtocolConfig(
            ApplicationProtocolConfig.Protocol.ALPN,
            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
            ApplicationProtocolNames.HTTP_2))
        .build();
  }

  public SslContext clientSslContext() {
    return clientSslContext;
  }

  public void start(WebConfig webConfig, WebMetrics metrics, ServiceBase base,
                    CertificateFinder certFinder, DomainFinder domainFinder) throws Exception {
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup(4);
    cache = new WebHandlerAssetCache(TimeSource.REAL_TIME, webConfig.cacheRoot);
    transformQueue = new TransformQueue(TimeSource.REAL_TIME, webConfig.transformRoot, base.assets(), webConfig);
    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new Initializer(webConfig, metrics, base, certFinder, serverSslContext, cache, domainFinder, transformQueue));
    channel = b.bind(webConfig.port).sync().channel();
  }

  public void shutdown() {
    if (channel != null) {
      channel.close();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
    }
    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
    }
    if (cache != null) {
      cache.shutdown();
    }
    if (transformQueue != null) {
      transformQueue.shutdown();
    }
  }
}
