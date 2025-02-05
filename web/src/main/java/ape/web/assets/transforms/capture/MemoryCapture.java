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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.ErrorCodes;
import ape.web.assets.AssetStream;

import java.io.*;

/** for small assets, we keep it in memory */
public class MemoryCapture implements AssetStream {
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(DiskCapture.class);
  private final Callback<InflightAsset> callback;
  private ByteArrayOutputStream output;
  private boolean finished;

  public MemoryCapture(Callback<InflightAsset> callback) {
    this.callback = callback;
    this.finished = false;
  }

  @Override
  public void headers(long length, String contentType, String contentMd5) {
    this.output = new ByteArrayOutputStream((int) length);
  }

  @Override
  public void body(byte[] chunk, int offset, int length, boolean last) {
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

  private void success() {
    if (finished) {
      return;
    }
    finished = true;
    byte[] memory = output.toByteArray();
    callback.success(new InflightAsset() {
      @Override
      public InputStream open() throws Exception {
        return new ByteArrayInputStream(memory);
      }

      @Override
      public void finished() {
        // nothing to clean up
      }
    });
  }

  private void fail(Exception ex) {
    if (finished) {
      return;
    }
    finished = true;
    callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.ASSET_TRANSFORM_DISK_CAPTURE_EXCEPTION, ex, EXLOGGER));
  }

  @Override
  public void failure(int code) {
    fail(new ErrorCodeException(code));
  }
}
