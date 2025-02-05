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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** this is a temporary file to figure out WTF is causing restores from timing out */
public class FileWriterHttpTimeoutTracker {
  private static final Logger LOG = LoggerFactory.getLogger(FileWriterHttpTimeoutTracker.class);
  private final AtomicBoolean finished;

  public final AtomicBoolean started;
  public final AtomicInteger started_status;
  public final AtomicBoolean body_start;
  public final AtomicLong body_size;
  public final AtomicLong left;
  public final AtomicBoolean body_end;
  public final AtomicInteger error;

  public FileWriterHttpTimeoutTracker() {
    this.finished = new AtomicBoolean(false);
    this.started = new AtomicBoolean(false);
    this.started_status = new AtomicInteger(0);
    this.body_start = new AtomicBoolean(false);
    this.body_size = new AtomicLong(0);
    this.left = new AtomicLong(0);
    this.body_end = new AtomicBoolean(false);
    this.error = new AtomicInteger(-1);
  }

  public void finish() {
    finished.set(true);
  }

  public void audit() {
    if (!finished.get()) {
      LOG.error("file-writer-not-finished: [started=" + started.get() + "] [status=" + started_status.get() + "] [bodystart=" + body_start.get() + "] [bodysize=" + body_size.get() + "] [left=" + left.get() + "] [end=" + body_end.get() + "] [error=" + error.get() + "]");
    }
  }
}
