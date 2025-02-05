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
package ape.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class GeneratedCodecMeTests {
  @Test
  public void flowA() {
    ByteBuf buf = Unpooled.buffer();
    {
      CodecCodeGenTests.TestClassA a = new CodecCodeGenTests.TestClassA();
      a.sssshort = 10000;
      a.str = "Howdy";
      a.x = 14000000;
      a.w = 1;
      a.z = 3.14;
      a.bbb = true;
      a.strarr = new String[]{"A", "B", "C"};
      GeneratedCodecMe.write(buf, a);
    }
    {
      CodecCodeGenTests.TestClassA a = GeneratedCodecMe.read_TestClassA(buf);
      Assert.assertEquals(10000, a.sssshort);
      Assert.assertEquals("Howdy", a.str);
      Assert.assertEquals(14000000, a.x);
      Assert.assertEquals(1, a.w, 0.00001);
      // Z is dropped due to it being an old field
      Assert.assertEquals(0.0, a.z, 0.00001);
      Assert.assertTrue(a.bbb);
      Assert.assertEquals(3, a.strarr.length);
      Assert.assertEquals("A", a.strarr[0]);
      Assert.assertEquals("B", a.strarr[1]);
      Assert.assertEquals("C", a.strarr[2]);
    }
  }

  @Test
  public void flowB() {
    ByteBuf buf = Unpooled.buffer();
    {
      CodecCodeGenTests.TestClassB b = new CodecCodeGenTests.TestClassB();
      b.x = 40;
      b.embed = new CodecCodeGenTests.TestClassA();
      b.embed.sssshort = 10000;
      b.embed.str = "Howdy";
      b.embed.x = 14000000;
      b.embed.w = 1;
      b.embed.z = 3.14;
      b.embed.bbb = false;
      b.embed.strarr = null;
      b.arr = new CodecCodeGenTests.TestClassA[]{b.embed};
      GeneratedCodecMe.write(buf, b);
    }
    {
      CodecCodeGenTests.TestClassB b = GeneratedCodecMe.read_TestClassB(buf);
      Assert.assertEquals(40, b.x);
      CodecCodeGenTests.TestClassA a = b.embed;
      Assert.assertEquals(10000, a.sssshort);
      Assert.assertEquals("Howdy", a.str);
      Assert.assertEquals(14000000, a.x);
      Assert.assertEquals(1, a.w, 0.00001);
      // Z is dropped due to it being an old field
      Assert.assertEquals(0.0, a.z, 0.00001);
      Assert.assertFalse(a.bbb);
      Assert.assertNull(a.strarr);
    }
  }

  @Test
  public void null_str() {
    ByteBuf buf = Unpooled.buffer();
    {
      CodecCodeGenTests.TestClassB b = new CodecCodeGenTests.TestClassB();
      b.x = 40;
      b.embed = new CodecCodeGenTests.TestClassA();
      b.embed.sssshort = 10000;
      b.embed.str = null;
      b.embed.x = 14000000;
      b.embed.w = 1;
      b.embed.z = 3.14;
      GeneratedCodecMe.write(buf, b);
    }
    {
      CodecCodeGenTests.TestClassB b = GeneratedCodecMe.read_TestClassB(buf);
      Assert.assertEquals(40, b.x);
      CodecCodeGenTests.TestClassA a = b.embed;
      Assert.assertEquals(10000, a.sssshort);
      Assert.assertNull(a.str);
      Assert.assertEquals(14000000, a.x);
      Assert.assertEquals(1, a.w, 0.00001);
      // Z is dropped due to it being an old field
      Assert.assertEquals(0.0, a.z, 0.00001);
    }
  }

  @Test
  public void null_object() {
    ByteBuf buf = Unpooled.buffer();

    {
      CodecCodeGenTests.TestClassB b = new CodecCodeGenTests.TestClassB();
      b.x = 40;
      b.embed = null;
      b.lng = -124;
      GeneratedCodecMe.write(buf, b);
    }
    {
      CodecCodeGenTests.TestClassB b = GeneratedCodecMe.read_TestClassB(buf);
      Assert.assertEquals(40, b.x);
      Assert.assertNull(b.embed);
      Assert.assertEquals(-124, b.lng);
    }
  }
}
