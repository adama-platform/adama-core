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

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * This callback interface is used by DataService such that actions not only succeed/fail, but
 * provide progress notifications which could relate to a state diagram.
 */
public interface Callback<T> {
  ExceptionLogger CALLBACK_LOGGER = ExceptionLogger.FOR(Callback.class);

  Callback<Integer> DONT_CARE_INTEGER = new Callback<Integer>() {
    @Override
    public void success(Integer value) {
    }

    @Override
    public void failure(ErrorCodeException ex) {
    }
  };
  Callback<Void> DONT_CARE_VOID = new Callback<Void>() {
    @Override
    public void success(Void value) {
    }

    @Override
    public void failure(ErrorCodeException ex) {
    }
  };
  Callback<String> DONT_CARE_STRING = new Callback<String>() {
    @Override
    public void success(String value) {
    }

    @Override
    public void failure(ErrorCodeException ex) {
    }
  };

  public static Callback<Void> FINISHED_LATCH_DONT_CARE_VOID(CountDownLatch latch) {
    return new Callback<Void>() {
      @Override
      public void success(Void value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    };
  }

  static <In, Out> Callback<In> transform(Callback<Out> output, int exceptionErrorCode, Function<In, Out> f) {
    return new Callback<>() {
      @Override
      public void success(In value) {
        try {
          output.success(f.apply(value));
        } catch (Throwable ex) {
          output.failure(ErrorCodeException.detectOrWrap(exceptionErrorCode, ex, CALLBACK_LOGGER));
        }
      }

      @Override
      public void failure(ErrorCodeException ex) {
        output.failure(ex);
      }
    };
  }

  /** the action happened successfully, and the result is value */
  void success(T value);

  /** the action failed outright, and the reason is the exception */
  void failure(ErrorCodeException ex);

  static <T> Callback<Void> handoff(Callback<T> next, int exceptionErrorCode, Runnable success) {
    return new Callback<>() {
      @Override
      public void success(Void value) {
        try {
          success.run();
        } catch (Throwable ex) {
          next.failure(ErrorCodeException.detectOrWrap(exceptionErrorCode, ex, CALLBACK_LOGGER));
        }
      }

      @Override
      public void failure(ErrorCodeException ex) {
        next.failure(ex);
      }
    };
  }

  static <T> Callback<T> SUCCESS_OR_FAILURE_THROW_AWAY_VALUE(Callback<Void> callback) {
    return new Callback<T>() {
      @Override
      public void success(T value) {
        callback.success(null);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        callback.failure(ex);
      }
    };
  }
}
