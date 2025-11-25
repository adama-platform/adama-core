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
package ape.common.net;

import ape.common.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import ape.ErrorCodes;
import ape.common.gossip.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/** defines the threading base for the common networking library */
public class NetBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetBase.class);
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(LOGGER);
  public final NetMetrics metrics;
  public final NioEventLoopGroup bossGroup;
  public final NioEventLoopGroup workerGroup;
  public final MachineIdentity identity;
  private final AtomicBoolean alive;
  private final CountDownLatch killLatch;
  private final SslContext sslContext;
  private final Engine engine;

  public NetBase(NetMetrics metrics, MachineIdentity identity, int bossThreads, int workerThreads) throws Exception {
    this.metrics = metrics;
    this.identity = identity;
    this.sslContext = SslContextBuilder.forClient().keyManager(identity.getCert(), identity.getKey()).trustManager(identity.getTrust()).build();
    this.bossGroup = new NioEventLoopGroup(bossThreads);
    this.workerGroup = new NioEventLoopGroup(workerThreads);
    this.alive = new AtomicBoolean(true);
    this.killLatch = new CountDownLatch(1);
    this.engine = new Engine(identity.ip, metrics.gossip, TimeSource.REAL_TIME);
  }

  public Engine startGossiping() {
    engine.kickoff(alive);
    return engine;
  }

  public boolean alive() {
    return alive.get();
  }

  public void connect(String target, Lifecycle lifecycle) {
    if (!alive.get()) {
      lifecycle.failed(new ErrorCodeException(ErrorCodes.NET_SHUTTING_DOWN));
    }
    try {
      String[] parts = target.split(Pattern.quote(":"));
      String peerHost = parts[0];
      int peerPort = Integer.parseInt(parts[1]);
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(workerGroup);
      bootstrap.remoteAddress(peerHost, peerPort);
      bootstrap.channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
      bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 250);
      bootstrap.option(ChannelOption.TCP_NODELAY, true);
      bootstrap.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
          metrics.net_create_client_handler.run();
          ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), peerHost, peerPort));
          ch.pipeline().addLast(new LengthFieldPrepender(4));
          ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(67108864, 0, 4, 0, 4));
          ch.pipeline().addLast(new ChannelClient(peerHost, peerPort, lifecycle, engine));
        }
      });
      bootstrap.connect().addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
          if (!channelFuture.isSuccess()) {
            if (!StartUp.hasRecentlyStartedUp()) {
              LOGGER.error("failed-connect({}) : {}", target, channelFuture.cause().getMessage());
            }
            lifecycle.failed(new ErrorCodeException(ErrorCodes.NET_CONNECT_FAILED_TO_CONNECT));
          }
        }
      });
    } catch (Exception ex) {
      lifecycle.failed(ErrorCodeException.detectOrWrap(ErrorCodes.NET_CONNECT_FAILED_UNKNOWN, ex, EXLOGGER));
    }
  }

  public ServerHandle serve(int port, Handler handler) throws Exception {
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup);
    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.localAddress(port);
    SslContext sslContext = makeServerSslContext();
    SocketChannelSet set = new SocketChannelSet();
    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
      protected void initChannel(SocketChannel ch) throws Exception {
        metrics.net_create_server_handler.run();
        ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
        ch.pipeline().addLast(new LengthFieldPrepender(4));
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(67108864, 0, 4, 0, 4));
        ch.pipeline().addLast(new ChannelServer(ch, set, handler, engine));
      }
    });
    ChannelFuture future = bootstrap.bind();
    LOGGER.error("started server:" + port);
    CountDownLatch waitForEndLatch = new CountDownLatch(1);
    return new ServerHandle() {
      @Override
      public void waitForEnd() {
        LOGGER.info("waiting");
        try {
          future.channel().closeFuture().sync();
        } catch (Exception ex) {
          LOGGER.info("failure", ex);
          ex.printStackTrace();
        }
        LOGGER.info("finished");
        waitForEndLatch.countDown();
      }

      @Override
      public void kill() {
        try {
          future.channel().close().sync();
          if (future.channel().parent() != null) {
            future.channel().parent().close().sync();
          }
          set.kill();
          waitForEndLatch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
    };
  }

  public SslContext makeServerSslContext() throws Exception {
    return SslContextBuilder.forServer(identity.getCert(), identity.getKey()).trustManager(identity.getTrust()).clientAuth(ClientAuth.REQUIRE).build();
  }

  public void waitForShutdown() throws InterruptedException {
    killLatch.await();
  }

  public void shutdown() {
    alive.set(false);
    killLatch.countDown();
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }
}
