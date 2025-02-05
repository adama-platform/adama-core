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

/** convert standard HTTP Error codes into specialized error codes that are actionable */
public class HttpError {
  public static int translateHttpStatusCodeToError(int status, int given) {
    if (status == 410) {
      return ErrorCodes.WEB_CALLBACK_RESOURCE_GONE;
    } else if (status == 404) {
      return ErrorCodes.WEB_CALLBACK_RESOURCE_NOT_FOUND;
    } else if (status == 403) {
      return ErrorCodes.WEB_CALLBACK_RESOURCE_NOT_AUTHORIZED;
    } else if (status == 301 || status == 302) {
      return ErrorCodes.WEB_CALLBACK_REDIRECT;
    }
    return given;
  }

  public static boolean convert(SimpleHttpResponseHeader header, Logger logger, int defaultErrorCode, RequestResponseMonitor.RequestResponseMonitorInstance monitor, Callback<?> callback) {
    boolean logBody = true;
    switch (header.status) {
      case 302:
      case 301:
      case 410:
      case 404:
      case 403:
        // these are converted to unique errors
        logBody = false;
        break;
      default:
        logger.error("void-callback-not-20x: {} -> {}", header.status, header.headers.toString());
    }
    int errorCode = HttpError.translateHttpStatusCodeToError(header.status, defaultErrorCode);
    if (monitor != null) {
      monitor.failure(errorCode);
    }
    callback.failure(new ErrorCodeException(errorCode, header.status + ""));
    return logBody;
  }
}
