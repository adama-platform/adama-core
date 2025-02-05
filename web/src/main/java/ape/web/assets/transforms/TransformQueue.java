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
package ape.web.assets.transforms;

import ape.common.*;
import ape.ErrorCodes;
import ape.common.cache.AsyncSharedLRUCache;
import ape.common.cache.SyncCacheLRU;
import ape.runtime.data.Key;
import ape.runtime.natives.NtAsset;
import ape.web.assets.AssetRequest;
import ape.web.assets.AssetStream;
import ape.web.assets.AssetSystem;
import ape.web.assets.transforms.capture.DiskCapture;
import ape.web.assets.transforms.capture.InflightAsset;
import ape.web.assets.transforms.capture.MemoryCapture;

import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** A queue for executing transforms */
public class TransformQueue {
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(TransformQueue.class);
  private final TimeSource time;
  private final File transformRoot;
  private final SimpleExecutor executorCache;
  private final SimpleExecutor executorTransform;
  private final SimpleExecutor executorDisk;
  private final AtomicBoolean alive;
  private final SyncCacheLRU<TransformTask, TransformAsset> cache;
  private final AsyncSharedLRUCache<TransformTask, TransformAsset> async;
  private final AssetSystem assets;

  public TransformQueue(TimeSource time,  File transformRoot, AssetSystem assets) {
    this.time = time;
    this.transformRoot = transformRoot;
    this.executorCache = SimpleExecutor.create("transforms-cache");
    this.executorTransform = SimpleExecutor.create("transforms-transform");
    this.executorDisk = SimpleExecutor.create("transforms-disk");
    this.alive = new AtomicBoolean(true);
    this.assets = assets;
    this.cache = new SyncCacheLRU<>(time, 10, 10000, 1024L * 1024L * 1024L, 30 * 60000, (key, item) -> {
      item.evict();
    });
    this.async = new AsyncSharedLRUCache<>(executorCache, cache, (task, cb) -> {
      executorTransform.execute(new NamedRunnable("transform") {
        @Override
        public void execute() throws Exception {
          task.execute(cb);
        }
      });
    });
    this.async.startSweeping(alive, 45000, 90000);
  }

  public class TransformTask {
    public final Key key;
    public final String instruction;
    public final Transform transform;
    public final NtAsset asset;
    public final String hash;

    public TransformTask(Key key, String instruction, Transform transform, NtAsset asset, String hash) {
      this.key = key;
      this.instruction = instruction;
      this.transform = transform;
      this.asset = asset;
      this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TransformTask that = (TransformTask) o;
      return Objects.equals(key, that.key) && Objects.equals(instruction, that.instruction) && Objects.equals(asset, that.asset) && Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, instruction, asset, hash);
    }

    public void execute(Callback<TransformAsset> callback) {
      AssetRequest request = new AssetRequest(key.space, key.key, asset.id);
      Callback<InflightAsset> thingToTransform = new Callback<InflightAsset>() {
        @Override
        public void success(InflightAsset inflight) {
          executorTransform.execute(new NamedRunnable("running-transform") {
            @Override
            public void execute() throws Exception {
              File output = new File(transformRoot, asset.id + "." + hash + ".result");
              try {
                InputStream input = inflight.open();
                try {
                  transform.execute(input, output);
                  callback.success(new TransformAsset(executorDisk, output, asset.contentType));
                } finally {
                  input.close();
                }
              } catch (Exception ex) {
                callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.ASSET_TRANSFORM_FAILED_TRANSFORM, ex, EXLOGGER));
              }
              inflight.finished();
            }
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          callback.failure(ex);
        }
      };
      AssetStream streamToUse;
      if (asset.size < 1024 * 1024) {
        streamToUse = new MemoryCapture(thingToTransform);
      } else {
        streamToUse = new DiskCapture(executorDisk, asset, transformRoot, thingToTransform);
      }
      assets.request(request, streamToUse);
    }
  }

  public void process(Key key, String instruction, Transform transform, NtAsset asset, AssetStream response) {
    final String hash;
    {
      MessageDigest sha = Hashing.sha384();
      sha.update(instruction.getBytes());
      hash = Hashing.finishAndEncodeHex(sha);
    }

    TransformTask task = new TransformTask(key, instruction, transform, asset, hash);

    async.get(task, new Callback<TransformAsset>() {
      @Override
      public void success(TransformAsset result) {
        result.serve(response);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        response.failure(ex.code);
      }
    });
  }

  public void shutdown() {
    this.alive.set(false);
    try {
      this.executorCache.shutdown().await(1000, TimeUnit.MILLISECONDS);
    } catch (Exception ex) {
    }
    try {
      this.executorTransform.shutdown().await(1000, TimeUnit.MILLISECONDS);
    } catch (Exception ex) {
    }
    try {
      this.executorDisk.shutdown().await(1000, TimeUnit.MILLISECONDS);
    } catch (Exception ex) {
    }
  }
}
