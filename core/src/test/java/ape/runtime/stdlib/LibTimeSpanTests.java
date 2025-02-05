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
package ape.runtime.stdlib;

import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtTimeSpan;
import org.junit.Assert;
import org.junit.Test;

public class LibTimeSpanTests {
  @Test
  public void basics() {
    NtTimeSpan a = new NtTimeSpan(4.1);
    NtTimeSpan b = new NtTimeSpan(50.5);
    NtTimeSpan c = LibTimeSpan.add(a, b);
    Assert.assertEquals(54.6, c.seconds, 0.01);
    NtTimeSpan d = LibTimeSpan.multiply(c, 4.2);
    Assert.assertEquals(229.32, d.seconds, 0.01);
    Assert.assertEquals(229.32, LibTimeSpan.seconds(d), 0.01);
    Assert.assertEquals(3.822, LibTimeSpan.minutes(d), 0.01);
    Assert.assertEquals(0.0637, LibTimeSpan.hours(d), 0.01);
    Assert.assertEquals(229.32, LibTimeSpan.seconds(new NtMaybe<>(d)).get(), 0.01);
    Assert.assertEquals(3.822, LibTimeSpan.minutes(new NtMaybe<>(d)).get(), 0.01);
    Assert.assertEquals(0.0637, LibTimeSpan.hours(new NtMaybe<>(d)).get(), 0.01);
    Assert.assertFalse(LibTimeSpan.seconds(new NtMaybe<>()).has());
    Assert.assertFalse(LibTimeSpan.minutes(new NtMaybe<>()).has());
    Assert.assertFalse(LibTimeSpan.hours(new NtMaybe<>()).has());
  }

  @Test
  public void make() {
    Assert.assertEquals(100, LibTimeSpan.makeFromSeconds(100.0).seconds, 0.01);
    Assert.assertEquals(100, LibTimeSpan.makeFromSeconds((int) 100).seconds, 0.01);
    Assert.assertEquals(6000, LibTimeSpan.makeFromMinutes(100.0).seconds, 0.01);
    Assert.assertEquals(6000, LibTimeSpan.makeFromMinutes((int) 100).seconds, 0.01);
  }
}
