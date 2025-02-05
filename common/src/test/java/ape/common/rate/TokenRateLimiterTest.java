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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class TokenRateLimiterTest {
  @Test
  public void exhausts_and_refills() {
    MockTime time = new MockTime();
    TokenRateLimiter defaults = new TokenRateLimiter(Json.newJsonObject(), time);
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
  public void validate_rate_greedy() {
    MockTime time = new MockTime();
    TokenRateLimiter defaults = new TokenRateLimiter(Json.newJsonObject(), time);
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
