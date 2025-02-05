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
package ape.runtime.sys.cron;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.runtime.data.Key;
import ape.runtime.mocks.MockKeyAlarm;
import ape.runtime.mocks.MockWakeService;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InMemoryWakeProxyTests {
  @Test
  public void flow() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("simple");
    MockKeyAlarm mockKeyAlarm = new MockKeyAlarm();
    MockWakeService mockWakeService = new MockWakeService();
    try {
      CountDownLatch latch = new CountDownLatch(2);
      InMemoryWakeProxy proxy = new InMemoryWakeProxy(executor, new KeyAlarm() {
        @Override
        public void wake(Key key) {
          mockKeyAlarm.wake(key);
          latch.countDown();
        }
      }, mockWakeService);
      proxy.wakeIn(new Key("s", "k"), 15, new Callback<Void>() {
        @Override
        public void success(Void value) {
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {}
      });
      Assert.assertTrue(latch.await(50000, TimeUnit.MILLISECONDS));
      Assert.assertEquals("WAKE:s/k@15", mockWakeService.get(0));
      Assert.assertEquals("ALARM:s/k", mockKeyAlarm.get(0));
    } finally {
      executor.shutdown();
    }
  }
}
