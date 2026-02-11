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

import ape.common.metrics.NoOpMetricsFactory;
import ape.web.service.mocks.MockWellKnownHandler;
import ape.web.service.mocks.NullCertificateFinder;
import org.junit.Assert;
import org.junit.Test;

public class RedirectServiceRunnableTests {

  @Test
  public void test_interrupt() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.Mock1);
    final var runnable = new RedirectAndWellknownServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), new MockWellKnownHandler(), new NullCertificateFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    Assert.assertTrue(runnable.waitForReady(2500));
    Assert.assertTrue(runnable.isAccepting());
    thread.interrupt();
    thread.join();
    runnable.shutdown();
  }

  @Test
  public void test_shutdown() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.Mock2);
    final var runnable = new RedirectAndWellknownServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), new MockWellKnownHandler(), new NullCertificateFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    Assert.assertTrue(runnable.waitForReady(10000));
    Assert.assertTrue(runnable.isAccepting());
    runnable.shutdown();
    thread.join();
    runnable.shutdown();
  }

  @Test
  public void test_tight_shutdown() throws Exception {
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.Mock3);
    final var runnable = new RedirectAndWellknownServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), new MockWellKnownHandler(), new NullCertificateFinder(), () -> {});
    runnable.shutdown();
    final var thread = new Thread(runnable);
    thread.start();
    thread.join();
    runnable.shutdown();
  }
}
