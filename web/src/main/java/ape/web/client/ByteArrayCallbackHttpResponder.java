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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ByteArrayCallbackHttpResponder implements SimpleHttpResponder {
  private final Logger logger;
  private final RequestResponseMonitor.RequestResponseMonitorInstance monitor;
  private final Callback<byte[]> callback;
  private ByteArrayOutputStream memory;
  private boolean invokeSuccess;
  private boolean emissionPossible;
  private boolean logBody;

  public ByteArrayCallbackHttpResponder(Logger logger, RequestResponseMonitor.RequestResponseMonitorInstance monitor, Callback<byte[]> callback) {
    this.logger = logger;
    this.monitor = monitor;
    this.callback = callback;
    this.invokeSuccess = false;
    this.emissionPossible = true;
    this.logBody = false;
  }

  @Override
  public void start(SimpleHttpResponseHeader header) {
    if (emissionPossible) {
      if (200 <= header.status && header.status <= 204) {
        invokeSuccess = true;
      } else {
        logger.error("get-callback-not-20x: {}, {}", header.status + ":" + header.headers.toString());
        emissionPossible = false;
        logBody = true;
        int errorCode = HttpError.translateHttpStatusCodeToError(header.status, ErrorCodes.WEB_BYTEARRAY_CALLBACK_NOT_200);
        monitor.failure(errorCode);
        callback.failure(new ErrorCodeException(errorCode, header.status + ""));
      }
    }
  }

  @Override
  public void bodyStart(long size) {
    if (size > 0) {
      this.memory = new ByteArrayOutputStream((int) size);
    }
  }

  @Override
  public void bodyFragment(byte[] chunk, int offset, int len) {
    if (memory == null) { // unknown size due to an error
      this.memory = new ByteArrayOutputStream();
    }
    memory.write(chunk, offset, len);
  }

  @Override
  public void bodyEnd() {
    if (invokeSuccess && emissionPossible) {
      callback.success(memory.toByteArray());
      monitor.success();
    }
    if (logBody) {
      logger.error("failed body: {}", new String(memory.toByteArray(), StandardCharsets.UTF_8));
    }
  }

  @Override
  public void failure(ErrorCodeException ex) {
    if (emissionPossible) {
      emissionPossible = false;
      logger.error("bytearray-callback-failure:", ex);
      monitor.failure(ex.code);
      callback.failure(ex);
    }
  }
}
