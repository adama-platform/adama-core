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
package ape.runtime.sys.web;

import org.junit.Assert;
import org.junit.Test;

public class WebFragmentTests {
  @Test
  public void coverage() {
    WebFragment fragment = new WebFragment("0123456789", "x", 5);
    Assert.assertEquals("56789", fragment.tail());
    Assert.assertNull(fragment.val_boolean);
    Assert.assertNull(fragment.val_double);
    Assert.assertNull(fragment.val_int);
    Assert.assertNull(fragment.val_long);
  }

  @Test
  public void parse_bool_true() {
    WebFragment fragment = new WebFragment("0123456789", "true", 5);
    Assert.assertEquals("56789", fragment.tail());
    Assert.assertNotNull(fragment.val_boolean);
    Assert.assertNull(fragment.val_double);
    Assert.assertNull(fragment.val_int);
    Assert.assertNull(fragment.val_long);
    Assert.assertTrue(fragment.val_boolean);
  }

  @Test
  public void parse_bool_false() {
    WebFragment fragment = new WebFragment("0123456789", "false", 5);
    Assert.assertEquals("56789", fragment.tail());
    Assert.assertNotNull(fragment.val_boolean);
    Assert.assertNull(fragment.val_double);
    Assert.assertNull(fragment.val_int);
    Assert.assertNull(fragment.val_long);
    Assert.assertFalse(fragment.val_boolean);
  }

  @Test
  public void parse_int() {
    WebFragment fragment = new WebFragment("0123456789", "123", 5);
    Assert.assertEquals("56789", fragment.tail());
    Assert.assertNull(fragment.val_boolean);
    Assert.assertNotNull(fragment.val_double);
    Assert.assertNotNull(fragment.val_int);
    Assert.assertNotNull(fragment.val_long);
    Assert.assertTrue((fragment.val_double - 123) < 0.01);
    Assert.assertEquals(123, (int) fragment.val_int);
    Assert.assertEquals(123L, (long) fragment.val_long);
  }

  @Test
  public void parse_long() {
    WebFragment fragment = new WebFragment("0123456789", "123123412344", 5);
    Assert.assertEquals("56789", fragment.tail());
    Assert.assertNull(fragment.val_boolean);
    Assert.assertNotNull(fragment.val_double);
    Assert.assertNull(fragment.val_int);
    Assert.assertNotNull(fragment.val_long);
    Assert.assertTrue((fragment.val_double - 1.23123412344E11) < 0.01);
    Assert.assertEquals(123123412344L, (long) fragment.val_long);
  }

  @Test
  public void parse_double() {
    WebFragment fragment = new WebFragment("0123456789", "42.69", 5);
    Assert.assertEquals("56789", fragment.tail());
    Assert.assertNull(fragment.val_boolean);
    Assert.assertNotNull(fragment.val_double);
    Assert.assertNull(fragment.val_int);
    Assert.assertNull(fragment.val_long);
    Assert.assertTrue((fragment.val_double - 42.69) < 0.01);
  }
}
