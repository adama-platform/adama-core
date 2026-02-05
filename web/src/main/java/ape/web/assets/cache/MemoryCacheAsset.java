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

import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.runtime.natives.NtAsset;
import ape.web.assets.AssetStream;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * In-memory cache entry for small web assets (HTML, CSS, JS under 196KB).
 * Buffers incoming chunks in ByteArrayOutputStream while forwarding to
 * attached streams. Late-joining streams receive buffered data immediately
 * then follow live writes. No cleanup needed on eviction.
 */
public class MemoryCacheAsset implements CachedAsset {
  private final NtAsset asset;
  private final SimpleExecutor executor;
  private ByteArrayOutputStream memory;
  private boolean done;
  private ArrayList<AssetStream> streams;
  private Integer failed;

  public MemoryCacheAsset(NtAsset asset, SimpleExecutor executor) {
    this.asset = asset;
    this.executor = executor;
    this.memory = new ByteArrayOutputStream();
    this.done = false;
    this.streams = new ArrayList<>();
    this.failed = null;
  }

  @Override
  public SimpleExecutor executor() {
    return executor;
  }

  @Override
  public void evict() {
    // no-op
  }

  @Override
  public AssetStream attachWhileInExecutor(AssetStream attach) {
    if (failed != null) {
      attach.failure(failed);
      return null;
    }
    attach.headers(asset.size, asset.contentType, asset.md5);
    if (done) { // the cache item has been fed, so simply replay what was captured
      byte[] body = memory.toByteArray();
      attach.body(body, 0, body.length, done);
      return null;
    }
    // otherwise, we need to wait for data...
    if (streams.size() == 0) {
      // this is the first attach call which is special; return the asset stream to pump both attach and late-joiners
      streams.add(attach);
      return new AssetStream() {
        @Override
        public void headers(long length, String contentType, String md5) {
          // the headers were already transmitted
        }

        @Override
        public void body(byte[] chunk, int offset, int length, boolean last) {
          byte[] clone = Arrays.copyOfRange(chunk, offset, length); // TODO: this signature of byte[] chunk is... bad

          // forward the body to all connected streams
          executor.execute(new NamedRunnable("mc-body") {
            @Override
            public void execute() throws Exception {
              // replicate the write to all attached streams
              for (AssetStream existing : streams) {
                existing.body(clone, 0, length, last);
              }
              // record the chunk to memory
              memory.write(clone, 0, length);
              // if this is the last chunk, then set the done flag and clean things up
              if (last) {
                done = true;
                streams.clear();
              }
            }
          });
        }

        @Override
        public void failure(int code) {
          executor.execute(new NamedRunnable("mc-failure") {
            @Override
            public void execute() throws Exception {
              failed = code;
              for (AssetStream existing : streams) {
                existing.failure(code);
              }
              streams.clear();
            }
          });
        }
      };
    } else {
      // another stream has started pumping data, so we simply need to replay what has already been record
      byte[] body = memory.toByteArray();
      attach.body(body, 0, body.length, false);
      streams.add(attach);
      return null;
    }
  }

  @Override
  public long measure() {
    return asset.size;
  }
}
