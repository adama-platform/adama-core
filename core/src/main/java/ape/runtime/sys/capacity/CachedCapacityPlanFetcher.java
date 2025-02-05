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
package ape.runtime.sys.capacity;

import ape.common.Callback;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.common.cache.AsyncSharedLRUCache;
import ape.common.cache.SyncCacheLRU;

import java.util.concurrent.atomic.AtomicBoolean;

/** fetches a capacity plan via a cache */
public class CachedCapacityPlanFetcher implements CapacityPlanFetcher {
  private final SyncCacheLRU<String, CapacityPlan> storage;
  private final AsyncSharedLRUCache<String, CapacityPlan> cache;

  public CachedCapacityPlanFetcher(TimeSource timeSource, int maxPlans, long maxAge, SimpleExecutor executor, CapacityPlanFetcher finder) {
    this.storage = new SyncCacheLRU<>(timeSource, 0, maxPlans, 1024L * maxPlans, maxAge, (name, record) -> {
    });
    this.cache = new AsyncSharedLRUCache<>(executor, storage, finder::fetch);
  }

  public void startSweeping(AtomicBoolean alive, int periodMinimumMs, int periodMaximumMs) {
    this.cache.startSweeping(alive, periodMinimumMs, periodMaximumMs);
  }

  @Override
  public void fetch(String space, Callback<CapacityPlan> callback) {
    cache.get(space, callback);
  }
}
