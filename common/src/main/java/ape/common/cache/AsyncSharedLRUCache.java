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
package ape.common.cache;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/** wrap a SyncCacheLRU */
public class AsyncSharedLRUCache<D, R extends Measurable> {
  public final SimpleExecutor executor;
  public final SyncCacheLRU<D, R> cache;
  public final BiConsumer<D, Callback<R>> resolver;
  public final HashMap<D, ArrayList<Callback<R>>> inflight;
  private final Random rng;

  public AsyncSharedLRUCache(SimpleExecutor executor, SyncCacheLRU<D, R> cache, BiConsumer<D, Callback<R>> resolver) {
    this.executor = executor;
    this.cache = cache;
    this.resolver = resolver;
    this.inflight = new HashMap<>();
    this.rng = new Random();
  }

  public void startSweeping(AtomicBoolean alive, int periodMinimumMs, int periodMaximumMs) {
    final int periodRange = Math.max(periodMaximumMs - periodMinimumMs, 10);
    executor.schedule(new NamedRunnable("async") {
      @Override
      public void execute() throws Exception {
        cache.sweep();
        if (alive.get()) {
          executor.schedule(this, periodMinimumMs + rng.nextInt(periodRange));
        }
      }
    }, periodMinimumMs);
  }

  public void get(D key, Callback<R> callback) {
    executor.execute(new NamedRunnable("ascache-get") {
      @Override
      public void execute() throws Exception {
        R cachedValue = cache.get(key);
        if (cachedValue != null) {
          callback.success(cachedValue);
          return;
        }

        ArrayList<Callback<R>> recent = inflight.get(key);
        if (recent == null) {
          recent = new ArrayList<>();
          inflight.put(key, recent);
          resolver.accept(key, new Callback<R>() {
            @Override
            public void success(R value) {
              executor.execute(new NamedRunnable("ascache-success") {
                @Override
                public void execute() throws Exception {
                  if (value != null) {
                    cache.add(key, value);
                  }
                  for (Callback<R> cb : inflight.remove(key)) {
                    cb.success(value);
                  }
                }
              });
            }

            @Override
            public void failure(ErrorCodeException ex) {
              executor.execute(new NamedRunnable("ascache-failure") {
                @Override
                public void execute() throws Exception {
                  for (Callback<R> cb : inflight.remove(key)) {
                    cb.failure(ex);
                  }
                }
              });
            }
          });
        }
        recent.add(callback);
      }
    });
  }

  public void forceEvictionFromCacheNoDownstreamEviction(D key) {
    executor.execute(new NamedRunnable("evict") {
      @Override
      public void execute() throws Exception {
        cache.forceEvictionFromCacheNoDownstreamEviction(key);
      }
    });
  }
}
