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
package ape.common;

import org.junit.Assert;
import org.junit.Test;

public class ValidatorsTests {
  @Test
  public void flow() {
    Assert.assertFalse(Validators.simple("1234", 3));
    Assert.assertFalse(Validators.simple("$!@", 43));
    Assert.assertTrue(Validators.simple("x1234", 1000));
    Assert.assertTrue(Validators.simple("1234", 1000));
    Assert.assertTrue(Validators.simple("ninja-cake", 1000));
  }

  @Test
  public void nullInput() {
    Assert.assertFalse(Validators.simple(null, 100));
  }

  @Test
  public void emptyString() {
    Assert.assertFalse(Validators.simple("", 100));
  }

  @Test
  public void exactMaxLength() {
    Assert.assertTrue(Validators.simple("abc", 3));
    Assert.assertFalse(Validators.simple("abcd", 3));
  }

  @Test
  public void allowedSpecialChars() {
    Assert.assertTrue(Validators.simple("a.b", 100));
    Assert.assertTrue(Validators.simple("a-b", 100));
    Assert.assertTrue(Validators.simple("a_b", 100));
    Assert.assertTrue(Validators.simple("A.B-C_D", 100));
  }

  @Test
  public void disallowedChars() {
    Assert.assertFalse(Validators.simple("a b", 100));
    Assert.assertFalse(Validators.simple("a/b", 100));
    Assert.assertFalse(Validators.simple("a@b", 100));
    Assert.assertFalse(Validators.simple("a#b", 100));
    Assert.assertFalse(Validators.simple("a&b", 100));
    Assert.assertFalse(Validators.simple("a+b", 100));
    Assert.assertFalse(Validators.simple("a=b", 100));
    Assert.assertFalse(Validators.simple("a<b", 100));
    Assert.assertFalse(Validators.simple("a>b", 100));
    Assert.assertFalse(Validators.simple("a;b", 100));
    Assert.assertFalse(Validators.simple("a:b", 100));
  }

  @Test
  public void allDigits() {
    Assert.assertTrue(Validators.simple("0123456789", 100));
  }

  @Test
  public void allUpperCase() {
    Assert.assertTrue(Validators.simple("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 100));
  }

  @Test
  public void allLowerCase() {
    Assert.assertTrue(Validators.simple("abcdefghijklmnopqrstuvwxyz", 100));
  }
}
