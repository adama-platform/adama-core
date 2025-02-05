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
package ape.web.service.mocks;

import ape.web.contracts.HttpHandler;
import ape.web.contracts.ServiceBase;
import ape.web.contracts.ServiceConnection;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;
import ape.runtime.natives.NtAsset;
import ape.web.assets.AssetStream;
import ape.web.assets.AssetSystem;
import ape.web.assets.AssetUploadBody;
import ape.web.io.ConnectionContext;
import ape.web.io.JsonRequest;
import ape.web.io.JsonResponder;
import ape.web.assets.AssetRequest;

import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

public class MockServiceBase implements ServiceBase {
  @Override
  public ServiceConnection establish(ConnectionContext context) {
    return new ServiceConnection() {
      boolean alive = true;

      @Override
      public void execute(JsonRequest request, JsonResponder responder) {
        try {
          switch (request.method()) {
            case "document/authorization": {
              Boolean error = request.getBoolean("failed", false, 5000);
              if (error != null && error) {
                responder.error(new ErrorCodeException(111));
                return;
              }
              responder.finish("{\"auth\":true}");
              return;
            }
            case "auth": {
              System.out.println("authentication");
              responder.finish("{\"result\":true}");
              return;
            }
            case "cake":
            {
              responder.stream("{\"boss\":1}");
              responder.finish("{\"boss\":2}");
              return;
            }
            case "cake2":
            {
              responder.stream("{\"boss\":1}");
              responder.finish(null);
              return;
            }
            case "empty":
            {
              responder.finish("{}");
              return;
            }
            case "crash":
            {
              throw new NullPointerException();
            }
            case "kill":
              {
                responder.stream("{\"death\":1}");
                alive = false;
                return;
              }
            case "ex":
              {
                responder.error(new ErrorCodeException(1234));
                return;
              }
            case "open":
              responder.stream("{\"hold\":1}");
              return;
          }

        } catch (ErrorCodeException ex) {
          responder.error(ex);
        }
      }

      @Override
      public boolean keepalive() {
        return alive;
      }

      @Override
      public void kill() {}
    };
  }

  @Override
  public HttpHandler http() {
    return new HttpHandler() {

      @Override
      public void handleDeepHealth(Callback<String> callback) {
        callback.success("MOCK");
      }

      @Override
      public void handle(ConnectionContext context, Method method, String identity, String uri, TreeMap<String, String> headers, String parametersJson, String body, Callback<HttpResult> callback) {
        switch (method) {
          case PUT:
            handlePost(uri, headers, parametersJson, body, callback);
            return;
          case OPTIONS:
            handleOptions(uri, headers, parametersJson, callback);
            return;
          case DELETE:
            handleDelete(uri, headers, parametersJson, callback);
            return;
          case GET:
          default:
            handleGet(identity, uri, headers, parametersJson, callback);
        }
      }

      public void handleOptions(String uri, TreeMap<String, String> headers, String parametersJson, Callback<HttpResult> callback) {
        callback.success(new HttpResult(200, "", new byte[0], uri.equalsIgnoreCase("/ok-cors")));
      }

      public void handleDelete(String uri, TreeMap<String, String> headers, String parametersJson, Callback<HttpResult> callback) {
        if ("/foo".equals(uri)){
          callback.success(new HttpHandler.HttpResult(200, "text/html; charset=UTF-8", "deleted".getBytes(StandardCharsets.UTF_8), true));
          return;
        }
        callback.failure(new ErrorCodeException(1000));
      }

      public void handleGet(String identity, String uri, TreeMap<String, String> headers, String parametersJson, Callback<HttpResult> callback) {
        if ("/foo".equals(uri)){
          callback.success(new HttpHandler.HttpResult(200, "text/html; charset=UTF-8", "goo".getBytes(StandardCharsets.UTF_8), true));
          return;
        }

        if ("/inject".equals(uri)){
          callback.success(new HttpHandler.HttpResult(200, "text/html; charset=UTF-8", (identity + ":" + parametersJson).getBytes(StandardCharsets.UTF_8), true));
          return;
        }
        if ("/crash".equals(uri)) {
          callback.failure(new ErrorCodeException(-1));
          return;
        }
        if ("/301".equals(uri)) {
          callback.success(new HttpResult("/loc1", 301));
          return;
        }
        if ("/302".equals(uri)) {
          callback.success(new HttpResult("/loc2", 302));
          return;
        }
        if ("/timeout".equals(uri)) {
          return;
        }
        callback.success(null);
      }

      public void handlePost(String uri, TreeMap<String, String> headers, String parametersJson, String body, Callback<HttpResult> callback) {
        if ("/body".equals(uri)){
          callback.success(new HttpHandler.HttpResult(200, "text/html; charset=UTF-8", ("body:" + body).getBytes(StandardCharsets.UTF_8), true));
          return;
        }
        if ("/crash".equals(uri)) {
          callback.failure(new ErrorCodeException(-1));
          return;
        }
        callback.success(null);
      }
    };
  }

