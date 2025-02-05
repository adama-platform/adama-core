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
import ape.common.SimpleExecutor;
import ape.runtime.natives.NtAsset;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DiskCaptureTests {
  private void body(Consumer<DiskCapture> body, int size, Callback<InflightAsset> callback) throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("disk");
    try {
      NtAsset asset = new NtAsset("assetid", "name", "text/plain", size, "md5", "sha");
      File temp = File.createTempFile("tr-pr", "post");
      temp.delete();
      temp.mkdirs();
      CountDownLatch finished = new CountDownLatch(1);
      try {
        body.accept(new DiskCapture(executor, asset, temp, new Callback<InflightAsset>() {
          @Override
          public void success(InflightAsset value) {
            callback.success(value);
            finished.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {
            callback.failure(ex);
            finished.countDown();
          }
        }));
        Assert.assertTrue(finished.await(50000, TimeUnit.MILLISECONDS));
      } finally {
        for (File f : temp.listFiles()) {
          f.delete();
        }
        temp.delete();
      }
    } finally {
      executor.shutdown().await(1000, TimeUnit.MILLISECONDS);
    }
  }

  @Test
  public void happy() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    Callback<InflightAsset> callback = new Callback<InflightAsset>() {
      @Override
      public void success(InflightAsset inflight) {
        try {
          InputStream input = inflight.open();
          String check = new String(input.readAllBytes(), StandardCharsets.UTF_8);
          Assert.assertEquals("hello world\nhello harry", check);
        } catch (Exception ex) {

        }
        inflight.finished();
        done.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        System.err.println("Failed:" + ex);
        done.countDown();
      }
    };

    byte[] write1 = "hello world\n".getBytes(StandardCharsets.UTF_8);
    byte[] write2 = "hello harry".getBytes(StandardCharsets.UTF_8);
    body((dc) -> {
      dc.headers(write1.length + write2.length, "text/merged", "md5");
      dc.body(write1, 0, write1.length, false);
      dc.body(write2, 0, write2.length, true);
    }, write1.length + write2.length, callback);
    Assert.assertTrue(done.await(10000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void sad_1_immediate() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    Callback<InflightAsset> callback = new Callback<InflightAsset>() {
      @Override
      public void success(InflightAsset inflight) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        System.err.println("Failed:" + ex);
        Assert.assertEquals(10000, ex.code);
        done.countDown();
      }
    };

    byte[] write1 = "hello world\n".getBytes(StandardCharsets.UTF_8);
    byte[] write2 = "hello harry".getBytes(StandardCharsets.UTF_8);
    body((dc) -> {
      dc.failure(10000);
    }, write1.length + write2.length, callback);
    Assert.assertTrue(done.await(10000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void sad_2_after_headers() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    Callback<InflightAsset> callback = new Callback<InflightAsset>() {
      @Override
      public void success(InflightAsset inflight) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        System.err.println("Failed:" + ex);
        Assert.assertEquals(10000, ex.code);
        done.countDown();
      }
    };

    byte[] write1 = "hello world\n".getBytes(StandardCharsets.UTF_8);
    byte[] write2 = "hello harry".getBytes(StandardCharsets.UTF_8);
    body((dc) -> {
      dc.headers(write1.length + write2.length, "text/merged", "md5");
      dc.failure(10000);
    }, write1.length + write2.length, callback);
    Assert.assertTrue(done.await(10000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void sad_3_after_headers_and_part_body() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    Callback<InflightAsset> callback = new Callback<InflightAsset>() {
      @Override
      public void success(InflightAsset inflight) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        System.err.println("Failed:" + ex);
        Assert.assertEquals(10000, ex.code);
        done.countDown();
      }
    };

    byte[] write1 = "hello world\n".getBytes(StandardCharsets.UTF_8);
    byte[] write2 = "hello harry".getBytes(StandardCharsets.UTF_8);
    body((dc) -> {
      dc.headers(write1.length + write2.length, "text/merged", "md5");
      dc.body(write1, 0, write1.length, false);
      dc.failure(10000);
    }, write1.length + write2.length, callback);
    Assert.assertTrue(done.await(10000, TimeUnit.MILLISECONDS));
  }
}
