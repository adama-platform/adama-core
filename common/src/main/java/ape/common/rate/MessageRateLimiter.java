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

import ape.common.TimeSource;

/**
 * Synchronous per-connection message rate limiter using a token bucket.
 * Designed for WebSocket connections to cap messages per second.
 * When a message is rejected, an error response is sent for the first
 * maxErrorResponses rejections; after that, messages are silently dropped.
 * The error counter resets on each successful message.
 */
public class MessageRateLimiter {
  private final int maxMessagesPerSecond;
  private final int maxErrorResponses;
  private final TimeSource time;
  private long windowStart;
  private int count;
  private int consecutiveRejects;

  public MessageRateLimiter(int maxMessagesPerSecond, int maxErrorResponses, TimeSource time) {
    this.maxMessagesPerSecond = maxMessagesPerSecond;
    this.maxErrorResponses = maxErrorResponses;
    this.time = time;
    this.windowStart = time.nowMilliseconds();
    this.count = 0;
    this.consecutiveRejects = 0;
  }

  /** result of trying to allow a message through */
  public enum Result {
    /** message allowed */
    ALLOWED,
    /** message rejected - caller should send an error response */
    REJECT_WITH_ERROR,
    /** message rejected - caller should silently drop */
    REJECT_SILENT
  }

  /** check if a message should be allowed; call this for each incoming message */
  public Result check() {
    long now = time.nowMilliseconds();
    long elapsed = now - windowStart;
    if (elapsed >= 1000) {
      windowStart = now;
      count = 0;
    }
    count++;
    if (count <= maxMessagesPerSecond) {
      consecutiveRejects = 0;
      return Result.ALLOWED;
    }
    consecutiveRejects++;
    if (consecutiveRejects <= maxErrorResponses) {
      return Result.REJECT_WITH_ERROR;
    }
    return Result.REJECT_SILENT;
  }
}
