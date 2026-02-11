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

import ape.common.Json;
import ape.common.gossip.MockTime;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TokenRateLimiterTests {
  @Test
  public void initialGrantUsesFullBucket() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 30);
    config.put("window-ms", 60000);
    config.put("max-grant", 5);
    config.put("minimum-wait", 250);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    TokenGrant grant = limiter.ask();
    Assert.assertEquals(5, grant.tokens);
    Assert.assertEquals(250, grant.millseconds);
  }

  @Test
  public void drainBucketCompletely() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 10);
    config.put("window-ms", 10000);
    config.put("max-grant", 5);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // First ask: get 5 tokens
    TokenGrant g1 = limiter.ask();
    Assert.assertEquals(5, g1.tokens);
    // Second ask: get 5 more
    TokenGrant g2 = limiter.ask();
    Assert.assertEquals(5, g2.tokens);
    // Third ask: bucket empty, no tokens
    TokenGrant g3 = limiter.ask();
    Assert.assertEquals(0, g3.tokens);
    Assert.assertTrue(g3.millseconds > 0);
  }

  @Test
  public void refillOverTime() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 10);
    config.put("window-ms", 10000);
    config.put("max-grant", 5);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // Drain the bucket
    limiter.ask();
    limiter.ask();
    TokenGrant empty = limiter.ask();
    Assert.assertEquals(0, empty.tokens);
    // Advance time by full window - should refill
    time.currentTime += 10000;
    TokenGrant refilled = limiter.ask();
    Assert.assertEquals(5, refilled.tokens);
  }

  @Test
  public void partialRefill() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 10);
    config.put("window-ms", 10000);
    config.put("max-grant", 5);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // Drain fully
    limiter.ask();
    limiter.ask();
    Assert.assertEquals(0, limiter.ask().tokens);
    // Advance by half the window - partial refill
    time.currentTime += 5000;
    TokenGrant partial = limiter.ask();
    Assert.assertTrue(partial.tokens > 0);
    Assert.assertTrue(partial.tokens <= 5);
  }

  @Test
  public void defaultConfig() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    // Use all defaults
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    TokenGrant grant = limiter.ask();
    Assert.assertEquals(5, grant.tokens);
    Assert.assertEquals(250, grant.millseconds);
  }

  @Test
  public void minimumWaitEnforced() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 100);
    config.put("window-ms", 100);
    config.put("max-grant", 100);
    config.put("minimum-wait", 500);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    TokenGrant grant = limiter.ask();
    Assert.assertTrue(grant.tokens > 0);
    Assert.assertTrue(grant.millseconds >= 500);
  }

  @Test
  public void emptyBucketReturnsPositiveWait() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 1);
    config.put("window-ms", 60000);
    config.put("max-grant", 1);
    config.put("minimum-wait", 250);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // Take the only token
    TokenGrant first = limiter.ask();
    Assert.assertEquals(1, first.tokens);
    // Now empty
    TokenGrant empty = limiter.ask();
    Assert.assertEquals(0, empty.tokens);
    Assert.assertTrue(empty.millseconds > 0);
  }

  @Test
  public void maxGrantCaps() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 100);
    config.put("window-ms", 60000);
    config.put("max-grant", 3);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    TokenGrant grant = limiter.ask();
    Assert.assertEquals(3, grant.tokens);
  }

  @Test
  public void askOneAtTime() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 5);
    config.put("window-ms", 60000);
    config.put("max-grant", 3);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(0, limiter.ask(1).tokens);
    time.currentTime += 120000;
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(0, limiter.ask(1).tokens);

    time.currentTime += 30000;
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(1, limiter.ask(1).tokens);
    Assert.assertEquals(0, limiter.ask(1).tokens);
  }


  @Test
  public void exhausts_and_refills() {
    MockTime time = new MockTime();
    TokenRateLimiter defaults = TokenRateLimiter.create(Json.newJsonObject(), time);
    TokenGrant grant;
    for (int k = 0; k < 6; k++) {
      time.currentTime += 1000;
      grant = defaults.ask();
      Assert.assertEquals(5, grant.tokens);
      Assert.assertEquals(250, grant.millseconds);
    }
    grant = defaults.ask();
    Assert.assertEquals(3, grant.tokens);
    Assert.assertEquals(4000, grant.millseconds);
    time.currentTime += 1000;
    grant = defaults.ask();
    Assert.assertEquals(0, grant.tokens);
    Assert.assertEquals(2000, grant.millseconds);

    time.currentTime += 160000;

    for (int k = 0; k < 6; k++) {
      time.currentTime += 1000;
      grant = defaults.ask();
      Assert.assertEquals(5, grant.tokens);
      Assert.assertEquals(250, grant.millseconds);
    }
    grant = defaults.ask();
    Assert.assertEquals(2, grant.tokens);
    Assert.assertEquals(6000, grant.millseconds);
    time.currentTime += 1000;
    grant = defaults.ask();
    Assert.assertEquals(1, grant.tokens);
    Assert.assertEquals(8000, grant.millseconds);
    grant = defaults.ask();
    Assert.assertEquals(0, grant.tokens);
    Assert.assertEquals(2000, grant.millseconds);
  }

  public static class Sample {
    public final long time;
    public final int tokens;

    public Sample(long time, int tokens) {
      this.time = time;
      this.tokens = tokens;
    }
  }

  @Test
  public void askWithOversizedTokensGetsClamped() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 10);
    config.put("window-ms", 10000);
    config.put("max-grant", 3);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // Request 100 tokens, should be clamped to maxGrant (3)
    TokenGrant grant = limiter.ask(100);
    Assert.assertEquals(3, grant.tokens);
  }

  @Test
  public void askWithZeroTokensGetsClamped() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 10);
    config.put("window-ms", 10000);
    config.put("max-grant", 5);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // Request 0 tokens, should be clamped to 1
    TokenGrant grant = limiter.ask(0);
    Assert.assertEquals(1, grant.tokens);
  }

  @Test
  public void askWithNegativeTokensGetsClamped() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 10);
    config.put("window-ms", 10000);
    config.put("max-grant", 5);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // Request negative tokens, should be clamped to 1
    TokenGrant grant = limiter.ask(-5);
    Assert.assertEquals(1, grant.tokens);
  }

  @Test
  public void askWithTokensDraining() {
    MockTime time = new MockTime();
    time.currentTime = 10000;
    ObjectNode config = Json.newJsonObject();
    config.put("max-tokens", 5);
    config.put("window-ms", 10000);
    config.put("max-grant", 5);
    config.put("minimum-wait", 100);
    TokenRateLimiter limiter = TokenRateLimiter.create(config, time);
    // Take 3
    TokenGrant g1 = limiter.ask(3);
    Assert.assertEquals(3, g1.tokens);
    // Take 2
    TokenGrant g2 = limiter.ask(2);
    Assert.assertEquals(2, g2.tokens);
    // Bucket empty
    TokenGrant g3 = limiter.ask(1);
    Assert.assertEquals(0, g3.tokens);
  }

  @Test
  public void validate_rate_greedy() {
    MockTime time = new MockTime();
    TokenRateLimiter defaults = TokenRateLimiter.create(Json.newJsonObject(), time);
    ArrayList<Sample> samples = new ArrayList<>();
    long tokens = 0;
    while (time.currentTime < 100 * 60000) {
      TokenGrant grant = defaults.ask();
      tokens += grant.tokens;
      if (grant.tokens > 0) {
        samples.add(new Sample(time.currentTime, grant.tokens));
      }
      time.currentTime += 250;
    }
    Assert.assertEquals(3029, tokens);
    for (int init = 0; init < samples.size(); init++) {
      long start = samples.get(init).time;
      int j = init;
      int k = init;
      while (j < samples.size() && (samples.get(j).time - start) < 60000) {
        j++;
      }
      int window = 0;
      while (k < j) {
        window += samples.get(k).tokens;
        k++;
      }
      // the burst rate is actually 2X the max tokens, but the steady state rate is max tokens
      Assert.assertTrue(window <= 60);
    }
  }
}