  @Override
  public AssetSystem assets() {
    return new AssetSystem() {
      @Override
      public void request(AssetRequest request, AssetStream stream) {
        if (request.key.equals("1")) {
          stream.headers(-1, "text/plain", "md5");
          byte[] chunk = "ChunkAndDone".getBytes(StandardCharsets.UTF_8);
          stream.body(chunk, 0, chunk.length, true);
          return;
        }
        if (request.key.equals("fail")) {
          stream.headers(-1, "text/plain", "md5");
          stream.failure(1234);
          return;
        }

        if (request.key.equals("incomplete")) {
          stream.headers(-1, "text/plain", "md5");
          byte[] chunk = "Chunk".getBytes(StandardCharsets.UTF_8);
          stream.body(chunk, 0, chunk.length, false);
          stream.failure(1234);
          return;
        }
        if (request.key.equals("3")) {
          stream.headers(-1, "text/plain", "md5");
          byte[] chunk1 = "Chunk1".getBytes(StandardCharsets.UTF_8);
          byte[] chunk2 = "Chunk2".getBytes(StandardCharsets.UTF_8);
          byte[] chunk3 = "Chunk3".getBytes(StandardCharsets.UTF_8);
          stream.body(chunk1, 0, chunk1.length, false);
          stream.body(chunk2, 0, chunk2.length, false);
          stream.body(chunk3, 0, chunk3.length, true);
          return;
        }
      }

      @Override
      public void request(Key key, NtAsset asset, AssetStream stream) {
        if (key.key.equals("1")) {
          byte[] chunk = "ChunkAndDone".getBytes(StandardCharsets.UTF_8);
          stream.headers(chunk.length, "text/plain", "md5");
          stream.body(chunk, 0, chunk.length, true);
          return;
        }
        if (key.key.equals("fail")) {
          stream.headers(-1, "text/plain", "md5");
          stream.failure(1234);
          return;
        }
        if (key.key.equals("incomplete")) {
          byte[] chunk = "Chunk".getBytes(StandardCharsets.UTF_8);
          stream.headers(chunk.length, "text/plain", "md5");
          stream.body(chunk, 0, chunk.length, false);
          stream.failure(1234);
          return;
        }
        if (key.key.equals("3")) {
          stream.headers(-1, "text/plain", "md5");
          byte[] chunk1 = "Chunk1".getBytes(StandardCharsets.UTF_8);
          byte[] chunk2 = "Chunk2".getBytes(StandardCharsets.UTF_8);
          byte[] chunk3 = "Chunk3".getBytes(StandardCharsets.UTF_8);
          stream.body(chunk1, 0, chunk1.length, false);
          stream.body(chunk2, 0, chunk2.length, false);
          stream.body(chunk3, 0, chunk3.length, true);
          return;
        }
      }

      @Override
      public void attach(String identity, ConnectionContext context, Key key, NtAsset asset, String channel, String message, Callback<Integer> callback) {
        callback.failure(new ErrorCodeException(-123));
      }

      @Override
      public void upload(Key key, NtAsset asset, AssetUploadBody body, Callback<Void> callback) {
        if ("failure".equals(key.key)) {
          callback.failure(new ErrorCodeException(-1));
        } else {
          callback.success(null);
        }
      }
    };
  }
}
