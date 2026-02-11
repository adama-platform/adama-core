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

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.runtime.natives.NtAsset;
import ape.web.assets.AssetStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Disk-backed cache entry for larger assets (up to 16MB).
 * Writes chunks to temp file as they arrive, supports concurrent readers
 * via file seek and replay. Late-joining streams receive already-written
 * data immediately then follow live writes. Cleans up temp file on eviction.
 */
public class FileCacheAsset implements CachedAsset {
  private static final Logger LOG = LoggerFactory.getLogger(FileCacheAsset.class);
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(LOG);
  private final NtAsset asset;
  private final SimpleExecutor executor;
  private ArrayList<AssetStream> streams;
  private final File filename;
  private final RandomAccessFile file;
  private long written;
  private boolean done;
  private boolean kill;
  private Integer failed;

  public FileCacheAsset(long localId, File cacheRoot, NtAsset asset, SimpleExecutor executor) throws ErrorCodeException {
    this.asset = asset;
    this.executor = executor;
    this.done = false;
    this.written = 0L;
    this.kill = false;
    this.failed = null;
    this.streams = new ArrayList<>();
    try {
      String name = "asset." + localId + "." + asset.id + ".cache";
      this.filename = new File(cacheRoot, name);
      this.file = new RandomAccessFile(filename, "rwd");
    } catch (Exception ex) {
      throw ErrorCodeException.detectOrWrap(ErrorCodes.CACHE_ASSET_FILE_FAILED_CREATE, ex, EXLOGGER);
    }
  }

  @Override
  public SimpleExecutor executor() {
    return executor;
  }

  private void pumpCurrent(AssetStream attach) {
    try {
      file.seek(0L);
      int rd;
      byte[] chunk = new byte[8192];
      long at = 0;
      while ((rd = file.read(chunk)) >= 0) {
        at += rd;
        byte[] copy = Arrays.copyOfRange(chunk, 0, rd);
        attach.body( copy, 0, copy.length, at == asset.size);
      }
    } catch (Exception ex) {
      attach.failure(ErrorCodeException.detectOrWrap(-123, ex, EXLOGGER).code);
    }
  }

  private void killWhileInExecutor() {
    try {
      file.close();
      filename.delete();
      if (failed == null) {
        failed = ErrorCodes.CACHE_ASSET_FILE_CLOSED_PRIOR_ATTACH;
      }
    } catch (Exception ignoreCacheLeak) {
      LOG.error("cache-leak", ignoreCacheLeak);
    }
  }

  @Override
  public AssetStream attachWhileInExecutor(AssetStream attach) {
    if (failed != null) {
      attach.failure(failed);
      return null;
    }
    attach.headers(asset.size, asset.contentType, asset.md5);
    if (done) { // the cache item has been fed, so simply replay what was captured
      pumpCurrent(attach);
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
          byte[] clone = Arrays.copyOfRange(chunk, offset, offset + length);

          // forward the body to all connected streams
          executor.execute(new NamedRunnable("mc-body") {
            @Override
            public void execute() throws Exception {
              // replicate the write to all attached streams
              for (AssetStream existing : streams) {
                existing.body(clone, 0, length, last);
              }

              try {
                file.seek(written);
                file.write(clone, 0, length);
                file.getFD().sync();
                written += length;
              } catch (Exception ex) {
                failure(ErrorCodes.CACHE_ASSET_FILE_FAILED_WRITE);
                return;
              }

              // if this is the last chunk, then set the done flag and clean things up
              if (last) {
                done = true;
                streams.clear();
                if (kill) {
                  killWhileInExecutor();
                }
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
              killWhileInExecutor();
            }
          });
        }
      };
    } else {
      pumpCurrent(attach);
      streams.add(attach);
      return null;
    }
  }

  @Override
  public void evict() {
    executor.execute(new NamedRunnable("mc-evict") {
      @Override
      public void execute() throws Exception {
        if (done) {
          killWhileInExecutor();
        } else {
          kill = true;
        }
      }
    });
  }

  @Override
  public long measure() {
    return asset.size;
  }
}
