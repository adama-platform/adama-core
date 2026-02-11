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

import ape.common.*;
import ape.common.rate.AsyncTokenLimiter;
import ape.web.assets.AssetFact;
import ape.web.assets.AssetStream;
import ape.web.assets.AssetSystem;
import ape.web.assets.AssetUploadBody;
import ape.web.assets.generate.QRCodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.cookie.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import ape.ErrorCodes;
import ape.runtime.data.Key;
import ape.runtime.natives.NtAsset;
import ape.runtime.sys.domains.Domain;
import ape.runtime.sys.domains.DomainFinder;
import ape.runtime.sys.web.KnownErrors;
import ape.web.assets.cache.CachedAsset;
import ape.web.assets.cache.WebHandlerAssetCache;
import ape.web.assets.transforms.Transform;
import ape.web.assets.transforms.TransformFactory;
import ape.web.assets.transforms.TransformQueue;
import ape.web.contracts.CertificateFinder;
import ape.web.contracts.HttpHandler;
import ape.web.contracts.ServiceBase;
import ape.web.contracts.ServiceConnection;
import ape.web.firewall.WebRequestShield;
import ape.web.io.ConnectionContext;
import ape.web.io.JsonRequest;
import ape.web.io.JsonResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.ssl.SslContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Netty HTTP request handler with comprehensive routing for the Adama platform.
 * Handles internal routes (/~upload, /~health, /libadama.js, cookie management),
 * domain-based routing to documents via HttpHandler, asset delivery with caching
 * and transforms, CORS support, and response streaming. Integrates with
 * WebRequestShield for security filtering.
 */
