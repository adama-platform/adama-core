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
package ape.web.client.socket;

import ape.common.ConfigObject;
import ape.common.Json;
import org.junit.Assert;
import org.junit.Test;

public class MultiWebClientRetryPoolConfigTests {

  @Test
  public void defaults() {
    MultiWebClientRetryPoolConfig config = new MultiWebClientRetryPoolConfig(new ConfigObject(Json.parseJsonObject("{}")));
    Assert.assertEquals(2, config.connectionCount);
    Assert.assertEquals(50, config.maxInflight);
    Assert.assertEquals(1500, config.findTimeout);
    Assert.assertEquals(5000, config.maxBackoff);
  }

  @Test
  public void coverage() {
    MultiWebClientRetryPoolConfig config = new MultiWebClientRetryPoolConfig(new ConfigObject(Json.parseJsonObject("{\"multi-connection-count\":5,\"multi-inflight-limit\":77,\"multi-timeout-find\":9998,\"multi-max-backoff-milliseconds\":1234}")));
    Assert.assertEquals(5, config.connectionCount);
    Assert.assertEquals(77, config.maxInflight);
    Assert.assertEquals(9998, config.findTimeout);
    Assert.assertEquals(1234, config.maxBackoff);
  }
}
