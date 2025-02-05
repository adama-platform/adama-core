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
package ape.runtime.async;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import org.junit.Assert;
import org.junit.Test;

public class EphemeralFutureTests {

  private class DumbCallback implements Callback<Object> {
    boolean success = false;
    Object got = null;
    Integer code = null;

    @Override
    public void success(Object value) {
      if (success) {
        Assert.fail();
      }
      success = true;
      got = value;
    }

    @Override
    public void failure(ErrorCodeException ex) {
      if (success || code != null) {
        Assert.fail();
      }
      code = ex.code;
    }
  };

  @Test
  public void happy_a() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.attach(cb);
    future.send("Hi");
    Assert.assertTrue(cb.success);
    Assert.assertEquals("Hi", cb.got);
  }

  @Test
  public void happy_a_redun() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.attach(cb);
    future.send("Hi");
    Assert.assertTrue(cb.success);
    Assert.assertEquals("Hi", cb.got);
    future.send("X");
    future.cancel();
    Assert.assertTrue(cb.success);
    Assert.assertEquals("Hi", cb.got);
  }

  @Test
  public void happy_b() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.send("Hi");
    future.attach(cb);
    Assert.assertTrue(cb.success);
    Assert.assertEquals("Hi", cb.got);
  }

  @Test
  public void happy_b_redun() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.send("Hi");
    future.attach(cb);
    Assert.assertTrue(cb.success);
    Assert.assertEquals("Hi", cb.got);
    future.send("X");
    future.cancel();
    Assert.assertTrue(cb.success);
    Assert.assertEquals("Hi", cb.got);
  }

  @Test
  public void sad_a() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.cancel();
    future.send("Hi");
    future.attach(cb);
    Assert.assertFalse(cb.success);
    Assert.assertEquals(114384, (int) cb.code);
  }

  @Test
  public void abort_a() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.abort(123);
    future.attach(cb);
    Assert.assertFalse(cb.success);
    Assert.assertEquals(123, (int) cb.code);
  }

  @Test
  public void abort_b() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.attach(cb);
    future.abort(123);
    Assert.assertFalse(cb.success);
    Assert.assertEquals(123, (int) cb.code);
  }

  @Test
  public void sad_a_redun() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.cancel();
    future.send("Hi");
    future.attach(cb);
    future.send("X");
    future.cancel();
    Assert.assertFalse(cb.success);
    Assert.assertEquals(114384, (int) cb.code);
  }

  @Test
  public void sad_b() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.send("Hi");
    future.cancel();
    future.attach(cb);
    Assert.assertFalse(cb.success);
    Assert.assertEquals(114384, (int) cb.code);
  }

  @Test
  public void sad_c() {
    EphemeralFuture future = new EphemeralFuture();
    DumbCallback cb = new DumbCallback();
    future.attach(cb);
    future.cancel();
    future.send("Hi");
    Assert.assertFalse(cb.success);
    Assert.assertEquals(114384, (int) cb.code);
  }
}
