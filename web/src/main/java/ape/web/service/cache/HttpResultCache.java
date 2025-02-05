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
package ape.web.service.cache;

import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.web.contracts.HttpHandler;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/** a very dumb temporal cache; no science */
public class HttpResultCache {
  public class Item {
    private final long at;
    private final HttpHandler.HttpResult result;

    public Item(long at, HttpHandler.HttpResult result) {
      this.at = at;
      this.result = result;
    }
  }

  private final TimeSource time;
  private final ConcurrentHashMap<String, Item> cache;

  public HttpResultCache(TimeSource time) {
    this.time = time;
    this.cache = new ConcurrentHashMap<>();
  }

  public BiConsumer<Integer, HttpHandler.HttpResult> inject(String key) {
    return (ttl, result) -> {
      if (result != null) {
        cache.put(key, new Item(time.nowMilliseconds() + ttl, result));
      }
    };
  }

  public HttpHandler.HttpResult get(String key) {
    Item item = cache.get(key);
    if (item == null) {
      return null;
    }

    if (item.at <= time.nowMilliseconds()) {
      cache.remove(key);
      return null;
    }
    return item.result;
  }

  public int sweep() {
    int count = 0;
    long now = time.nowMilliseconds();
    Iterator<Item> it = cache.values().iterator();
    while (it.hasNext()) {
      if (it.next().at <= now) {
        it.remove();
        count++;
      }
    }
    return count;
  }

  public static void sweeper(SimpleExecutor executor, AtomicBoolean alive, HttpResultCache cache, int minSweepMs, int maxSweepMs) {
    final Random rng = new Random();
    executor.schedule(new NamedRunnable("sweep cache") {
      @Override
      public void execute() throws Exception {
        if (alive.get()) {
          cache.sweep();
          executor.schedule(this, rng.nextInt(maxSweepMs - minSweepMs) + minSweepMs);
        }
      }
    }, rng.nextInt(maxSweepMs - minSweepMs) + minSweepMs);
  }
}
