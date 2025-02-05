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

/** a simple http handler */
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
    }

    public HttpResult(String location, int code) {
      this.status = code;
      this.contentType = null;
      this.body = null;
      this.space = null;
      this.key = null;
      this.asset = null;
      this.transform = null;
      this.cors = true;
      this.redirect = true;
      this.location = location;
      this.cacheTimeSeconds = null;
      this.headers = null;
      this.identity = null;
    }

    public HttpResult(String location, int code, String identity) {
      this.status = code;
      this.contentType = null;
      this.body = null;
      this.space = null;
      this.key = null;
      this.asset = null;
      this.transform = null;
      this.cors = true;
      this.redirect = true;
      this.location = location;
      this.cacheTimeSeconds = null;
      this.headers = null;
      this.identity = identity;
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
