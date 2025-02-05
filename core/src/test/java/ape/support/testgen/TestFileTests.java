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
package ape.support.testgen;

import org.junit.Assert;
import org.junit.Test;

public class TestFileTests {
  @Test
  public void happy() {
    final var tst = new TestFile("Z", "x", true);
    Assert.assertEquals("Z_x_success.a", tst.filename());
    final var tst2 = TestFile.fromFilename(tst.filename());
    Assert.assertEquals("Z", tst.clazz);
    Assert.assertEquals("x", tst.name);
    Assert.assertTrue(tst2.success);
  }

  @Test
  public void sad() {
    final var tst = new TestFile("Z", "x", false);
    Assert.assertEquals("Z_x_failure.a", tst.filename());
    final var tst2 = TestFile.fromFilename(tst.filename());
    Assert.assertEquals("Z", tst2.clazz);
    Assert.assertEquals("x", tst2.name);
    Assert.assertFalse(tst2.success);
  }

  @Test
  public void failure() {
    try {
      new TestFile("Z", "x_x", false);
      Assert.fail();
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains("underscore"));
    }
  }
}
