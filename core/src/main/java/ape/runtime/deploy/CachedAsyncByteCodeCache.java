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
package ape.runtime.deploy;

import ape.common.Callback;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.common.cache.AsyncSharedLRUCache;
import ape.common.cache.SyncCacheLRU;

import java.util.concurrent.atomic.AtomicBoolean;

/** a herd protection mechanism for the compiler */
public class CachedAsyncByteCodeCache implements AsyncByteCodeCache {
  private final SyncCacheLRU<ByteCodeKey, CachedByteCode> storage;
  private final AsyncSharedLRUCache<ByteCodeKey, CachedByteCode> cache;

  public CachedAsyncByteCodeCache(TimeSource timeSource, int maxByteCodes, long maxAge, SimpleExecutor executor, AsyncByteCodeCache byteCodeCache) {
    this.storage = new SyncCacheLRU<>(timeSource, 0, maxByteCodes, 1024 * 1024L * maxByteCodes, maxAge, SyncCacheLRU.MAKE_NO_OP());
    this.cache = new AsyncSharedLRUCache<>(executor, storage, (key, cb) -> {
      byteCodeCache.fetchOrCompile(key.spaceName, key.className, key.javaSource, key.reflection, cb);
    });
  }

  public void startSweeping(AtomicBoolean alive, int periodMinimumMs, int periodMaximumMs) {
    this.cache.startSweeping(alive, periodMinimumMs, periodMaximumMs);
  }

  @Override
  public void fetchOrCompile(String spaceName, String className, String javaSource, String reflection, Callback<CachedByteCode> callback) {
    this.cache.get(new ByteCodeKey(spaceName, className, javaSource, reflection), callback);
  }
}
