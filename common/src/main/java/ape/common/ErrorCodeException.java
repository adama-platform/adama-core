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

/**
 * Exception carrying an integer error code for structured error handling.
 * Error codes provide stable identifiers for error conditions that can be
 * safely exposed to clients and used for programmatic error handling.
 */
public class ErrorCodeException extends Exception {
  public final int code;

  public ErrorCodeException(final int code) {
    super("code:" + code);
    this.code = code;
  }

  public ErrorCodeException(final int code, String message) {
    super(message);
    this.code = code;
  }

  public ErrorCodeException(final int code, final Throwable cause) {
    super("code:" + code + ":" + cause.getMessage(), cause);
    this.code = code;
  }

  public static ErrorCodeException detectOrWrap(int code, Throwable cause, ExceptionLogger logger) {
    if (cause instanceof RuntimeException) {
      if (cause.getCause() instanceof ErrorCodeException) {
        return (ErrorCodeException) (cause.getCause());
      }
    }
    if (cause instanceof ErrorCodeException) {
      return (ErrorCodeException) cause;
    }
    if (logger != null) {
      logger.convertedToErrorCode(cause, code);
    }
    return new ErrorCodeException(code, cause);
  }
}
