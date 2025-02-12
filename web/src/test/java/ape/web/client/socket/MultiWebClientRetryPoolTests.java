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

import ape.common.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import ape.common.metrics.NoOpMetricsFactory;
import ape.web.client.WebClientBase;
import ape.web.client.WebClientBaseMetrics;
import ape.web.contracts.WebJsonStream;
import ape.web.service.ServiceRunnable;
import ape.web.service.WebConfig;
import ape.web.service.WebConfigTests;
import ape.web.service.WebMetrics;
import ape.web.service.mocks.MockDomainFinder;
import ape.web.service.mocks.MockServiceBase;
import ape.web.service.mocks.NullCertificateFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MultiWebClientRetryPoolTests {
  @Test
  public void flow() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.Pool);
    MockServiceBase base = new MockServiceBase();
    var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    SimpleExecutor executor = SimpleExecutor.create("simple");
    WebClientBase webbase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), new WebConfig(new ConfigObject(Json.newJsonObject())));
    try {
      runnable.waitForReady(1000);
      MultiWebClientRetryPoolMetrics metrics = new MultiWebClientRetryPoolMetrics(new NoOpMetricsFactory());
      MultiWebClientRetryPoolConfig config = new MultiWebClientRetryPoolConfig(new ConfigObject(Json.newJsonObject()));
      MultiWebClientRetryPool pool = new MultiWebClientRetryPool(executor, webbase, metrics, config, (connection, callback) -> connection.requestResponse(Json.parseJsonObject("{\"method\":\"auth\"}"), (r) -> (Boolean) (r.get("result").asBoolean()), new Callback<Boolean>() {
        @Override
        public void success(Boolean value) {
          System.out.println("auth check: " + value);
          callback.success(null);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          callback.failure(ex);
        }
      }), "http://localhost:16000/~s");
      try {
        CountDownLatch latch = new CountDownLatch(2);
        CountDownLatch failure = new CountDownLatch(1);

        pool.get(new Callback<WebClientConnection>() {
          @Override
          public void success(WebClientConnection conn) {
            latch.countDown();
            conn.execute(Json.parseJsonObject("{\"method\":\"open\"}"), new WebJsonStream() {
              @Override
              public void data(int connection, ObjectNode node) {
                latch.countDown();
              }

              @Override
              public void complete() {

              }

              @Override
              public void failure(int code) {
                System.err.println("Failure-1:" + code);
                Assert.assertEquals(787632, code);
                failure.countDown();
              }
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });

        Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
        runnable.shutdown();
        thread.join();
        Assert.assertTrue(failure.await(10000, TimeUnit.MILLISECONDS));
        CountDownLatch cant_connect = new CountDownLatch(2);
        pool.get(new Callback<WebClientConnection>() {
          @Override
          public void success(WebClientConnection conn) {
            cant_connect.countDown();
            conn.execute(Json.parseJsonObject("{\"method\":\"empty\"}"), new WebJsonStream() {
              @Override
              public void data(int connection, ObjectNode node) {
              }

              @Override
              public void complete() {

              }

              @Override
              public void failure(int code) {
                System.err.println("Failure-2:" + code);
                Assert.assertEquals(770224, code);
                cant_connect.countDown();
              }
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {
            cant_connect.countDown();
            cant_connect.countDown();
          }
        });
        Assert.assertTrue(cant_connect.await(10000, TimeUnit.MILLISECONDS));

        runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
        thread = new Thread(runnable);
        thread.start();
        runnable.waitForReady(1000);
        CountDownLatch redo = new CountDownLatch(2);
        pool.get(new Callback<WebClientConnection>() {
          @Override
          public void success(WebClientConnection conn) {
            redo.countDown();
            conn.execute(Json.parseJsonObject("{\"method\":\"empty\"}"), new WebJsonStream() {
              @Override
              public void data(int connection, ObjectNode node) {
              }

              @Override
              public void complete() {
                redo.countDown();
              }

              @Override
              public void failure(int code) {
                System.err.println("Failure-3:" + code);
              }
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(redo.await(5000, TimeUnit.MILLISECONDS));

      } finally {
        pool.shutdown();
      }

    } finally {
      if (runnable != null) {
        runnable.shutdown();
      }
      thread.join();
      group.shutdownGracefully();
      executor.shutdown();
      webbase.shutdown();
    }
  }
}
