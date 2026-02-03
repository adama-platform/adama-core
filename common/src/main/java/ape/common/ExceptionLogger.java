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
package ape.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for logging exceptions that are converted to error codes.
 * Provides visibility into unexpected exceptions that get wrapped in
 * ErrorCodeException, enabling monitoring and debugging of error conversion.
 */
public interface ExceptionLogger {
  static ExceptionLogger FOR(Class<?> clazz) {
    Logger logger = LoggerFactory.getLogger(clazz);
    return (t, ec) -> {
      logger.error("exception", t);
    };
  }

  static ExceptionLogger FOR(Logger logger) {
    return (t, ec) -> {
      logger.error("exception", t);
    };
  }

  /**
   * an issue emerged which was not understood by an error code, and was returned to use as the
   * given error code
   */
  void convertedToErrorCode(Throwable t, int errorCode);
}
