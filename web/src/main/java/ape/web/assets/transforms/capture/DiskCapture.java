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
package ape.web.assets.transforms.capture;

import ape.common.*;
import ape.ErrorCodes;
import ape.runtime.natives.NtAsset;
import ape.web.assets.AssetStream;

import java.io.*;

/** for large assets, we store them on a disk using a random-ish name */
public class DiskCapture implements AssetStream {
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(DiskCapture.class);
  private final SimpleExecutor executor;
  private final NtAsset asset;
  private final File fileToUse;
  private final Callback<InflightAsset> callback;
  private FileOutputStream output;
  private boolean finished;

  public DiskCapture(SimpleExecutor executor, NtAsset asset, File transformCacheDirectory, Callback<InflightAsset> callback) {
    this.executor = executor;
    this.asset = asset;
    this.fileToUse = new File(transformCacheDirectory, asset.id() + "." + ProtectedUUID.generate() + ".cache");
    this.callback = callback;
    this.finished = false;
  }

  @Override
  public void headers(long length, String contentType, String contentMd5) {
    executor.execute(new NamedRunnable("open") {
      @Override
      public void execute() throws Exception {
        try {
          output = new FileOutputStream(fileToUse);
        } catch (Exception ex) {
          fail(ex);
        }
      }
    });
  }

  @Override
  public void body(byte[] chunk, int offset, int length, boolean last) {
    executor.execute(new NamedRunnable("body") {
      @Override
      public void execute() throws Exception {
        try {
          output.write(chunk, offset, length);
          if (last) {
            output.flush();
            output.close();
            success();
          }
        } catch (Exception ex) {
          fail(ex);
        }
      }
    });
  }

  private boolean raiseFinish(boolean cleanup) {
    if (finished) {
      return false;
    }
    if (cleanup) {
      AvoidExceptions.flushAndClose(output);
      AvoidExceptions.deleteFile(fileToUse);
    }
    finished = true;
    return true;
  }

  private void success() {
    if (raiseFinish(false)) {
      InflightAsset ia = new InflightAsset() {
        @Override
        public InputStream open() throws Exception {
          return new FileInputStream(fileToUse);
        }

        @Override
        public void finished() {
          fileToUse.delete();
        }
      };
      callback.success(ia);
    }
  }

  private void fail(Exception ex) {
    if (raiseFinish(true)) {
      callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.ASSET_TRANSFORM_DISK_CAPTURE_EXCEPTION, ex, EXLOGGER));
    }
  }

  @Override
  public void failure(int code) {
    executor.execute(new NamedRunnable("failure") {
      @Override
      public void execute() throws Exception {
        fail(new ErrorCodeException(code));
      }
    });
  }
}
