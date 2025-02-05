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

import ape.runtime.natives.NtTime;
import ape.runtime.natives.NtTimeSpan;
import org.junit.Assert;
import org.junit.Test;

public class LibTimeTests {
  @Test
  public void overlaps() {
    Assert.assertTrue(LibTime.overlaps(new NtTime(13, 23), new NtTime(14, 50), new NtTime(13, 40), new NtTime(19, 00)));
    Assert.assertFalse(LibTime.overlaps(new NtTime(13, 23), new NtTime(14, 50), new NtTime(15, 40), new NtTime(19, 00)));
  }

  @Test
  public void extendWithDay_battery() {
    {
      NtTime x = LibTime.extendWithinDay(new NtTime(13, 00), new NtTimeSpan(70));
      Assert.assertEquals(13 * 60 + 1, LibTime.toInt(x));
      Assert.assertEquals(13, x.hour);
      Assert.assertEquals(1, x.minute);
    }
    {
      NtTime x = LibTime.extendWithinDay(new NtTime(13, 00), new NtTimeSpan(24 * 60 * 60 * 4));
      Assert.assertEquals(24 * 60 - 1, LibTime.toInt(x));
      Assert.assertEquals(23, x.hour);
      Assert.assertEquals(59, x.minute);
    }
    {
      NtTime x = LibTime.extendWithinDay(new NtTime(13, 00), new NtTimeSpan(-24 * 60 * 60 * 4));
      Assert.assertEquals(0, LibTime.toInt(x));
      Assert.assertEquals(0, x.hour);
      Assert.assertEquals(0, x.minute);
    }
  }

  @Test
  public void cylicAdd_Battery() {
    {
      NtTime x = LibTime.cyclicAdd(new NtTime(13, 00), new NtTimeSpan(70));
      Assert.assertEquals(13 * 60 + 1, LibTime.toInt(x));
      Assert.assertEquals(13, x.hour);
      Assert.assertEquals(1, x.minute);
    }
    {
      NtTime x = LibTime.cyclicAdd(new NtTime(13, 00), new NtTimeSpan(24 * 60 * 60 * 4));
      Assert.assertEquals(940, LibTime.toInt(x));
      Assert.assertEquals(15, x.hour);
      Assert.assertEquals(40, x.minute);
    }
    {
      NtTime x = LibTime.cyclicAdd(new NtTime(13, 00), new NtTimeSpan(-24 * 60 * 60 * 4));
      Assert.assertEquals(620, LibTime.toInt(x));
      Assert.assertEquals(10, x.hour);
      Assert.assertEquals(20, x.minute);
    }
  }

  @Test
  public void make() {
    NtTime t = LibTime.make(23, 59).get();
    Assert.assertEquals(23, t.hour);
    Assert.assertEquals(59, t.minute);

    Assert.assertFalse(LibTime.make(42, 1).has());
    Assert.assertFalse(LibTime.make(-42, 1).has());
    Assert.assertFalse(LibTime.make(1, 90).has());
    Assert.assertFalse(LibTime.make(1, -90).has());
  }
}
