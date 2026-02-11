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
package ape.web.contracts;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Callback;
import ape.runtime.natives.NtAsset;
import ape.web.io.ConnectionContext;

import java.util.TreeMap;

/**
 * HTTP request handler interface for routing requests to documents and spaces.
 * Implementations resolve domain/URI combinations to HttpResult responses which
 * can be body content, asset references, or redirects. Also provides deep health
 * check endpoint for infrastructure monitoring.
 */
public interface HttpHandler {

  public enum Method {
    OPTIONS,
    GET,
    DELETE,
    PUT
  };


  HttpHandler NULL = new HttpHandler() {
    @Override
    public void handle(ConnectionContext context, Method method, String identity, String uri, TreeMap<String, String> headers, String parametersJson, String body, Callback<HttpResult> callback) {
      callback.success(null);
    }

    @Override
    public void handleDeepHealth(Callback<String> callback) {
      callback.success("NULL");
    }
  };

  void handle(ConnectionContext context, Method method, String identity, String uri, TreeMap<String, String> headers, String parametersJson, String body, Callback<HttpResult> callback);

  void handleDeepHealth(Callback<String> callback);

  class HttpResultBuilder {
    public int status;
    public String contentType;
    public byte[] body;
    public String space;
    public String key;
    public NtAsset asset;
    public String transform;
    public boolean cors;
    public boolean redirect;
    public String location;
    public Integer cacheTimeSeconds;
    public TreeMap<String, String> headers;
    public String identity;
    public int size;

    public HttpResultBuilder() {
      this.status = 500;
      this.contentType = null;
      this.body = null;
      this.space = null;
      this.key = null;
      this.asset = null;
      this.transform = null;
      this.cors = false;
      this.redirect = false;
      this.location = null;
      this.cacheTimeSeconds = null;
      this.headers = null;
      this.identity = null;
      this.size = -1;
    }
  }

  /** The concrete result of handling a request; */
  class HttpResult {
    public final int status;
    public final String contentType;
    public final byte[] body;
    public final String space;
    public final String key;
    public final NtAsset asset;
    public final String transform;
    public final boolean cors;
    public final boolean redirect;
    public final String location;
    public final Integer cacheTimeSeconds;
    public final TreeMap<String, String> headers;
    public final String identity;
    public final int size;

    public HttpResult(HttpResultBuilder builder) {
      this.status = builder.status;
      this.contentType = builder.contentType;
      this.body = builder.body;
      this.space = builder.space;
      this.key = builder.key;
      this.asset = builder.asset;
      this.transform = builder.transform;
      this.cors = builder.cors;
      this.redirect = builder.redirect;
      this.location = builder.location;
      this.cacheTimeSeconds = builder.cacheTimeSeconds;
      this.headers = builder.headers;
      this.identity = builder.identity;
      this.size = builder.size;
    }

    public HttpResult(int status, String contentType, byte[] body, boolean cors) {
      this.status = status;
      this.contentType = contentType != null ? contentType : "";
      this.body = body;
      this.space = null;
      this.key = null;
      this.asset = null;
      this.transform = null;
      this.cors = cors;
      this.redirect = false;
      this.location = null;
      this.cacheTimeSeconds = null;
      this.headers = null;
      this.identity = null;
      this.size = -1;
    }

    public HttpResult(int status, String contentType, byte[] body, boolean cors, TreeMap<String, String> headers) {
      this.status = status;
      this.contentType = contentType != null ? contentType : "";
      this.body = body;
      this.space = null;
      this.key = null;
      this.asset = null;
      this.transform = null;
      this.cors = cors;
      this.redirect = false;
      this.location = null;
      this.cacheTimeSeconds = null;
      this.headers = headers;
      this.identity = null;
      this.size = -1;
    }

    public HttpResult(int status, String contentType, byte[] body, boolean cors, TreeMap<String, String> headers, String identity) {
      this.status = status;
      this.contentType = contentType != null ? contentType : "";
      this.body = body;
      this.space = null;
      this.key = null;
      this.asset = null;
      this.transform = null;
      this.cors = cors;
      this.redirect = false;
      this.location = null;
      this.cacheTimeSeconds = null;
      this.headers = headers;
      this.identity = identity;
      this.size = -1;
    }

    public HttpResult(int status, String space, String key, NtAsset asset, String transform, boolean cors, int cts) {
      this.status = status;
      this.contentType = asset.contentType;
      this.body = null;
      this.space = space;
      this.key = key;
      this.asset = asset;
      this.transform = transform;
      this.cors = cors;
      this.redirect = false;
      this.location = null;
      this.cacheTimeSeconds = cts > 0 ? cts : null;
      this.headers = null;
      this.identity = null;
      this.size = -1;
    }

    public static HttpResult REDIRECT(String location, int code) {
      HttpResultBuilder builder = new HttpResultBuilder();
      builder.status = code;
      builder.location = location;
      builder.redirect = true;
      return new HttpResult(builder);
    }

    public static HttpResult REDIRECT(String location, int code, String identity) {
      HttpResultBuilder builder = new HttpResultBuilder();
      builder.status = code;
      builder.location = location;
      builder.identity = identity;
      builder.redirect = true;
      return new HttpResult(builder);
    }

    public static HttpResult QRCODE(String url, int size) {
      HttpResultBuilder builder = new HttpResultBuilder();
      builder.status = 200;
      builder.contentType = "internal/qr-code";
      builder.location = url;
      builder.size = size;
      return new HttpResult(builder);
    }

    public void logInto(ObjectNode logItem) {
      if (asset != null) {
        logItem.put("asset", asset.id);
      }
      if (contentType != null) {
        logItem.put("content-type", contentType);
      }
    }

  }
}
