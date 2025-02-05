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

import ape.common.ErrorCodeException;
import ape.web.assets.AssetSystem;
import ape.web.io.ConnectionContext;
import ape.web.io.JsonRequest;
import ape.web.io.JsonResponder;

/**
 * this is the base of the service which is used to spawn ServiceConnection's when a new request
 * comes online
 */
public interface ServiceBase {

  static ServiceBase JUST_HTTP(HttpHandler http) {
    return new ServiceBase() {
      @Override
      public ServiceConnection establish(ConnectionContext context) {
        return new ServiceConnection() {
          @Override
          public void execute(JsonRequest request, JsonResponder responder) {
            responder.error(new ErrorCodeException(-1));
          }

          @Override
          public boolean keepalive() {
            return false;
          }

          @Override
          public void kill() {
          }
        };
      }

      @Override
      public HttpHandler http() {
        return http;
      }

      @Override
      public AssetSystem assets() {
        return null;
      }
    };
  }

  /** a new connection has presented itself */
  ServiceConnection establish(ConnectionContext context);

  HttpHandler http();

  AssetSystem assets();
}
