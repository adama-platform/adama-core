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
package ape.web.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.common.metrics.NoOpMetricsFactory;
import ape.web.client.socket.WebClientConnection;
import ape.web.contracts.WebJsonStream;
import ape.web.contracts.WebLifecycle;
import ape.web.service.ServiceRunnable;
import ape.web.service.WebConfig;
import ape.web.service.WebConfigTests;
import ape.web.service.WebMetrics;
import ape.web.service.mocks.MockDomainFinder;
import ape.web.service.mocks.MockServiceBase;
import ape.web.service.mocks.NullCertificateFinder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WebClientBaseTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebClientBaseTests.class);

  @Test
  public void happy() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.ClientTest1);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    WebClientBase clientBase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), webConfig);
    try {
      AtomicReference<WebClientConnection> connRef = new AtomicReference<>();
      CountDownLatch connectedLatch = new CountDownLatch(1);
      CountDownLatch firstPing = new CountDownLatch(1);
      CountDownLatch disconnected = new CountDownLatch(1);
      LatchedWebJsonStream streamCake = new LatchedWebJsonStream();
      LatchedWebJsonStream streamEx = new LatchedWebJsonStream();
      LatchedWebJsonStream streamEmpty = new LatchedWebJsonStream();
      Runnable cakeFin = streamCake.latchAt(3);
      Runnable exFin = streamEx.latchAt(1);
      Runnable emptyFin = streamEmpty.latchAt(1);
      clientBase.open(
          "http://localhost:" + webConfig.port + "/~s",
          new WebLifecycle() {
            @Override
            public void connected(WebClientConnection connection, String version) {
              connRef.set(connection);
              connection.execute(Json.parseJsonObject("{\"method\":\"cake\"}"), streamCake);
              connection.execute(Json.parseJsonObject("{\"method\":\"ex\"}"), streamEx);
              connection.execute(Json.parseJsonObject("{\"method\":\"empty\"}"), streamEmpty);
              connectedLatch.countDown();
            }

            @Override
            public void ping(int latency) {
              firstPing.countDown();
            }

            @Override
            public void failure(Throwable t) {}

            @Override
            public void disconnected() {
              disconnected.countDown();
            }
          });
      Assert.assertTrue(connectedLatch.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertTrue(firstPing.await(5000, TimeUnit.MILLISECONDS));
      cakeFin.run();
      exFin.run();
      emptyFin.run();
      streamCake.assertLine(0, "DATA:{\"boss\":1}");
      streamCake.assertLine(1, "DATA:{\"boss\":2}");
      streamCake.assertLine(2, "COMPLETE");
      streamEx.assertLine(0, "FAILURE:1234");
      streamEmpty.assertLine(0, "COMPLETE");
      runnable.shutdown();
      Assert.assertTrue(disconnected.await(5000, TimeUnit.MILLISECONDS));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally{
      clientBase.shutdown();
    }
  }

  @Test
  public void http_simple() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.HttpExecute1);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    WebClientBase clientBase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), webConfig);
    try {
      SimpleHttpRequest request = new SimpleHttpRequest("GET", "http://localhost:" + webConfig.port + "/foo", new TreeMap<>(), SimpleHttpRequestBody.EMPTY);
      CountDownLatch done = new CountDownLatch(1);
      clientBase.executeShared(request, new StringCallbackHttpResponder(LOGGER, new NoOpMetricsFactory().makeRequestResponseMonitor("simple").start(), new Callback<String>() {
        @Override
        public void success(String value) {
          Assert.assertEquals("goo", value);
          done.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {
          ex.printStackTrace();
          done.countDown();
        }
      }));
      Assert.assertTrue(done.await(10000, TimeUnit.MILLISECONDS));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally{
      clientBase.shutdown();
    }
  }

  /*
  @Test
  public void http_timeout() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.HttpExecute1);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    WebClientBase clientBase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), webConfig);
    try {
      SimpleHttpRequest request = new SimpleHttpRequest("GET", "http://localhost:" + webConfig.port + "/timeout", new TreeMap<>(), SimpleHttpRequestBody.EMPTY);
      CountDownLatch done = new CountDownLatch(1);
      clientBase.executeShared(request, new StringCallbackHttpResponder(LOGGER, new NoOpMetricsFactory().makeRequestResponseMonitor("simple").start(), new Callback<String>() {
        @Override
        public void success(String value) {
          System.err.println("SUCCESS");
          Assert.assertEquals("goo", value);
          done.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {
          System.err.println("FAILURE");
          ex.printStackTrace();
          done.countDown();
        }
      }));
      Assert.assertTrue(done.await(1000000, TimeUnit.MILLISECONDS));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally{
      clientBase.shutdown();
    }
  }
  */

  @Test
  public void quickclose() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.ClientTest1);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    WebClientBase clientBase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), webConfig);
    try {
      AtomicReference<WebClientConnection> connRef = new AtomicReference<>();
      CountDownLatch connectedLatch = new CountDownLatch(1);
      CountDownLatch disconnected = new CountDownLatch(1);
      clientBase.open(
          "http://localhost:" + webConfig.port + "/~s",
          new WebLifecycle() {
            @Override
            public void connected(WebClientConnection connection, String version) {
              connection.close();
              connectedLatch.countDown();
            }

            @Override
            public void ping(int latency) {

            }

            @Override
            public void failure(Throwable t) {}

            @Override
            public void disconnected() {
              disconnected.countDown();
            }
          });
      Assert.assertTrue(connectedLatch.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertTrue(disconnected.await(5000, TimeUnit.MILLISECONDS));
      runnable.shutdown();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally{
      clientBase.shutdown();
    }
  }

  @Test
  public void remoteCrash() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.ClientTest2);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    WebClientBase clientBase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), webConfig);
    try {
      AtomicReference<WebClientConnection> connRef = new AtomicReference<>();
      CountDownLatch connectedLatch = new CountDownLatch(1);
      CountDownLatch firstPing = new CountDownLatch(1);
      CountDownLatch disconnected = new CountDownLatch(1);
      clientBase.open(
          "http://localhost:" + webConfig.port + "/~s",
          new WebLifecycle() {
            @Override
            public void connected(WebClientConnection connection, String version) {
              connRef.set(connection);
              connection.execute(Json.parseJsonObject("{\"method\":\"crash\"}"), new LatchedWebJsonStream());
              connectedLatch.countDown();
            }

            @Override
            public void ping(int latency) {
              firstPing.countDown();
            }

            @Override
            public void failure(Throwable t) {
            }

            @Override
            public void disconnected() {
              disconnected.countDown();
            }
          });
      Assert.assertTrue(connectedLatch.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertTrue(disconnected.await(5000, TimeUnit.MILLISECONDS));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally{
      clientBase.shutdown();
    }
  }

  @Test
  public void localCrash() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.ClientTest3);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    WebClientBase clientBase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), webConfig);
    try {
      AtomicReference<WebClientConnection> connRef = new AtomicReference<>();
      CountDownLatch connectedLatch = new CountDownLatch(1);
      CountDownLatch firstPing = new CountDownLatch(1);
      CountDownLatch failure = new CountDownLatch(1);
      CountDownLatch disconnected = new CountDownLatch(1);
      clientBase.open(
          "http://localhost:" + webConfig.port + "/~s",
          new WebLifecycle() {
            @Override
            public void connected(WebClientConnection connection, String version) {
              connRef.set(connection);
              connection.execute(Json.parseJsonObject("{\"method\":\"cake\"}"), new WebJsonStream() {
                @Override
                public void data(int cId, ObjectNode node) {
                  throw new NullPointerException();
                }

                @Override
                public void complete() {

                }

                @Override
                public void failure(int code) {

                }
              });
              connectedLatch.countDown();
            }

            @Override
            public void ping(int latency) {
              firstPing.countDown();
            }

            @Override
            public void failure(Throwable t) {
              failure.countDown();
            }

            @Override
            public void disconnected() {
              disconnected.countDown();
            }
          });
      Assert.assertTrue(connectedLatch.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertTrue(failure.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertTrue(disconnected.await(5000, TimeUnit.MILLISECONDS));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally{
      clientBase.shutdown();
    }
  }

  @Test
  public void nope() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.ClientTest4);
    WebClientBase clientBase = new WebClientBase(new WebClientBaseMetrics(new NoOpMetricsFactory()), webConfig);
    try {
      AtomicReference<WebClientConnection> connRef = new AtomicReference<>();
      CountDownLatch failure = new CountDownLatch(1);
      CountDownLatch disconnected = new CountDownLatch(1);
      clientBase.open(
          "http://xyz.localhost.not.found:9999/s",
          new WebLifecycle() {
            @Override
            public void connected(WebClientConnection connection, String version) {
            }

            @Override
            public void ping(int latency) {}

            @Override
            public void failure(Throwable t) {
              failure.countDown();
            }

            @Override
            public void disconnected() {
              disconnected.countDown();
            }
          });
      Assert.assertTrue(failure.await(15000, TimeUnit.MILLISECONDS));
      Assert.assertTrue(disconnected.await(15000, TimeUnit.MILLISECONDS));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally{
      clientBase.shutdown();
    }
  }
}
