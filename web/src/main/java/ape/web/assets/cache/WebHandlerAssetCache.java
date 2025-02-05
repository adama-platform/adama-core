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
package ape.web.assets.cache;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.common.cache.AsyncSharedLRUCache;
import ape.common.cache.SyncCacheLRU;
import ape.runtime.natives.NtAsset;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** the asset cache for the web handler */
public class WebHandlerAssetCache {
  private final SimpleExecutor memoryExecutor;
  private final SimpleExecutor fileExecutor;
  private final SyncCacheLRU<NtAsset, CachedAsset> memoryCache;
  private final AsyncSharedLRUCache<NtAsset, CachedAsset> memoryAsync;
  private final SyncCacheLRU<NtAsset, CachedAsset> fileCache;
  private final AsyncSharedLRUCache<NtAsset, CachedAsset> fileAsync;
  private final AtomicLong localId;
  private final AtomicBoolean alive;

  public WebHandlerAssetCache(TimeSource time, File cacheRoot) {
    this.alive = new AtomicBoolean(true);
    { // memory cache
      this.memoryExecutor = SimpleExecutor.create("webhandle-memcache");
      this.memoryCache = new SyncCacheLRU<>(time, 10, 2000, 64 * 1024 * 1024L, 10 * 60000, (key, mem) -> {
        mem.evict();
      });
      this.memoryAsync = new AsyncSharedLRUCache<>(memoryExecutor, memoryCache, (asset, cb) -> {
        cb.success(new MemoryCacheAsset(asset, memoryExecutor));
      });
      this.memoryAsync.startSweeping(alive, 45000, 90000);
    }
    this.localId = new AtomicLong(0);
    if (cacheRoot.exists()) {
      for (File prior : cacheRoot.listFiles((dir, name) -> name.endsWith(".cache") && name.startsWith("asset."))) {
        prior.delete();
      }
    } else {
      cacheRoot.mkdirs();
    }
    { // file cache
      this.fileExecutor = SimpleExecutor.create("webhandle-file");
      this.fileCache = new SyncCacheLRU<>(time, 25, 500, 1024 * 1024 * 1024L, 20 * 60000, (key, file) -> {
        file.evict();
      });
      this.fileAsync = new AsyncSharedLRUCache<>(fileExecutor, fileCache, (asset, cb) -> {
        try {
          cb.success(new FileCacheAsset(localId.getAndIncrement(), cacheRoot, asset, fileExecutor));
        } catch (ErrorCodeException ece) {
          cb.failure(ece);
        }
      });
      this.fileAsync.startSweeping(alive, 45000, 90000);
    }
  }

  public static boolean policyCacheMemory(NtAsset asset) {
    // TODO: expand policy
    boolean isHtml = asset.contentType.equals("text/html");
    boolean isCSS = asset.contentType.equals("text/css");
    boolean isJavaScript = asset.contentType.equals("text/javascript");
    if (asset.size < 196 * 1024 && (isHtml || isCSS || isJavaScript)) {
      return true;
    }
    return false;
  }

  public static boolean policyCacheDisk(NtAsset asset) {
    return asset.size < 16 * 1024 * 1024;
  }

  public static boolean canCache(NtAsset asset) {
    return policyCacheMemory(asset) || policyCacheDisk(asset);
  }

  public void get(NtAsset asset, Callback<CachedAsset> callback) {
    if (policyCacheMemory(asset)) {
      memoryAsync.get(asset, callback);
    } else {
      fileAsync.get(asset, callback);
    }
  }

  public void failure(NtAsset asset) {
    memoryAsync.forceEvictionFromCacheNoDownstreamEviction(asset);
    fileAsync.forceEvictionFromCacheNoDownstreamEviction(asset);
  }

  public void shutdown() {
    alive.set(false);
    try {
      memoryExecutor.shutdown();
    } catch (Exception ex) {
    }
    try {
      fileExecutor.shutdown();
    } catch (Exception ex) {
    }
  }
}
