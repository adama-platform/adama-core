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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.common.TimeSource;

/** a simple rate limiter using a token bucket that fills up */
public class TokenRateLimiter {
  private final int maxTokensInWindow;
  private final int windowMilliseconds;
  private final int maxGrant;
  private final TimeSource time;
  private double tokens;
  private long at;
  private final double refreshGuard;
  private final int minimumWait;

  public TokenRateLimiter(ObjectNode config, TimeSource time) {
    this.maxTokensInWindow = Json.readInteger(config, "max-tokens", 30);
    this.windowMilliseconds = Json.readInteger(config, "window-ms", 60000);
    this.maxGrant = Json.readInteger(config, "max-grant", 5);
    this.minimumWait = Json.readInteger(config, "minimum-wait", 250);
    this.time = time;
    this.tokens = maxTokensInWindow;
    this.at = time.nowMilliseconds();
    this.refreshGuard = windowMilliseconds / maxTokensInWindow;
  }

  public synchronized TokenGrant ask() {
    long now = time.nowMilliseconds();
    long delta = now - at;
    if (delta >= refreshGuard) { // threshold for numerical significance
      tokens += delta * maxTokensInWindow / windowMilliseconds;
      tokens = Math.min(maxTokensInWindow, Math.ceil(tokens));
      at = now;
    }
    if (tokens >= 1) {
      int tokensToTake = (int) Math.min(maxGrant, tokens);
      tokens -= tokensToTake;
      return new TokenGrant(tokensToTake, Math.max(minimumWait, (int) ((maxGrant - tokensToTake) * refreshGuard)));
    } else {
      return new TokenGrant(0, (int) Math.max(refreshGuard, minimumWait));
    }
  }
}
