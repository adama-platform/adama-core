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
package ape.runtime.natives;

import ape.runtime.exceptions.ComputeBlockedException;
import org.junit.Assert;
import org.junit.Test;

public class NtResultTests {
  @Test
  public void good() {
    NtResult<Integer> result = new NtResult<>(123, false, 0, null);
    Assert.assertEquals(123, (int) result.get());
    Assert.assertEquals(123, (int) result.as_maybe().get());
    Assert.assertTrue(result.finished());
    Assert.assertTrue(result.has());
    Assert.assertFalse(result.failed());
    Assert.assertEquals("OK", result.message());
    Assert.assertEquals(0, result.code());
    Assert.assertEquals(123, (int) result.await().get());
  }

  @Test
  public void copy_cons() {
    NtResult<Integer> result = new NtResult<>(new NtResult<>(123, false, 0, null));
    Assert.assertEquals(123, (int) result.get());
    Assert.assertEquals(123, (int) result.as_maybe().get());
    Assert.assertTrue(result.finished());
    Assert.assertTrue(result.has());
    Assert.assertFalse(result.failed());
    Assert.assertEquals("OK", result.message());
    Assert.assertEquals(0, result.code());
    Assert.assertEquals(123, (int) result.await().get());
  }

  @Test
  public void inprogress() {
    NtResult<Integer> result = new NtResult<>(null, false, 0, null);
    Assert.assertFalse(result.as_maybe().has());
    Assert.assertFalse(result.finished());
    Assert.assertFalse(result.failed());
    Assert.assertFalse(result.has());
    Assert.assertEquals("waiting...", result.message());
    Assert.assertEquals(0, result.code());
    try {
      result.await();
      Assert.fail();
    } catch (ComputeBlockedException cbe) {

    }
  }

  @Test
  public void bad() {
    NtResult<Integer> result = new NtResult<>(null, true, 500, "Failure");
    Assert.assertFalse(result.as_maybe().has());
    Assert.assertTrue(result.finished());
    Assert.assertTrue(result.failed());
    Assert.assertFalse(result.has());
    Assert.assertEquals("Failure", result.message());
    Assert.assertEquals(500, result.code());
    Assert.assertFalse(result.await().has());
  }
}
