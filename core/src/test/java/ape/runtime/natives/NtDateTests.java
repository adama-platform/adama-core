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

import org.junit.Assert;
import org.junit.Test;

public class NtDateTests {
  @Test
  public void coverage() {
    NtDate d = new NtDate(2010, 11, 22);
    Assert.assertEquals(d, d);
    Assert.assertEquals(d, new NtDate(2010, 11, 22));
    Assert.assertNotEquals(d, new NtDate(2222, 11, 22));
    Assert.assertNotEquals(d, new NtDate(2010, 7, 22));
    Assert.assertNotEquals(d, new NtDate(2010, 11, 18));
    Assert.assertNotEquals(d, "");
    Assert.assertNotEquals(d, null);
    d.hashCode();
    Assert.assertEquals("2010-11-22", d.toString());
    Assert.assertEquals(24, d.memory());
  }

  @Test
  public void toint() {
    NtDate d = new NtDate(2010, 11, 22);
    Assert.assertEquals(23541494, d.toInt());
  }

  @Test
  public void compare_year() {
    Assert.assertEquals(0, new NtDate(2010, 11, 22).compareTo(new NtDate(2010, 11, 22)));
    Assert.assertEquals(-1, new NtDate(2010, 11, 22).compareTo(new NtDate(2011, 11, 22)));
    Assert.assertEquals(1, new NtDate(2011, 11, 22).compareTo(new NtDate(2010, 11, 22)));
  }

  @Test
  public void compare_month() {
    Assert.assertEquals(-1, new NtDate(2010, 10, 22).compareTo(new NtDate(2010, 11, 22)));
    Assert.assertEquals(1, new NtDate(2010, 12, 22).compareTo(new NtDate(2010, 11, 22)));
  }

  @Test
  public void compare_day() {
    Assert.assertEquals(-1, new NtDate(2010, 11, 21).compareTo(new NtDate(2010, 11, 22)));
    Assert.assertEquals(1, new NtDate(2010, 11, 23).compareTo(new NtDate(2010, 11, 22)));
  }
}
