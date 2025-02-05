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
package ape.web.client;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.metrics.Inflight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

/** write the body of an HTTP response to disk */
public class FileWriterHttpResponder implements SimpleHttpResponder {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileWriterHttpResponder.class);
  public final FileOutputStream output;
  public final Callback<Void> callback;
  private boolean good;
  private long left;
  private boolean checkSize;
  private final Inflight notFoundAlarm;
  private final FileWriterHttpTimeoutTracker tracker;

  public FileWriterHttpResponder(File fileToWrite, Inflight notFoundAlarm, FileWriterHttpTimeoutTracker tracker, Callback<Void> callback) throws ErrorCodeException {
    this.notFoundAlarm = notFoundAlarm;
    this.callback = callback;
    this.good = true;
    this.tracker = tracker;
    try {
      this.output = new FileOutputStream(fileToWrite);
    } catch (Exception ex) {
      LOGGER.error("failed-open", ex);
      throw new ErrorCodeException(ErrorCodes.WEB_BASE_FILE_WRITER_IOEXCEPTION_CREATE, ex);
    }
  }

  @Override
  public void start(SimpleHttpResponseHeader header) {
    tracker.started.set(true);
    tracker.started_status.set(header.status);
    if (good && header.status != 200) {
      this.good = false;
      if (header.status == 404) {
        notFoundAlarm.up();
      }
      HttpError.convert(header, LOGGER,  ErrorCodes.WEB_BASE_FILE_WRITER_NOT_200, null, callback);
    }
  }

  @Override
  public void bodyStart(long size) {
    tracker.body_start.set(true);
    tracker.body_size.set(size);
    this.left = size;
    this.checkSize = size >= 0;
    tracker.left.set(left);
  }

  @Override
  public void bodyFragment(byte[] chunk, int offset, int len) {
    if (write(output, chunk, offset, len, callback)) {
      if (good && checkSize) {
        left -= len;
        tracker.left.set(left);
      }
    } else {
      good = false;
    }
  }

  @Override
  public void bodyEnd() {
    tracker.body_end.set(true);
    if (finish(output, callback)) {
      if (good) {
        if (left == 0 || !checkSize) {
          callback.success(null);
        } else {
          callback.failure(new ErrorCodeException(ErrorCodes.WEB_BASE_FILE_WRITER_PREMATURE_END));
        }
      }
    } else {
      good = false;
    }
  }

  @Override
  public void failure(ErrorCodeException ex) {
    tracker.error.set(ex.code);
    if (good) {
      good = false;
      callback.failure(ex);
    }
  }

  public static boolean write(FileOutputStream output, byte[] chunk, int offset, int len, Callback<Void> callback) {
    try {
      output.write(chunk, offset, len);
      return true;
    } catch (Exception ioe) {
      callback.failure(new ErrorCodeException(ErrorCodes.WEB_BASE_FILE_WRITER_IOEXCEPTION_FRAGMENT, ioe));
      return false;
    }
  }

  public static boolean finish(FileOutputStream output, Callback<Void> callback) {
    try {
      output.flush();
      output.close();
      return true;
    } catch (Exception ioe) {
      callback.failure(new ErrorCodeException(ErrorCodes.WEB_BASE_FILE_WRITER_IOEXCEPTION_CLOSE, ioe));
      return false;
    }
  }
}
