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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.ScheduledFuture;
import ape.common.TimeSource;
import ape.runtime.sys.domains.DomainFinder;
import ape.web.assets.cache.WebHandlerAssetCache;
import ape.web.assets.transforms.TransformQueue;
import ape.web.contracts.CertificateFinder;
import ape.web.contracts.ServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main HTTP/WebSocket server bootstrap using Netty NIO.
 * Binds to configured port, optionally enables TLS via cert.pem/key.pem,
 * initializes the channel pipeline with WebHandler and WebSocketHandler,
 * and runs periodic heartbeats. Manages graceful shutdown of event loops,
 * asset cache, and transform queue.
 */
public class ServiceRunnable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRunnable.class);
  private final WebConfig webConfig;
  private final WebMetrics metrics;
  private final ServiceBase base;
  private final CertificateFinder certificateFinder;
  private final CountDownLatch ready;
  private final AtomicBoolean started;
  private final Runnable heartbeat;
  private final WebHandlerAssetCache cache;
  private Channel channel;
  private boolean stopped;
  private final DomainFinder domainFinder;
  private final TransformQueue transformQueue;

  public ServiceRunnable(final WebConfig webConfig, final WebMetrics metrics, ServiceBase base, CertificateFinder certificateFinder, DomainFinder domainFinder, Runnable heartbeat) {
    this.webConfig = webConfig;
    this.metrics = metrics;
    this.base = base;
    this.certificateFinder = certificateFinder;
    this.domainFinder = domainFinder;
    started = new AtomicBoolean();
    channel = null;
    stopped = false;
    ready = new CountDownLatch(1);
    this.heartbeat = heartbeat;
    this.cache = new WebHandlerAssetCache(TimeSource.REAL_TIME, webConfig.cacheRoot);
    this.transformQueue = new TransformQueue(TimeSource.REAL_TIME, webConfig.transformRoot, base.assets());
  }

  public synchronized boolean isAccepting() {
    return channel != null;
  }

  public boolean waitForReady(final int ms) throws InterruptedException {
    return ready.await(ms, TimeUnit.MILLISECONDS);
  }

  @Override
  public void run() {
    if (!started.compareAndExchange(false, true)) {
      LOGGER.info("starting-web-proxy");
      try {
        try {
          AtomicBoolean alive = new AtomicBoolean(true);
          SslContext context = null;
          File certificate = new File("cert.pem");
          File privateKey = new File("key.pem");
          if (certificate.exists() && privateKey.exists()) {
            context = SslContextBuilder.forServer(certificate, privateKey).build();
            LOGGER.info("found-certificate-and-key");
          }
          final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
          final EventLoopGroup workerGroup = new NioEventLoopGroup(webConfig.workerThreads);
          try {
            final var b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)//
             .channel(NioServerSocketChannel.class) //
             .childHandler(new Initializer(webConfig, metrics, base, certificateFinder, context, cache, domainFinder, transformQueue));
            final var ch = b.bind(webConfig.port).sync().channel();
            channelRegistered(ch);
            LOGGER.info("channel-registered");
            ScheduledFuture<?> future = bossGroup.scheduleAtFixedRate(() -> {
              workerGroup.schedule(() -> {
                if (alive.get()) {
                  metrics.websockets_server_heartbeat.run();
                  heartbeat.run();
                }
              }, (int) (10 + 15 * Math.random()), TimeUnit.MILLISECONDS);
            }, 50, 50, TimeUnit.MILLISECONDS);
            ch.closeFuture().sync();
            LOGGER.info("channel-close-future-syncd");
            future.cancel(false);
          } finally {
            alive.set(false);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
          }
        } catch (final Exception ex) {
          LOGGER.error("exception-server", ex);
          shutdown();
        }
      } finally {
        ready.countDown();
      }
    }
  }

  private synchronized void channelRegistered(final Channel channel) {
    this.channel = channel;
    if (stopped) {
      channel.close();
    }
    ready.countDown();
  }

  public synchronized void shutdown() {
    stopped = true;
    if (channel != null) {
      channel.close();
    }
    transformQueue.shutdown();
    cache.shutdown();
  }
}
