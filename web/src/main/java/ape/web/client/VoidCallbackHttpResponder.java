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
package ape.web.client;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.metrics.RequestResponseMonitor;
import org.slf4j.Logger;

public class VoidCallbackHttpResponder implements SimpleHttpResponder {
  private final Logger logger;
  private final RequestResponseMonitor.RequestResponseMonitorInstance monitor;
  private final Callback<Void> callback;
  private boolean emissionPossible;

  public VoidCallbackHttpResponder(Logger logger, RequestResponseMonitor.RequestResponseMonitorInstance monitor, Callback<Void> callback) {
    this.logger = logger;
    this.monitor = monitor;
    this.callback = callback;
    this.emissionPossible = true;
  }

  @Override
  public void start(SimpleHttpResponseHeader header) {
    if (emissionPossible) {
      emissionPossible = false;
      if (200 <= header.status && header.status <= 204) {
        monitor.success();
        callback.success(null);
      } else {
        HttpError.convert(header, logger,  ErrorCodes.WEB_VOID_CALLBACK_NOT_200, monitor, callback);
      }
    }
  }

  @Override
  public void bodyStart(long size) {
    if (size != 0) {
      monitor.extra();
    }
  }

  @Override
  public void bodyFragment(byte[] chunk, int offset, int len) {
    if (len > 0) {
      logger.error("unexpected-body: {}", new String(chunk, offset, len));
    }
  }

  @Override
  public void bodyEnd() {}

  @Override
  public void failure(ErrorCodeException ex) {
    if (emissionPossible) {
      emissionPossible = false;
      logger.error("void-callback-failure:", ex);
      monitor.failure(ex.code);
      callback.failure(ex);
    }
  }
}
