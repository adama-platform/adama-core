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

import ape.web.service.routes.RouteAdama;
import ape.web.service.routes.RouteMCP;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.sys.domains.DomainFinder;
import ape.web.assets.cache.WebHandlerAssetCache;
import ape.web.assets.transforms.TransformQueue;
import ape.web.contracts.CertificateFinder;
import ape.web.contracts.ServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Netty channel pipeline configurator for incoming connections.
 * Builds the handler chain: read timeout, SNI-based TLS (when enabled),
 * HTTP codec, content aggregation, WebSocket compression and protocol handling,
 * HTTP compression, WebHandler for HTTP requests, and WebSocketHandler for
 * upgraded WebSocket connections.
 */
public class Initializer extends ChannelInitializer<SocketChannel> {
  private final Logger logger;
  private final WebConfig webConfig;
  private final WebMetrics metrics;
  private final ServiceBase base;
  private final CertificateFinder certificateFinder;
  private final SslContext context;
  private final WebHandlerAssetCache cache;
  private final DomainFinder domainFinder;
  private final TransformQueue transformQueue;

  public Initializer(final WebConfig webConfig, final WebMetrics metrics, final ServiceBase base, final CertificateFinder certificateFinder, SslContext context, WebHandlerAssetCache cache, DomainFinder domainFinder, TransformQueue transformQueue) {
    this.logger = LoggerFactory.getLogger("Initializer");
    this.webConfig = webConfig;
    this.metrics = metrics;
    this.base = base;
    this.certificateFinder = certificateFinder;
    this.context = context;
    this.cache = cache;
    this.domainFinder = domainFinder;
    this.transformQueue = transformQueue;
  }

  @Override
  public void initChannel(final SocketChannel ch) throws Exception {
    logger.info("initializing channel: {}", ch.remoteAddress());
    final var pipeline = ch.pipeline();
    pipeline.addLast(new ReadTimeoutHandler(webConfig.idleReadSeconds, TimeUnit.SECONDS));
    if (context != null) {
      pipeline.addLast("sni", new SniHandler((domain, promise) -> {
        certificateFinder.fetch(domain, new Callback<SslContext>() {
          @Override
          public void success(SslContext contextToUse) {
            if (contextToUse != null) {
              promise.setSuccess(contextToUse);
            } else { // the default context when given a null
              promise.setSuccess(context);
            }
          }

          @Override
          public void failure(ErrorCodeException ex) {
            promise.setFailure(ex);
          }
        });
        return promise;
      }));
    }
    pipeline.addLast(new HttpServerCodec());
    pipeline.addLast(new HttpObjectAggregator(webConfig.maxContentLengthSize));
    pipeline.addLast(new WebSocketServerCompressionHandler());
    pipeline.addLast(new WebSocketServerProtocolHandler("/~s", null, true, webConfig.maxWebSocketFrameSize, false, true, webConfig.timeoutWebsocketHandshake));
    // the pipeline must reflect the various routes for the table
    WebSocketRouteTable table = new WebSocketRouteTable(new RouteAdama(base), new RouteMCP(base));
    pipeline.addLast(new HttpContentCompressor());
    pipeline.addLast(new WebHandler(webConfig, metrics, base, cache, domainFinder, transformQueue));
    pipeline.addLast(new WebSocketHandler(webConfig, metrics, table));
  }
}
