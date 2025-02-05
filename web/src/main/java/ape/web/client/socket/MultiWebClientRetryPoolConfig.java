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

/** the configuration for the behavior of maintaining a connection to an endpoint */
public class MultiWebClientRetryPoolConfig {
  public final int connectionCount;
  public final int maxInflight;
  public final int findTimeout;
  public final int maxBackoff;

  public MultiWebClientRetryPoolConfig(ConfigObject config) {
    this.connectionCount = config.intOf("multi-connection-count", 2);
    this.maxInflight = config.intOf("multi-inflight-limit", 50);
    this.findTimeout = config.intOf("multi-timeout-find", 1500);
    this.maxBackoff = config.intOf("multi-max-backoff-milliseconds", 5000);
  }
}
