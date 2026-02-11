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
package ape.common.rate;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;

/** a rate limiter that will re-try such that bursts are delayed, and also ensure we introduce a bit of latency for critical operations to protect even at rate limit */
public class AsyncTokenLimiter {
  private final SimpleExecutor executor;
  private final TokenRateLimiter limiter;

  public AsyncTokenLimiter(SimpleExecutor executor, TokenRateLimiter limiter) {
    this.executor = executor;
    this.limiter = limiter;
  }

  public void execute(int maxAttempts, int delay, int jitter, Callback<Void> callback) {
    final NamedRunnable testAndExecute = new NamedRunnable("async") {
      private int attempts = 0;

      @Override
      public void execute() throws Exception {
        TokenGrant grant = limiter.ask(1);
        attempts++;
        if (attempts < maxAttempts) {
          if (grant.tokens > 0) {
            callback.success(null);
          } else {
            executor.schedule(this, (int) (grant.millseconds + delay * Math.random()));
          }
        } else {
          callback.failure(new ErrorCodeException(ErrorCodes.OVERLOADED_RATE_LIMITED));
        }
      }
    };
    executor.schedule(testAndExecute, (int) (delay + Math.random() * jitter));
  }
}
