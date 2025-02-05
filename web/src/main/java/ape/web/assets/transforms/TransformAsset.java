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

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.common.cache.Measurable;
import ape.web.assets.AssetStream;

import java.io.File;
import java.io.FileInputStream;

public class TransformAsset implements Measurable {
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(TransformAsset.class);
  private final SimpleExecutor executor;
  private final File cache;
  private byte[] serveFromMemory;
  private final String contentType;
  private final long size;

  public TransformAsset(SimpleExecutor executor, File cache, String contentType) {
    this.executor = executor;
    this.cache = cache;
    this.serveFromMemory = null;
    this.contentType = contentType;
    this.size = cache.length();
  }

  public void serve(AssetStream response) {
    executor.execute(new NamedRunnable("serve-transform-asset") {
      @Override
      public void execute() throws Exception {
        try {
          response.headers(size, contentType, null);
          FileInputStream input = new FileInputStream(cache);
          try {
            byte[] chunk = new byte[64 * 1024];
            long written = 0;
            int rd;
            while ((rd = input.read(chunk)) >= 0) {
              written += rd;
              response.body(chunk, 0, rd, written >= size);
              chunk = new byte[64 * 1024];
            }
          } finally {
            input.close();
          }
        } catch (Exception ex) {
          response.failure(ErrorCodeException.detectOrWrap(ErrorCodes.CACHE_ASSET_SERVE_FAILURE, ex, EXLOGGER).code);
        }
      }
    });
  }

  public void evict() {
    executor.schedule(new NamedRunnable("evict") {
      @Override
      public void execute() throws Exception {
        cache.delete();
      }
    }, 1000);
  }

  @Override
  public long measure() {
    return size;
  }
}
