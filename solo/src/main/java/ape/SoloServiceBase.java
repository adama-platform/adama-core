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
package ape;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;
import ape.runtime.natives.NtAsset;
import ape.runtime.sys.CoreService;
import ape.web.assets.AssetRequest;
import ape.web.assets.AssetStream;
import ape.web.assets.AssetSystem;
import ape.web.assets.AssetUploadBody;
import ape.web.contracts.HttpHandler;
import ape.web.contracts.ServiceBase;
import ape.web.contracts.ServiceConnection;
import ape.web.io.ConnectionContext;
import ape.web.io.JsonRequest;
import ape.web.io.JsonResponder;

import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

/** connects HTTP to CoreService + File System */
public class SoloServiceBase implements ServiceBase {
  private final CoreService service;

  public SoloServiceBase(CoreService service) {
    this.service = service;
  }

  @Override
  public ServiceConnection establish(ConnectionContext context) {
    SoloResponder base = new SoloResponder(context, service);
    return new ServiceConnection() {
      @Override
      public void execute(JsonRequest request, JsonResponder responder) {
        base.route(request, responder);
      }

      @Override
      public boolean keepalive() {
        return true;
      }

      @Override
      public void kill() {
        base.kill();
      }
    };
  }

  @Override
  public HttpHandler http() {
    return new HttpHandler() {
      @Override
      public void handle(ConnectionContext context, Method method, String identity, String uri, TreeMap<String, String> headers, String parametersJson, String body, Callback<HttpResult> callback) {
        if (method == Method.GET) {
          // TODO: CHECK static directory
        }
        callback.success(new HttpResult(400, "text/html", "NOPE".getBytes(StandardCharsets.UTF_8), true));
      }

      @Override
      public void handleDeepHealth(Callback<String> callback) {
        callback.success("solo");
      }
    };
  }

  @Override
  public AssetSystem assets() {
    return new AssetSystem() {
      @Override
      public void request(AssetRequest request, AssetStream stream) {
        stream.failure(-1);
      }

      @Override
      public void request(Key key, NtAsset asset, AssetStream stream) {
        stream.failure(-1);
      }

      @Override
      public void attach(String identity, ConnectionContext context, Key key, NtAsset asset, String channel, String message, Callback<Integer> callback) {
        callback.failure(new ErrorCodeException(-1));
      }

      @Override
      public void upload(Key key, NtAsset asset, AssetUploadBody body, Callback<Void> callback) {
        callback.failure(new ErrorCodeException(-1));
      }
    };
  }
}