public class WebHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(WebHandler.class);
  private static final Logger CLIENT_LOG = LoggerFactory.getLogger("client");

  private static final byte[] EMPTY_RESPONSE = new byte[0];
  private static final byte[] OK_RESPONSE = "OK".getBytes(StandardCharsets.UTF_8);
  private static final byte[] NOT_FOUND_RESPONSE = "<html><head><title>Bad Request; Not Found</title></head><body>Sorry, the request was not found within our handler space.</body></html>".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_UPLOAD_FAILURE = "<html><head><title>Bad Request; Internal Error Uploading</title></head><body>Sorry, the upload failed.</body></html>".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_TRANSFORM_FAILURE = "<html><head><title>Bad Request; asset content type not understood</title></head><body>Sorry.</body></html>".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_UPLOAD_INCOMPLETE_FIELDS = "<html><head><title>Bad Request; Incomplete</title></head><body>Sorry, the post request was incomplete.</body></html>".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_PUT_NO_IDENTITY = "missing bearer token".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_PUT_MISSING_SPACE = "missing 'space' query parameter".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_PUT_MISSING_KEY = "missing 'key' query parameter".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_PUT_MISSING_FILENAME = "missing 'filename' query parameter".getBytes(StandardCharsets.UTF_8);
  private static final byte[] ASSET_PUT_MD5_MISMATCH = "content-md5 mismatch".getBytes(StandardCharsets.UTF_8);
  private static final byte[] COOKIE_SET_FAILURE = "<html><head><title>Bad Request; Failed to set cookie</title></head><body>Sorry, the request was incomplete.</body></html>".getBytes(StandardCharsets.UTF_8);
  private static final byte[] COOKIE_TOO_LARGE = "cookie value too large".getBytes(StandardCharsets.UTF_8);

  private static final int MAX_COOKIE_VALUE_LENGTH = 1024;
  private static final int MAX_TOTAL_COOKIE_SIZE = 8192;
  private static final byte[] JAR_FAILURE = "<html><head><title>Bad Request; Internal Error Access Jar</title></head><body>Sorry, the download failed.</body></html>".getBytes(StandardCharsets.UTF_8);

  private static final byte[] BAD_REQUEST = "bad request".getBytes(StandardCharsets.UTF_8);
  private static final byte[] METHOD_NOT_ALLOWED = "method not allowed".getBytes(StandardCharsets.UTF_8);
  private static final byte[] RATE_LIMITED = "rate limited".getBytes(StandardCharsets.UTF_8);

  private final WebConfig webConfig;
  private final WebMetrics metrics;
  private final ServiceBase serviceBase;
  private final HttpHandler httpHandler;
  private final AssetSystem assets;
  private final WebHandlerAssetCache cache;
  private static final ExecutorService jarThread = Executors.newSingleThreadExecutor();
  private final DomainFinder domainFinder;
  private final TransformQueue transformQueue;
  private final CertificateFinder certificateFinder;
  private final AsyncTokenLimiter onceRateLimiter;

  public WebHandler(WebConfig webConfig, WebMetrics metrics, ServiceBase serviceBase, WebHandlerAssetCache cache, DomainFinder incomingDomainFinder, TransformQueue transformQueue, CertificateFinder certificateFinder, AsyncTokenLimiter onceRateLimiter) {
    this.webConfig = webConfig;
    this.metrics = metrics;
    this.serviceBase = serviceBase;
    this.httpHandler = serviceBase.http();
    this.assets = serviceBase.assets();
    this.cache = cache;
    this.transformQueue = transformQueue;
    this.certificateFinder = certificateFinder;
    this.onceRateLimiter = onceRateLimiter;
    this.domainFinder = (domain, callback) -> {
      for (String suffix : webConfig.globalDomains) {
        if (domain.endsWith("." + suffix)) {
          String space = domain.substring(0, domain.length() - suffix.length() - 1);
          callback.success(new Domain(domain, -0, space, "default-document", null, false, null, null, 0, false));
          return;
        }
      }
      incomingDomainFinder.find(domain, callback);
    };
  }

  /** apply standard security headers to every HTTP response */
  static void addSecurityHeaders(HttpResponse res) {
    res.headers().set("X-Content-Type-Options", "nosniff");
    res.headers().set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    res.headers().set("Referrer-Policy", "no-referrer");
    res.headers().set("X-Frame-Options", "SAMEORIGIN");
    String contentType = res.headers().get(HttpHeaderNames.CONTENT_TYPE);
    if ("image/svg+xml".equals(contentType)) {
      res.headers().set("Content-Security-Policy", "script-src 'none'; frame-ancestors 'self'");
    } else {
      res.headers().set("Content-Security-Policy", "frame-ancestors 'self'");
    }
  }

  private static void sendWithKeepAlive(final WebConfig webConfig, final ChannelHandlerContext ctx, final FullHttpRequest req, final FullHttpResponse res) {
    addSecurityHeaders(res);
    final var responseStatus = res.status();
    final var keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
    HttpUtil.setKeepAlive(res, keepAlive);
    final var future = ctx.writeAndFlush(res);
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
  }

  /** internal: copy the origin to access control when allowed */
  private void transferCors(final HttpResponse res, final FullHttpRequest req, boolean allow) {
    String origin = req.headers().get(HttpHeaderNames.ORIGIN);
    if (origin != null && allow) { // CORS support directly
      res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "OPTIONS,GET,PUT,POST,DELETE");
      res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, true);
      res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "content-type,authorization");
    }
  }

  /** send an immediate data result */
  private void sendImmediate(Runnable metric, FullHttpRequest req, final ChannelHandlerContext ctx, HttpResponseStatus status, byte[] content, String contentType, boolean cors) {
    metric.run();
    final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), status, Unpooled.wrappedBuffer(content));
    HttpUtil.setContentLength(res, content.length);
    res.headers().set(HttpHeaderNames.ACCEPT_RANGES, "none");
    if (contentType != null) {
      res.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }
    transferCors(res, req, cors);
    sendWithKeepAlive(webConfig, ctx, req, res);
  }

  private void redirect(Runnable metric, FullHttpRequest req, final ChannelHandlerContext ctx, HttpResponseStatus status, String location, boolean isDevBox, String identity) {
    metric.run();
    final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), status, Unpooled.wrappedBuffer(EMPTY_RESPONSE));
    HttpUtil.setContentLength(res, 0);
    res.headers().set(HttpHeaderNames.LOCATION, location);
    injectIdentity(identity, isDevBox, res);
    transferCors(res, req, true);
    sendWithKeepAlive(webConfig, ctx, req, res);
  }

  private AssetStream streamOf(FullHttpRequest req, final ChannelHandlerContext ctx, boolean cors, Integer cacheTimeSec) {
    final boolean keepalive = HttpUtil.isKeepAlive(req);

    return new AssetStream() {
      private boolean started = false;
      private String contentType = null;
      private Integer cacheTimeSeconds = cacheTimeSec;
      private String contentMd5;
      private long contentLength;

      @Override
      public void headers(long contentLength, String contentType, String md5) {
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentMd5 = md5;
      }

      @Override
      public void body(byte[] chunk, int offset, int length, boolean last) {
        if (!started && last) {
          byte[] content = Arrays.copyOfRange(chunk, offset, length);
          final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(content));
          setResponseHeaders(res);
          sendWithKeepAlive(webConfig, ctx, req, res);
        } else {
          if (!started) {
            DefaultHttpResponse res = new DefaultHttpResponse(req.protocolVersion(), HttpResponseStatus.OK);

            setResponseHeaders(res);
            HttpUtil.setKeepAlive(res, keepalive);
            ctx.write(res);
            started = true;
          }
          if (chunk.length == length && offset == 0) {
            ctx.write(new DefaultHttpContent(Unpooled.wrappedBuffer(chunk)));
          } else {
            ctx.write(new DefaultHttpContent(Unpooled.wrappedBuffer(Arrays.copyOfRange(chunk, offset, length))));
          }
          if (last) {
            final var future = ctx.writeAndFlush(new DefaultLastHttpContent());
            if (!keepalive) {
              future.addListener(ChannelFutureListener.CLOSE);
            }
          }
        }
      }

      private void setResponseHeaders(HttpResponse response) {
        if (this.contentLength < 0 && req.protocolVersion() == HttpVersion.HTTP_1_1) {
          response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        } else {
          if (this.contentLength >= 0) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
          }
        }
        if (contentMd5 != null) {
          response.headers().set(HttpHeaderNames.CONTENT_MD5, contentMd5);
        }
        if (contentType != null) {
          response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        }
        if (cacheTimeSeconds != null && cacheTimeSeconds > 0) {
          response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=" + cacheTimeSeconds);
        }

        response.headers().set(HttpHeaderNames.ACCEPT_RANGES, "none");
        addSecurityHeaders(response);
        transferCors(response, req, cors);
      }

      @Override
      public void failure(int code) {
        if (started) {
          ctx.close();
        } else {
          sendImmediate(metrics.webhandler_asset_failed, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ("Download asset failure:" + code).getBytes(StandardCharsets.UTF_8), "text/plain", false);
        }
      }
    };
  }

  /** handle a native asset */
  private void handleNtAsset(FullHttpRequest req, final ChannelHandlerContext ctx, Key key, NtAsset asset, String transform, boolean cors, Integer cacheTimeSeconds) {
    AssetStream response = streamOf(req, ctx, cors, cacheTimeSeconds);

    if (transform != null) {
      Transform how = TransformFactory.make(webConfig, asset.contentType, transform);
      if (how == null) {
        sendImmediate(metrics.webhandler_transform_failure_none_available, req, ctx, HttpResponseStatus.BAD_REQUEST, ASSET_TRANSFORM_FAILURE, "text/html; charset=UTF-8", true);
      } else {
        transformQueue.process(key, transform, how, asset, response);
      }
      return;
    }

    if (!WebHandlerAssetCache.canCache(asset)) {
      // we can't cache? sad face -> stream direct
      assets.request(key, asset, response);
      return;
    }

    // if the response fails, for any reason, force the stream out of the cache to try again
    AssetStream wrapResponseToEvict = new AssetStream() {
      @Override
      public void headers(long length, String contentType, String md5) {
        response.headers(length, contentType, md5);
      }

      @Override
      public void body(byte[] chunk, int offset, int length, boolean last) {
        response.body(chunk, offset, length, last);
      }

      @Override
      public void failure(int code) {
        response.failure(code);
        cache.failure(asset);
      }
    };

    // ask the cache for the cached asset
    cache.get(asset, new Callback<>() {
      @Override
      public void success(CachedAsset cachedAsset) {
        // attach the wrapped response to the asset
        AssetStream feed = cachedAsset.attachWhileInExecutor(wrapResponseToEvict);
        if (feed != null) {
          // pump the stream since this is the first requestor
          assets.request(key, asset, feed);
        }
      }

      @Override
      public void failure(ErrorCodeException ex) {
        response.failure(ex.code);
      }
    });
  }

  private void handleAssetUpload(final ChannelHandlerContext ctx, final FullHttpRequest req) {
    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(req);
    try {
      ArrayList<FileUpload> files = new ArrayList<>();
      String _identity = null;
      String space = null;
      String key = null;
      String _channel = null;
      String domain = null;
      HashMap<String, String> message_parts = new HashMap<>();
      for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
        switch (data.getHttpDataType()) {
          case Attribute:
            Attribute attribute = (Attribute) data;
            switch (attribute.getName()) {
              case "identity":
                _identity = attribute.getValue();
                break;
              case "space":
                space = attribute.getValue();
                break;
              case "key":
                key = attribute.getValue();
                break;
              case "channel":
                _channel = attribute.getValue();
                break;
              case "domain":
                domain = attribute.getValue();
                break;
              default: {
                if (attribute.getName().startsWith("message_") || attribute.getName().startsWith("message.")) {
                  message_parts.put(attribute.getName().substring(8), attribute.getValue());
                }
              }
            }
            break;
          case FileUpload:
            files.add(((FileUpload) data));
            break;
          default:
            break;
        }
      }
      final String channel = _channel;
      final ConnectionContext context = ConnectionContextFactory.of(ctx, req.headers(), webConfig.useXForwardedFor);
      final String identity = context.identityOf(_identity);
      if (identity != null && domain != null) {
        domainFinder.find(domain, new Callback<Domain>() {
          @Override
          public void success(final Domain domainValue) {
            if (domainValue == null) {
              failure(new ErrorCodeException(ErrorCodes.DOMAIN_LOOKUP_WEB_NULL_FAILURE));
              return;
            }
            if (domainValue.space == null || domainValue.key == null) {
              failure(new ErrorCodeException(ErrorCodes.DOMAIN_LOOKUP_WEB_NO_KEY_FAILURE));
              return;
            }
            Key uploadKey = new Key(domainValue.space, domainValue.key);
            ctx.executor().execute(() -> {
              try {
                finishAssetUpload(context, identity, uploadKey, channel, files, message_parts, ctx, req, decoder);
              } catch (Exception ex) {
                LOG.error("failed-upload-for-domain", ex);
                sendImmediate(metrics.webhandler_upload_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ASSET_UPLOAD_FAILURE, "text/html; charset=UTF-8", true);
                decoder.destroy();}
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {
            LOG.error("failed-upload-ex:" + ex.code);
            sendImmediate(metrics.webhandler_upload_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ASSET_UPLOAD_FAILURE, "text/html; charset=UTF-8", true);
            decoder.destroy();
          }
        });
      } else if (identity != null && space != null && key != null) {
        Key uploadKey = new Key(space, key);
        finishAssetUpload(context, identity, uploadKey, channel, files, message_parts, ctx, req, decoder);
      } else {
        sendImmediate(metrics.webhandler_upload_asset_failure, req, ctx, HttpResponseStatus.BAD_REQUEST, ASSET_UPLOAD_INCOMPLETE_FIELDS, "text/html; charset=UTF-8", true);
        decoder.destroy();
      }
    } catch (Exception ex) {
      LOG.error("failed-upload", ex);
      sendImmediate(metrics.webhandler_upload_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ASSET_UPLOAD_FAILURE, "text/html; charset=UTF-8", true);
      decoder.destroy();
    }
  }

  private void finishAssetUpload(ConnectionContext context, String identity, Key uploadKey, String channel, ArrayList<FileUpload> files, HashMap<String, String> message_parts, final ChannelHandlerContext ctx, final FullHttpRequest req, HttpPostRequestDecoder decoder) throws IOException {
    final MultiVoidCallbackLatch latch = new MultiVoidCallbackLatch(metrics.web_asset_upload.wrap(new Callback<Void>() {
      @Override
      public void success(Void value) {
        sendImmediate(metrics.webhandler_upload_asset_failure, req, ctx, HttpResponseStatus.OK, EMPTY_RESPONSE, "text/html; charset=UTF-8", true);
        decoder.destroy();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        sendImmediate(metrics.webhandler_upload_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ASSET_UPLOAD_FAILURE, "text/html; charset=UTF-8", true);
        decoder.destroy();
      }
    }), files.size(), ErrorCodes.WEB_FAILED_ASSET_UPLOAD_ALL);
    for (FileUpload upload : files) {
      AssetUploadBody body = new AssetUploadBody() {
        @Override
        public File getFileIfExists() {
          try {
            return upload.getFile();
          } catch (Exception ex) {
            return null;
          }
        }

        @Override
        public byte[] getBytes() {
          try {
            return upload.get();
          } catch (Exception ex) {
            return null;
          }
        }
      };
      AssetFact fact = AssetFact.of(body);
      NtAsset asset = new NtAsset(ProtectedUUID.generate(), upload.getFilename(), upload.getContentType(), fact.size, fact.md5, fact.sha384);
      final String message;
      if (channel != null) {
        ObjectNode messageNode = Json.newJsonObject();
        messageNode.put("asset_id", asset.id);
        for (Map.Entry<String, String> entry : message_parts.entrySet()) {
          messageNode.put(entry.getKey(), entry.getValue());
        }
        message = messageNode.toString();
      } else {
        message = null;
      }
      final String channelFinal = channel;
      final String identityFinal = identity;
      assets.upload(uploadKey, asset, body, new Callback<>() {
        @Override
        public void success(Void value) {
          assets.attach(identityFinal, context, uploadKey, asset, channelFinal, message, new Callback<Integer>() {
            @Override
            public void success(Integer value) {
              latch.success();
            }

            @Override
            public void failure(ErrorCodeException ex) {
              latch.failure();
              LOG.error("failed-asset-attach:" + ex.code);
            }
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          latch.failure();
          LOG.error("failed-asset-post-upload:" + ex.code);
        }
      });
    }
  }

  private void handleAssetPut(final ChannelHandlerContext ctx, final FullHttpRequest req) {
    try {
      // extract Bearer token identity
      final ConnectionContext context = ConnectionContextFactory.of(ctx, req.headers(), webConfig.useXForwardedFor);
      String bearerIdentity = null;
      String authHeader = req.headers().get(HttpHeaderNames.AUTHORIZATION);
      if (authHeader != null) {
        String stripped = authHeader.stripLeading();
        if (stripped.startsWith("Bearer ")) {
          bearerIdentity = stripped.substring(7).trim();
        }
      }
      if (bearerIdentity == null || bearerIdentity.isEmpty()) {
        sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.UNAUTHORIZED, ASSET_PUT_NO_IDENTITY, "text/plain", true);
        return;
      }
      final String identity = bearerIdentity;

      // parse query parameters
      QueryStringDecoder qsd = new QueryStringDecoder(req.uri());
      String space = getQueryParam(qsd, "space");
      String key = getQueryParam(qsd, "key");
      String filename = getQueryParam(qsd, "filename");
      String contentType = req.headers().get(HttpHeaderNames.CONTENT_TYPE);
      String channel = getQueryParam(qsd, "channel");
      HashMap<String, String> message_parts = new HashMap<>();
      for (Map.Entry<String, java.util.List<String>> entry : qsd.parameters().entrySet()) {
        if (entry.getKey().startsWith("message_") || entry.getKey().startsWith("message.")) {
          message_parts.put(entry.getKey().substring(8), entry.getValue().get(0));
        }
      }

      if (space == null || space.isEmpty()) {
        sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.BAD_REQUEST, ASSET_PUT_MISSING_SPACE, "text/plain", true);
        return;
      }
      if (key == null || key.isEmpty()) {
        sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.BAD_REQUEST, ASSET_PUT_MISSING_KEY, "text/plain", true);
        return;
      }
      if (filename == null || filename.isEmpty()) {
        sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.BAD_REQUEST, ASSET_PUT_MISSING_FILENAME, "text/plain", true);
        return;
      }
      if (contentType == null || contentType.isEmpty()) {
        contentType = "application/octet-stream";
      }

      // read body bytes
      byte[] bodyBytes = new byte[req.content().readableBytes()];
      req.content().readBytes(bodyBytes);

      // compute asset facts
      AssetUploadBody body = AssetUploadBody.WRAP(bodyBytes);
      AssetFact fact = AssetFact.of(body);

      // optional Content-MD5 verification
      String clientMd5 = req.headers().get(HttpHeaderNames.CONTENT_MD5);
      if (clientMd5 != null && !clientMd5.isEmpty()) {
        if (!clientMd5.equals(fact.md5)) {
          metrics.webhandler_put_asset_md5_mismatch.run();
          sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.BAD_REQUEST, ASSET_PUT_MD5_MISMATCH, "text/plain", true);
          return;
        }
      }

      NtAsset asset = new NtAsset(ProtectedUUID.generate(), filename, contentType, fact.size, fact.md5, fact.sha384);
      Key uploadKey = new Key(space, key);

      // build channel message
      final String message;
      if (channel != null && !channel.isEmpty()) {
        ObjectNode messageNode = Json.newJsonObject();
        messageNode.put("asset_id", asset.id);
        for (Map.Entry<String, String> entry : message_parts.entrySet()) {
          messageNode.put(entry.getKey(), entry.getValue());
        }
        message = messageNode.toString();
      } else {
        message = null;
      }
      final String channelFinal = (channel != null && !channel.isEmpty()) ? channel : null;

      metrics.webhandler_put_asset.run();
      assets.upload(uploadKey, asset, body, new Callback<>() {
        @Override
        public void success(Void value) {
          assets.attach(identity, context, uploadKey, asset, channelFinal, message, new Callback<Integer>() {
            @Override
            public void success(Integer value) {
              sendImmediate(metrics.webhandler_put_asset, req, ctx, HttpResponseStatus.OK, EMPTY_RESPONSE, "text/plain", true);
            }

            @Override
            public void failure(ErrorCodeException ex) {
              LOG.error("failed-put-asset-attach:" + ex.code);
              sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ASSET_UPLOAD_FAILURE, "text/html; charset=UTF-8", true);
            }
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          LOG.error("failed-put-asset-upload:" + ex.code);
          sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ASSET_UPLOAD_FAILURE, "text/html; charset=UTF-8", true);
        }
      });
    } catch (Exception ex) {
      LOG.error("failed-put-asset", ex);
      sendImmediate(metrics.webhandler_put_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ASSET_UPLOAD_FAILURE, "text/html; charset=UTF-8", true);
    }
  }

  private static String getQueryParam(QueryStringDecoder qsd, String name) {
    java.util.List<String> values = qsd.parameters().get(name);
    if (values != null && !values.isEmpty()) {
      return values.get(0);
    }
    return null;
  }

  private static final String CACHED_ADAMA_JAR_MD5 = hashAdamaJar();

  private static String hashAdamaJar() {
    try {
      File adamaJar = new File("adama.jar");
      FileInputStream input = new FileInputStream(adamaJar);
      MessageDigest md5 = Hashing.md5();
      byte[] buffer = new byte[8192];
      int rd;
      while ((rd = input.read(buffer)) >= 0) {
        md5.update(buffer, 0, rd);
      }
      return Hashing.finishAndEncode(md5);
    } catch (Exception ex) {
      return null;
    }
  }

  private void sendJar(final ChannelHandlerContext ctx, final FullHttpRequest req) {
    jarThread.execute(new Runnable() {
      @Override
      public void run() {
        try {
          File adamaJar = new File("adama.jar");
          FileInputStream input = new FileInputStream(adamaJar);
          try {
            boolean keepalive = HttpUtil.isKeepAlive(req);
            HttpResponse res = new DefaultHttpResponse(req.protocolVersion(), HttpResponseStatus.OK);
            if (CACHED_ADAMA_JAR_MD5 != null) {
              res.headers().set(HttpHeaderNames.CONTENT_MD5, CACHED_ADAMA_JAR_MD5);
            }
            res.headers().set(HttpHeaderNames.CONTENT_LENGTH, adamaJar.length());
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/java-archive");
            HttpUtil.setKeepAlive(res, keepalive);
            ctx.write(res);
            long remaining = adamaJar.length();
            byte[] buffer = new byte[8192];
            int rd;
            while ((rd = input.read(buffer)) >= 0) {
              remaining -= rd;
              if (rd == buffer.length) {
                ctx.write(new DefaultHttpContent(Unpooled.wrappedBuffer(buffer)));
              } else {
                ctx.write(new DefaultHttpContent(Unpooled.wrappedBuffer(buffer, 0, rd)));
              }
              if (remaining <= 0) {
                final var future = ctx.writeAndFlush(new DefaultLastHttpContent());
                if (!keepalive) {
                  future.addListener(ChannelFutureListener.CLOSE);
                }
              }
              buffer = new byte[8192];
            }
          } finally {
            input.close();
          }
        } catch (Exception ex) {
          LOG.error("failed-sending-jar", ex);
          sendImmediate(metrics.webhandler_upload_asset_failure, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, JAR_FAILURE, "text/html; charset=UTF-8", true);
        }
      }
    });
  }

  private void okOpen(final ChannelHandlerContext ctx, final FullHttpRequest req) {
    final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(EMPTY_RESPONSE));
    HttpUtil.setContentLength(res, 0);
    transferCors(res, req, true);
    res.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
    sendWithKeepAlive(webConfig, ctx, req, res);
  }

  /**
   * Sanitize a string for safe logging. Balances reasonableness (allowing enough characters for
   * useful log messages including URLs, JSON fragments, stack traces) with security (stripping
   * control characters, null bytes, and other injection vectors). Newlines are converted to spaces.
   */
  static String logSanitize(String x) {
    if (x == null) {
      return "";
    }
    // cap length to prevent unbounded log entries
    if (x.length() > 4096) {
      x = x.substring(0, 4096);
    }
    PrimitiveIterator.OfInt it = x.codePoints().iterator();
    StringBuilder result = new StringBuilder();
    while (it.hasNext()) {
      int codepoint = it.nextInt();
      if (codepoint == '\n' || codepoint == '\r') {
        result.append(' ');
      } else if (Character.isLetterOrDigit(codepoint) || Character.isWhitespace(codepoint)
          || ":./\\-_=,;()[]@#+!?&~\"'{}".indexOf(codepoint) >= 0) {
        result.append(Character.toString(codepoint));
      }
    }
    return result.toString();
  }

  /** Sanitize the log name: only allow lowercase alphanumeric, dashes, and dots. Cap length. */
  static String sanitizeLogName(String name) {
    if (name == null || name.isEmpty()) {
      return "unknown";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < name.length() && sb.length() < 64; i++) {
      char c = name.charAt(i);
      if (Character.isLetterOrDigit(c) || c == '-' || c == '.') {
        sb.append(Character.toLowerCase(c));
      }
    }
    if (sb.length() == 0) {
      return "unknown";
    }
    return sb.toString();
  }

  /**
   * Devbox mode is intended for personal development machines that are isolated from real traffic.
   * It relaxes several security controls (cookie Secure flag, CORS origin validation, SameSite policy)
   * to simplify local development. It should NEVER be reachable from the public internet.
   */
  static boolean computeIsDevBox(String host) {
    boolean isLocalHost = "localhost".equals(host) || host.startsWith("localhost:");
    boolean is127 = "127.0.0.1".equals(host) || host.startsWith("127.0.0.1:");
    return isLocalHost || is127;
  }

  /** extract hostname from a URL like "https://example.com" or "http://example.com:8080"; returns null for relative URLs or unparseable input */
  static String extractHostname(String url) {
    try {
      URI uri = new URI(url);
      return uri.getHost();
    } catch (Exception ex) {
      return null;
    }
  }

  private boolean handleInternal(final String host, boolean isDevBox, final ChannelHandlerContext ctx, final FullHttpRequest req) {
    if (webConfig.healthCheckPath.equals(req.uri())) { // health checks
      sendImmediate(metrics.webhandler_healthcheck, req, ctx, HttpResponseStatus.OK, ("HEALTHY:" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8), "text/text; charset=UTF-8", true);
      return true;
    } else if (webConfig.deepHealthCheckPath.equals(req.uri())) { // deep health check
      httpHandler.handleDeepHealth(new Callback<String>() {
        @Override
        public void success(String report) {
          sendImmediate(metrics.webhandler_deephealthcheck, req, ctx, HttpResponseStatus.OK, report.getBytes(StandardCharsets.UTF_8), "text/html", false);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          sendImmediate(metrics.webhandler_deephealthcheck, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, ("Deep health failed!").getBytes(StandardCharsets.UTF_8), "text/html", false);
        }
      });
      return true;
    } else if (req.uri().equals("/~adama/once") && req.method() == HttpMethod.OPTIONS) {
      okOpen(ctx, req);
      return true;
    } else if (req.uri().equals("/~adama/once") && req.method() == HttpMethod.PUT) {
      try {
        byte[] memory = new byte[req.content().readableBytes()];
        req.content().readBytes(memory);
        String result = new String(memory, StandardCharsets.UTF_8);
        ObjectNode request = Json.parseJsonObject(result);
        request.put("id", 0); // inject a faux-id
        String method = request.get("method").textValue();
        if (OnceFilter.allowed(method)) {
          onceRateLimiter.execute(webConfig.onceRateLimitMaxAttempts, webConfig.onceRateLimitDelay, webConfig.onceRateLimitJitter, new Callback<Void>() {
            @Override
            public void success(Void value) {
              ctx.executor().execute(() -> {
                final ConnectionContext context = ConnectionContextFactory.of(ctx, req.headers(), webConfig.useXForwardedFor);
                ServiceConnection connection = serviceBase.establishServiceConnection(context);
                connection.execute(new JsonRequest(request, context), new JsonResponder() {
                  @Override
                  public void stream(String json) {
                    // we don't hold it open as we don't allow streams
                    finish(json);
                  }

                  @Override
                  public void finish(String json) {
                    sendImmediate(metrics.webhandler_success_once, req, ctx, HttpResponseStatus.OK, json.getBytes(StandardCharsets.UTF_8), "application/json", true);
                    connection.kill();
                  }

                  @Override
                  public void error(ErrorCodeException ex) {
                    sendImmediate(metrics.webhandler_failed_once_exception, req, ctx, HttpResponseStatus.BAD_REQUEST, ("" + ex.code).getBytes(StandardCharsets.UTF_8), "text/plain", true);
                    connection.kill();
                  }
                });
              });
            }

            @Override
            public void failure(ErrorCodeException ex) {
              ctx.executor().execute(() -> {
                metrics.webhandler_failed_once_rate_limited.run();
                sendImmediate(metrics.webhandler_failed_once_rate_limited, req, ctx, HttpResponseStatus.TOO_MANY_REQUESTS, RATE_LIMITED, "text/plain", true);
              });
            }
          });
        } else {
          sendImmediate(metrics.webhandler_failed_once_not_allowed, req, ctx, HttpResponseStatus.BAD_REQUEST, METHOD_NOT_ALLOWED, "text/plain", true);
        }
      } catch (Exception ex) {
        sendImmediate(metrics.webhandler_failed_once_abort, req, ctx, HttpResponseStatus.BAD_REQUEST, BAD_REQUEST, "text/plain", true);
      }
      return true;
    } else if (req.uri().startsWith("/~upload") && req.method() == HttpMethod.POST) {
      handleAssetUpload(ctx, req);
      return true;
    } else if (req.uri().startsWith("/~put") && req.method() == HttpMethod.PUT) {
      handleAssetPut(ctx, req);
      return true;
    } else if (req.uri().startsWith("/~put") && req.method() == HttpMethod.OPTIONS) {
      okOpen(ctx, req);
      return true;
    } else if (req.uri().equalsIgnoreCase("/adama.jar") && host.endsWith(webConfig.adamaJarDomain)) {
      sendJar(ctx, req);
      return true;
    } else if (req.uri().equals("/~version")) { // get the version of the platform
      sendImmediate(metrics.webhandler_version, req, ctx, HttpResponseStatus.OK, Platform.VERSION.getBytes(), "text/plain", true);
      return true;
    } else if (req.uri().startsWith("/libadama.js")) { // in-memory JavaScript library for the client
      if (webConfig.beta) {
        sendImmediate(metrics.webhandler_client_download, req, ctx, HttpResponseStatus.OK, JavaScriptClient.BETA_ADAMA_JS_CLIENT_BYTES, "text/javascript; charset=UTF-8", true);
      } else {
        sendImmediate(metrics.webhandler_client_download, req, ctx, HttpResponseStatus.OK, JavaScriptClient.ADAMA_JS_CLIENT_BYTES, "text/javascript; charset=UTF-8", true);
      }
      return true;
    } else if (req.uri().startsWith("/libadama-worker.js")) { // in-memory JavaScript library for the client
      if (webConfig.beta) {
        sendImmediate(metrics.webhandler_worker_download, req, ctx, HttpResponseStatus.OK, JavaScriptClient.BETA_ADAMA_WORKER_JS_CLIENT_BYTES, "text/javascript; charset=UTF-8", true);
      } else {
        sendImmediate(metrics.webhandler_worker_download, req, ctx, HttpResponseStatus.OK, JavaScriptClient.ADAMA_WORKER_JS_CLIENT_BYTES, "text/javascript; charset=UTF-8", true);
      }
      return true;
    } else if ((req.uri().startsWith("/~lg/") || req.uri().startsWith("/~bm/")) && req.method() == HttpMethod.OPTIONS) {
      okOpen(ctx, req);
      return true;
    } else if (req.uri().startsWith("/~lg/") && req.method() == HttpMethod.PUT) {
      // This handler is for logs from the web client or other clients.
      // Log data is untrusted, so both the log name and content are sanitized.
      String logName = sanitizeLogName(req.uri().substring(5));
      byte[] memory = new byte[req.content().readableBytes()];
      req.content().readBytes(memory);
      String result = new String(memory, StandardCharsets.UTF_8);
      CLIENT_LOG.debug(logName + ":" + logSanitize(result));
      okOpen(ctx, req);
      return true;
    } else if (req.uri().startsWith("/~bm/") && req.uri().length() >= 6) { // bump a metric
      String metricName = req.uri().substring(5);
      Runnable counter = metrics.client_metrics.get(metricName);
      if (counter != null) {
        counter.run();
      }
      okOpen(ctx, req);
      return true;
    } else if (req.uri().startsWith("/~stash/") && (req.method() == HttpMethod.OPTIONS)) {
      String origin = req.headers().get(HttpHeaderNames.ORIGIN);
      if (origin == null) { // CORS support directly
        sendImmediate(metrics.webhandler_failed_cookie_set, req, ctx, HttpResponseStatus.BAD_REQUEST, COOKIE_SET_FAILURE, "text/html; charset=UTF-8", true);
        return true;
      }
      final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(EMPTY_RESPONSE));
      res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, true);
      res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "PUT");
      res.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
      sendWithKeepAlive(webConfig, ctx, req, res);
      return true;
    } else if (req.uri().startsWith("/~stash/") && (req.method() == HttpMethod.PUT)) {
      try {
        byte[] memory = new byte[req.content().readableBytes()];
        req.content().readBytes(memory);
        ObjectNode body = Json.parseJsonObject(new String(memory, StandardCharsets.UTF_8));
        String name = body.get("name").textValue();
        String value = body.get("identity").textValue();
        int maxAge = body.get("max-age").intValue();
        if (value != null && value.length() > MAX_COOKIE_VALUE_LENGTH) {
          sendImmediate(metrics.webhandler_failed_cookie_set, req, ctx, HttpResponseStatus.BAD_REQUEST, COOKIE_TOO_LARGE, "text/plain", true);
          return true;
        }
        String origin = req.headers().get(HttpHeaderNames.ORIGIN);
        if (origin == null) { // CORS support directly
          sendImmediate(metrics.webhandler_failed_cookie_set, req, ctx, HttpResponseStatus.BAD_REQUEST, COOKIE_SET_FAILURE, "text/html; charset=UTF-8", true);
          return true;
        }
        final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(OK_RESPONSE));
        res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        res.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, true);
        res.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
        DefaultCookie cookie = new DefaultCookie("id_" + name, value);
        cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        if (!isDevBox) {
          cookie.setSecure(true);
        }
        cookie.setPath("/");
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        res.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
        sendWithKeepAlive(webConfig, ctx, req, res);
      } catch (Exception ex) {
        sendImmediate(metrics.webhandler_failed_cookie_set, req, ctx, HttpResponseStatus.BAD_REQUEST, COOKIE_SET_FAILURE, "text/html; charset=UTF-8", true);
      }
      return true;
    }
    return false;
  }

  private void handleQRJsonTransform(HttpHandler.HttpResult httpResult, final ChannelHandlerContext ctx, final FullHttpRequest req, boolean isDevBox) {
    byte[] body = null;
    try {
      body = QRCodeFactory.generate(httpResult.location, httpResult.size);
    } catch (Exception e) {
      metrics.web_qr_code_creation_failure.run();
    }
    if (body != null) {
      final HttpResponseStatus status = HttpResponseStatus.valueOf(200);
      final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), status, Unpooled.wrappedBuffer(body));
      HttpUtil.setContentLength(res, body.length);
      res.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/png");
      fullTransferSend(httpResult, ctx, req, res, isDevBox);
    } else {
      body = "Unable to produce QR code".getBytes(StandardCharsets.UTF_8);
      final HttpResponseStatus status = HttpResponseStatus.valueOf(400);
      final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), status, Unpooled.wrappedBuffer(body));
      HttpUtil.setContentLength(res, body.length);
      res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
    }
  }

  private void handleHttpResult(final String host, final boolean isDevBox, HttpHandler.HttpResult httpResultIncoming, final ChannelHandlerContext ctx, final FullHttpRequest req) {
    HttpHandler.HttpResult httpResult = httpResultIncoming;
    if (httpResult == null) { // no response found
      sendImmediate(metrics.webhandler_notfound, req, ctx, HttpResponseStatus.NOT_FOUND, NOT_FOUND_RESPONSE, "text/html; charset=UTF-8", true);
      return;
    }

    if (httpResult.redirect) {
      redirect(metrics.webhandler_redirect, req, ctx, httpResult.status == 301 ? HttpResponseStatus.MOVED_PERMANENTLY : HttpResponseStatus.FOUND, httpResult.location, isDevBox, httpResult.identity);
      return;
    }

    if (httpResult.asset != null && httpResult.space != null && httpResult.key != null) {
      handleNtAsset(req, ctx, new Key(httpResult.space, httpResult.key), httpResult.asset, httpResult.transform, httpResult.cors, httpResult.cacheTimeSeconds);
      return;
    }

    // otherwise, send the body
    metrics.webhandler_found.run();

    if (httpResult.contentType != null && httpResult.contentType.equals("internal/qr-code")) {
      handleQRJsonTransform(httpResult, ctx, req, isDevBox);
      return;
    }

    byte[] body = httpResult.body != null ? httpResult.body : EMPTY_RESPONSE;
    final HttpResponseStatus status = HttpResponseStatus.valueOf(httpResult.status);
    final FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), status, Unpooled.wrappedBuffer(body));
    HttpUtil.setContentLength(res, body.length);
    if (httpResult.contentType.length() > 0) {
      res.headers().set(HttpHeaderNames.CONTENT_TYPE, httpResult.contentType);
    }
    fullTransferSend(httpResult, ctx, req, res, isDevBox);
  }

  private void fullTransferSend(HttpHandler.HttpResult httpResult, final ChannelHandlerContext ctx, final FullHttpRequest req, final FullHttpResponse res, boolean isDevBox) {
    if (httpResult.cacheTimeSeconds != null && httpResult.cacheTimeSeconds > 0) {
      res.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=" + httpResult.cacheTimeSeconds);
    }
    if (httpResult.headers != null) {
      for (Map.Entry<String, String> header : httpResult.headers.entrySet()) {
        if (!webConfig.headerBlacklist.contains(header.getKey().toLowerCase(Locale.ROOT))) {
          res.headers().set(header.getKey(), header.getValue());
        }
      }
    }
    injectIdentity(httpResult.identity, isDevBox, res);
    transferCors(res, req, httpResult.cors);
    sendWithKeepAlive(webConfig, ctx, req, res);
  }

  private void injectIdentity(String identity, boolean isDevBox, FullHttpResponse res) {
    if (identity != null) {
      if (identity.length() > MAX_COOKIE_VALUE_LENGTH && !identity.equalsIgnoreCase("clear")) {
        return;
      }
      DefaultCookie cookie;
      if (identity.equalsIgnoreCase("clear")) {
        cookie = new DefaultCookie("id_default", "");
        cookie.setMaxAge(0);
      } else {
        cookie = new DefaultCookie("id_default", identity);
        cookie.setMaxAge(60 * 60 * 24 * 365);
      }
      cookie.setSameSite(CookieHeaderNames.SameSite.Lax);
      cookie.setHttpOnly(true);
      if (!isDevBox) {
        cookie.setSecure(true);
      }
      cookie.setPath("/");
      res.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
    // Step 1: Quick reject anything the shield doesn't like
    if (WebRequestShield.block(req.uri())) {
      sendImmediate(metrics.webhandler_firewall, req, ctx, HttpResponseStatus.GONE, EMPTY_RESPONSE, null, false);
      return;
    }

    String hostTemp = req.headers().get(HttpHeaderNames.HOST);
    if (hostTemp == null) {
      hostTemp = "";
    }
    final String host = hostTemp;
    final boolean isDevBox = computeIsDevBox(host);

    // Step 2: Validate CORS origin via CertificateFinder
    String origin = req.headers().get(HttpHeaderNames.ORIGIN);
    if (origin != null && !isDevBox) {
      String hostname = extractHostname(origin);
      if (hostname == null) {
        req.headers().remove(HttpHeaderNames.ORIGIN);
        metrics.webhandler_cors_origin_blocked.run();
        processRequest(host, isDevBox, ctx, req);
        return;
      }
      if (webConfig.specialDomains.contains(hostname)) {
        processRequest(host, isDevBox, ctx, req);
        return;
      }
      certificateFinder.fetch(hostname, new Callback<SslContext>() {
        @Override
        public void success(SslContext value) {
          ctx.executor().execute(() -> processRequest(host, isDevBox, ctx, req));
        }

        @Override
        public void failure(ErrorCodeException ex) {
          ctx.executor().execute(() -> {
            req.headers().remove(HttpHeaderNames.ORIGIN);
            metrics.webhandler_cors_origin_blocked.run();
            processRequest(host, isDevBox, ctx, req);
          });
        }
      });
    } else {
      processRequest(host, isDevBox, ctx, req);
    }
  }

  private void processRequest(final String host, final boolean isDevBox, final ChannelHandlerContext ctx, final FullHttpRequest req) {
    // Handle internal routing for Adama only stuff
    if (handleInternal(host, isDevBox, ctx, req)) {
      return;
    }

    // Handle the result from the web request
    Callback<HttpHandler.HttpResult> callback = new Callback<>() {
      @Override
      public void success(HttpHandler.HttpResult value) {
        ctx.executor().execute(() -> {
          handleHttpResult(host, isDevBox, value, ctx, req);
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        if (ex.code == ErrorCodes.FRONTEND_IP_DONT_RESOLVE || ex.code == ErrorCodes.FRONTEND_NO_DOMAIN_MAPPING) {
          metrics.bad_traffic.run();
        } else {
          LOG.error("failed-web-handler:" + ex.getMessage());
        }
        handleHttpResult(host, isDevBox, new HttpHandler.HttpResult(KnownErrors.inferHttpStatusCodeFrom(ex.code), "text/html", ("error:" + ex.code).getBytes(StandardCharsets.UTF_8), true), ctx, req);
      }
    };

    // Parse the request and then route to the appropriate handler
    try {
      AdamaWebRequest wta = new AdamaWebRequest(req, ctx, webConfig.useXForwardedFor);
      HttpHandler.Method hhmethod = HttpHandler.Method.GET;
      final ConnectionContext context = ConnectionContextFactory.of(ctx, req.headers(), webConfig.useXForwardedFor);
      if (req.method() == HttpMethod.OPTIONS) {
        metrics.webhandler_options.run();
        hhmethod = HttpHandler.Method.OPTIONS;
      } else if (req.method() == HttpMethod.DELETE) {
        metrics.webhandler_delete.run();
        hhmethod = HttpHandler.Method.DELETE;
      } else if (req.method() == HttpMethod.POST || req.method() == HttpMethod.PUT) {
        metrics.webhandler_post.run();
        hhmethod = HttpHandler.Method.PUT;
      } else {
        metrics.webhandler_get.run();
        hhmethod = HttpHandler.Method.GET;
      }
      httpHandler.handle(context, hhmethod, wta.identity, wta.uri, wta.headers, wta.parameters, wta.body, callback);
    } catch (Exception ex) {
      LOG.error("failure-to-build-wta:", ex);
      sendImmediate(metrics.webhandler_wta_crash, req, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, EMPTY_RESPONSE, null, true);
    }
  }
}
