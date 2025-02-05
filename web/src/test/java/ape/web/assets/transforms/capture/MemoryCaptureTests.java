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
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MemoryCaptureTests {
  @Test
  public void happy() throws Exception {
    CountDownLatch success = new CountDownLatch(1);
    MemoryCapture mc = new MemoryCapture(new Callback<InflightAsset>() {
      @Override
      public void success(InflightAsset inflight) {
        try {
          InputStream input = inflight.open();
          String check = new String(input.readAllBytes(), StandardCharsets.UTF_8);
          Assert.assertEquals("hello world\nhello harry", check);
          inflight.finished();
          success.countDown();
        } catch (Exception ex) {
        }
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    });
    byte[] write1 = "hello world\n".getBytes(StandardCharsets.UTF_8);
    byte[] write2 = "hello harry".getBytes(StandardCharsets.UTF_8);
    mc.headers(write1.length + write2.length, "text/merged", "md5");
    mc.body(write1, 0, write1.length, false);
    mc.body(write2, 0, write2.length, true);
    Assert.assertTrue(success.await(15000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void sad() throws Exception {
    CountDownLatch failed = new CountDownLatch(1);
    MemoryCapture mc = new MemoryCapture(new Callback<InflightAsset>() {
      @Override
      public void success(InflightAsset inflight) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(100000, ex.code);
        failed.countDown();
      }
    });
    mc.failure(100000);
    Assert.assertTrue(failed.await(15000, TimeUnit.MILLISECONDS));
  }
}
