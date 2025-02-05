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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.metrics.Inflight;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class FileWriterHttpResponderTests {

  private final static Inflight alarm = new Inflight() {
    @Override
    public void up() {

    }

    @Override
    public void down() {

    }

    @Override
    public void set(int value) {

    }
  };

  @Test
  public void happy() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.start(new SimpleHttpResponseHeader(200, Collections.emptyMap()));
    writer.bodyStart(3);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    Assert.assertEquals(0, callbackValue.get());
    writer.bodyEnd();
    Assert.assertEquals(1, callbackValue.get());
    Assert.assertEquals("XYZ", Files.readString(file.toPath()));
  }

  @Test
  public void happyNoLenCheck() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.start(new SimpleHttpResponseHeader(200, Collections.emptyMap()));
    writer.bodyStart(-1);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    Assert.assertEquals(0, callbackValue.get());
    writer.bodyEnd();
    Assert.assertEquals(1, callbackValue.get());
    Assert.assertEquals("XYZ", Files.readString(file.toPath()));
  }

  @Test
  public void prematureEnd() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.start(new SimpleHttpResponseHeader(200, Collections.emptyMap()));
    writer.bodyStart(5);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    Assert.assertEquals(0, callbackValue.get());
    writer.bodyEnd();
    Assert.assertEquals(986319, callbackValue.get());
  }

  @Test
  public void not200_500() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.start(new SimpleHttpResponseHeader(500, Collections.emptyMap()));
    writer.bodyStart(5);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    Assert.assertEquals(903347, callbackValue.get());
    writer.bodyEnd();
    Assert.assertEquals(903347, callbackValue.get());
  }

  @Test
  public void not200_302() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.start(new SimpleHttpResponseHeader(302, Collections.emptyMap()));
    writer.bodyStart(5);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    Assert.assertEquals(991218, callbackValue.get());
    writer.bodyEnd();
    Assert.assertEquals(991218, callbackValue.get());
  }

  @Test
  public void not200_404() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.start(new SimpleHttpResponseHeader(404, Collections.emptyMap()));
    writer.bodyStart(5);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    Assert.assertEquals(986396, callbackValue.get());
    writer.bodyEnd();
    Assert.assertEquals(986396, callbackValue.get());
  }


  @Test
  public void not200_410() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.start(new SimpleHttpResponseHeader(410, Collections.emptyMap()));
    writer.bodyStart(5);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    Assert.assertEquals(984312, callbackValue.get());
    writer.bodyEnd();
    Assert.assertEquals(984312, callbackValue.get());
  }

  @Test
  public void failureProxy() throws Exception {
    AtomicInteger callbackValue = new AtomicInteger(0);
    File file = File.createTempFile("ADAMA_tempfile", "suffix");
    file.deleteOnExit();
    FileWriterHttpResponder writer = new FileWriterHttpResponder(file, alarm, new FileWriterHttpTimeoutTracker(), wrap(callbackValue));
    writer.failure(new ErrorCodeException(123));
    Assert.assertEquals(123, callbackValue.get());
    writer.start(new SimpleHttpResponseHeader(200, Collections.emptyMap()));
    writer.bodyStart(5);
    writer.bodyFragment("XYZ".getBytes(StandardCharsets.UTF_8), 0, 3);
    writer.bodyEnd();
    Assert.assertEquals(123, callbackValue.get());
  }

  @Test
  public void dumbCrash() throws Exception {
    try {
      new FileWriterHttpResponder(null, alarm, new FileWriterHttpTimeoutTracker(), null);
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(928944, ece.code);
    }
  }

  @Test
  public void dumbCoverageFinish() {
    AtomicInteger callbackValue = new AtomicInteger(0);
    FileWriterHttpResponder.finish(null, wrap(callbackValue));
    Assert.assertEquals(993487, callbackValue.get());
  }

  @Test
  public void dumbCoverageWrite() {
    AtomicInteger callbackValue = new AtomicInteger(0);
    FileWriterHttpResponder.write(null, null, 0, 0, wrap(callbackValue));
    Assert.assertEquals(913615, callbackValue.get());
  }

  public Callback<Void> wrap(AtomicInteger value) {
    return new Callback<Void>() {
      @Override
      public void success(Void x) {
        value.set(1);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
        value.set(ex.code);
      }
    };
  }
}
