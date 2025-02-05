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
package ape.common.pool;

import ape.common.*;
import ape.common.gossip.MockTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncPoolTests {

  @Test
  public void battery_enforce_various_limits() {
    TimeSource time = new MockTime();
    ConnManager mgr = new ConnManager();
    AsyncPool<String, FauxConn> pool = new AsyncPool<>(SimpleExecutor.NOW, time, 5000, 5, 13, 1234, mgr);

    ArrayList<PoolItem<FauxConn>> values = new ArrayList<>();
    AtomicInteger errors = new AtomicInteger(0);
    for (int j = 0; j < 10; j++) {
      final int _j = j;
      for (int k = 0; k < 20; k++) {
        pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
          @Override
          public void success(PoolItem<FauxConn> value) {
            int prior = value.item().stuff;
            value.item().stuff++;
            Assert.assertEquals(_j % 5, prior);
            values.add(value);
          }

          @Override
          public void failure(ErrorCodeException ex) {
            errors.incrementAndGet();
          }
        });
      }
      Assert.assertEquals(7 * (1 + j), errors.get());
      for (PoolItem<FauxConn> value : values) {
        value.returnToPool();
      }
      values.clear();
    }
  }

  @Test
  public void failures() {
    TimeSource time = new MockTime();
    ConnManager mgr = new ConnManager();
    AsyncPool<String, FauxConn> pool = new AsyncPool<>(SimpleExecutor.NOW, time, 5000, 5, 13, 1234, mgr);
    AtomicInteger errors = new AtomicInteger(0);
    mgr.failure = true;
    pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
      @Override
      public void success(PoolItem<FauxConn> value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        errors.incrementAndGet();
      }
    });
    Assert.assertEquals(1, errors.get());
    mgr.failure = false;
    pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
      @Override
      public void success(PoolItem<FauxConn> value) {
        value.item().stuff = 123;
        value.signalFailure();
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    });

    pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
      @Override
      public void success(PoolItem<FauxConn> value) {
        Assert.assertEquals(0, value.item().stuff);
        value.item().stuff = 42;
        value.returnToPool();
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    });
    pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
      @Override
      public void success(PoolItem<FauxConn> value) {
        Assert.assertEquals(42, value.item().stuff);
        value.returnToPool();
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    });
  }

  @Test
  public void aging() {
    MockTime time = new MockTime();
    ConnManager mgr = new ConnManager();
    AsyncPool<String, FauxConn> pool = new AsyncPool<>(SimpleExecutor.NOW, time, 5000, 10000, 13, 1234, mgr);
    for (int k = 0; k < 100; k++) {
      pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
        @Override
        public void success(PoolItem<FauxConn> value) {
          value.item().stuff++;
          value.returnToPool();
          Assert.assertTrue(value.item().stuff <= 4);
        }

        @Override
        public void failure(ErrorCodeException ex) {
        }
      });
      time.currentTime += 1500;
    }
  }


  @Test
  public void death() {
    MockTime time = new MockTime();
    ConnManager mgr = new ConnManager();
    AsyncPool<String, FauxConn> pool = new AsyncPool<>(SimpleExecutor.NOW, time, 5000, 10000, 13, 1234, mgr);
    for (int k = 0; k < 100; k++) {
      pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
        @Override
        public void success(PoolItem<FauxConn> value) {
          value.item().stuff++;
          value.returnToPool();
          value.item().alive = false;
          Assert.assertTrue(value.item().stuff <= 1);
        }

        @Override
        public void failure(ErrorCodeException ex) {
        }
      });
    }
  }

  @Test
  public void max_usage() {
    MockTime time = new MockTime();
    ConnManager mgr = new ConnManager();
    AsyncPool<String, FauxConn> pool = new AsyncPool<>(SimpleExecutor.NOW, time, 5000, 5, 13, 1234, mgr);
    for (int k = 0; k < 100; k++) {
      pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
        @Override
        public void success(PoolItem<FauxConn> value) {
          value.item().stuff++;
          value.returnToPool();
          Assert.assertTrue(value.item().stuff <= 5);
        }

        @Override
        public void failure(ErrorCodeException ex) {
        }
      });
    }
  }

  @Test
  public void sweep() {
    MockTime time = new MockTime();
    ConnManager mgr = new ConnManager();
    AsyncPool<String, FauxConn> pool = new AsyncPool<>(SimpleExecutor.NOW, time, 5000, 5, 13, 1234, mgr);
    pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
      @Override
      public void success(PoolItem<FauxConn> value) {
        value.returnToPool();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    time.currentTime = 4999;
    Assert.assertEquals(0, pool.sweepInExecutor());
    time.currentTime = 5000;
    Assert.assertEquals(1, pool.sweepInExecutor());
    Assert.assertEquals(0, pool.sweepInExecutor());
  }

  @Test
  public void sweep_death() {
    MockTime time = new MockTime();
    ConnManager mgr = new ConnManager();
    AsyncPool<String, FauxConn> pool = new AsyncPool<>(SimpleExecutor.NOW, time, 5000, 5, 13, 1234, mgr);
    pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
      @Override
      public void success(PoolItem<FauxConn> value) {
        value.item().alive = false;
        value.returnToPool();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertEquals(1, pool.sweepInExecutor());
    Assert.assertEquals(0, pool.sweepInExecutor());
  }

  @Test
  public void schedule() throws Exception {
    ConnManager mgr = new ConnManager();
    SimpleExecutor executor = SimpleExecutor.create("test");
    try {
      AsyncPool<String, FauxConn> pool = new AsyncPool<>(executor, TimeSource.REAL_TIME, 5, 5, 13, 1234, mgr);
      pool.get("Hello World", new Callback<PoolItem<FauxConn>>() {
        @Override
        public void success(PoolItem<FauxConn> value) {
          value.returnToPool();
        }

        @Override
        public void failure(ErrorCodeException ex) {
        }
      });
      AtomicBoolean alive = new AtomicBoolean(true);
      pool.scheduleSweeping(alive);
      Thread.sleep(500);
      alive.set(false);
    } finally {
      executor.shutdown();
    }
  }

  public class FauxConn implements Living {
    public int stuff = 0;
    public boolean alive = true;

    @Override
    public boolean alive() {
      return alive;
    }
  }

  public class ConnManager implements PoolActions<String, FauxConn> {
    public boolean failure = false;

    @Override
    public void create(String request, Callback<FauxConn> created) {
      if (failure) {
        created.failure(new ErrorCodeException(-123));
        return;
      }
      created.success(new FauxConn());
    }

    @Override
    public void destroy(FauxConn item) {
      item.stuff = -1;
    }
  }
}
