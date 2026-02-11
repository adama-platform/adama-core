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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Volume-based rate limiter keyed by an arbitrary string (e.g. IP address).
 * Tracks bytes consumed per key within a rolling time window and rejects
 * when the volume exceeds the configured threshold.
 */
public class VolumeRateLimiter {
  private final long maxBytesPerWindow;
  private final long windowMilliseconds;
  private final TimeSource time;
  private final ConcurrentHashMap<String, VolumeBucket> buckets;

  public VolumeRateLimiter(long maxBytesPerWindow, long windowMilliseconds, TimeSource time) {
    this.maxBytesPerWindow = maxBytesPerWindow;
    this.windowMilliseconds = windowMilliseconds;
    this.time = time;
    this.buckets = new ConcurrentHashMap<>();
  }

  /** attempt to consume the given number of bytes for the given key; returns true if allowed */
  public boolean consume(String key, int bytes) {
    VolumeBucket bucket = buckets.computeIfAbsent(key, k -> new VolumeBucket(time.nowMilliseconds()));
    return bucket.tryConsume(bytes, time.nowMilliseconds());
  }

  /** remove stale entries that haven't been touched within twice the window */
  public void gc() {
    long now = time.nowMilliseconds();
    long expiry = windowMilliseconds * 2;
    buckets.entrySet().removeIf(e -> (now - e.getValue().lastTouched()) > expiry);
  }

  private class VolumeBucket {
    private long windowStart;
    private long consumed;
    private long lastTouch;

    VolumeBucket(long now) {
      this.windowStart = now;
      this.consumed = 0;
      this.lastTouch = now;
    }

    synchronized long lastTouched() {
      return lastTouch;
    }

    synchronized boolean tryConsume(int bytes, long now) {
      lastTouch = now;
      long elapsed = now - windowStart;
      if (elapsed >= windowMilliseconds) {
        // reset the window
        windowStart = now;
        consumed = 0;
      } else if (elapsed > 0) {
        // drain proportionally to elapsed time
        long drained = (maxBytesPerWindow * elapsed) / windowMilliseconds;
        consumed = Math.max(0, consumed - drained);
        windowStart = now;
      }
      if (consumed + bytes > maxBytesPerWindow) {
        return false;
      }
      consumed += bytes;
      return true;
    }
  }
}
