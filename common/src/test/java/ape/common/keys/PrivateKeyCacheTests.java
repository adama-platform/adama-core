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
package ape.common.keys;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PrivateKeyCacheTests {
  @Test
  public void flow() throws Exception {
    ArrayList<NamedRunnable> runnables = new ArrayList<>();
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");
    PrivateKey one = keyPairGen.generateKeyPair().getPrivate();
    CountDownLatch latch = new CountDownLatch(4);
    PrivateKeyCache cache = new PrivateKeyCache(new SimpleExecutor() {
      @Override
      public void execute(NamedRunnable command) {
        runnables.add(command);
      }

      @Override
      public Runnable schedule(NamedRunnable command, long milliseconds) {
        return null;
      }

      @Override
      public Runnable scheduleNano(NamedRunnable command, long nanoseconds) {
        return null;
      }

      @Override
      public CountDownLatch shutdown() {
        return null;
      }
    }) {
      @Override
      protected PrivateKey find(PrivateKeyCache.SpaceKeyIdPair pair) {
        if (pair.space.equals("one")) {
          return one;
        }
        return null;
      }
    };
    cache.get("one", 1, new Callback<PrivateKey>() {
      @Override
      public void success(PrivateKey value) {
        Assert.assertNotNull(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    });
    cache.get("one", 1, new Callback<PrivateKey>() {
      @Override
      public void success(PrivateKey value) {
        Assert.assertNotNull(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    });
    cache.get("space", 1, new Callback<PrivateKey>() {
      @Override
      public void success(PrivateKey value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(643100, ex.code);
        latch.countDown();
      }
    });
    while (runnables.size() > 0) {
      runnables.remove(0).run();
    }
    cache.get("one", 1, new Callback<PrivateKey>() {
      @Override
      public void success(PrivateKey value) {
        Assert.assertNotNull(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
  }
}
