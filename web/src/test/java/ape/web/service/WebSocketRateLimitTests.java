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
package ape.web.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import ape.common.ConfigObject;
import ape.common.Json;
import ape.common.Platform;
import ape.common.metrics.NoOpMetricsFactory;
import ape.web.client.TestClientCallback;
import ape.web.client.TestClientRequestBuilder;
import ape.web.service.mocks.MockDomainFinder;
import ape.web.service.mocks.MockServiceBase;
import ape.web.service.mocks.NullCertificateFinder;
import org.junit.Test;

public class WebSocketRateLimitTests {

  /**
   * Validates WebSocket rate limiting by configuring a 1-token bucket and verifying
   * that the second connection is rejected with "rate-limited" after the token is consumed.
   *
   * Reliability rationale:
   * - Token bucket has exactly 1 token with a 1-second window (refreshGuard=1000ms).
   *   The time between TokenRateLimiter construction and connection 2's first check
   *   is ~200-400ms, well under 1000ms, so no token refill occurs.
   * - max-attempts=2 means the second attempt ALWAYS fails regardless of token state
   *   (attempts >= maxAttempts), making the outcome deterministic once the first
   *   attempt finds 0 tokens.
   * - The retry delay (~1000ms) is under the 2-second client read timeout.
   */
  @Test
  public void rateLimitRejectsAfterTokensExhausted() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    ObjectNode configNode = Json.newJsonObject();
    configNode.put("http-port", 16300);
    configNode.put("websocket-heart-beat-ms", 250);
    ObjectNode wsRate = configNode.putObject("websocket-rate-limit");
    wsRate.put("max-tokens", 1);
    wsRate.put("window-ms", 1000);
    wsRate.put("max-grant", 1);
    wsRate.put("minimum-wait", 10);
    wsRate.put("max-attempts", 2);
    wsRate.put("delay", 1);
    wsRate.put("jitter", 0);
    WebConfig webConfig = new WebConfig(new ConfigObject(configNode));
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      // Connection 1: consumes the sole rate-limit token and succeeds
      TestClientCallback callback1 = new TestClientCallback();
      TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s")
          .withWebSocket()
          .execute(callback1);
      callback1.awaitFirst();
      callback1.assertData("{\"status\":\"connected\",\"version\":\"" + Platform.VERSION + "\",\"identities\":{}}");

      // Connection 2: token bucket is empty, first attempt gets 0 tokens,
      // retry fires after ~1000ms, second attempt always fails -> rate-limited
      TestClientCallback callback2 = new TestClientCallback();
      TestClientRequestBuilder.start(group)
          .server("localhost", webConfig.port)
          .get("/~s")
          .withWebSocket()
          .execute(callback2);
      callback2.awaitFirst();
      callback2.assertData("{\"status\":\"disconnected\",\"reason\":\"rate-limited\"}");
    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }
}
