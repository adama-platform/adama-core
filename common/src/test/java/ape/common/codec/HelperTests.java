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

public class HelperTests {
  @Test
  public void intarray() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeIntArray(buf, new int[]{1, 2, 4});
    int[] arr = Helper.readIntArray(buf);
    Assert.assertEquals(1, arr[0]);
    Assert.assertEquals(2, arr[1]);
    Assert.assertEquals(4, arr[2]);
  }

  @Test
  public void intarray_null() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeIntArray(buf, null);
    Assert.assertNull(Helper.readIntArray(buf));
  }

  @Test
  public void stringRoundTrip() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeString(buf, "hello world");
    Assert.assertEquals("hello world", Helper.readString(buf));
  }

  @Test
  public void stringNull() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeString(buf, null);
    Assert.assertNull(Helper.readString(buf));
  }

  @Test
  public void stringEmpty() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeString(buf, "");
    Assert.assertEquals("", Helper.readString(buf));
  }

  @Test
  public void stringArrayRoundTrip() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeStringArray(buf, new String[]{"a", "b", "c"});
    String[] arr = Helper.readStringArray(buf);
    Assert.assertEquals(3, arr.length);
    Assert.assertEquals("a", arr[0]);
    Assert.assertEquals("b", arr[1]);
    Assert.assertEquals("c", arr[2]);
  }

  @Test
  public void stringArrayNull() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeStringArray(buf, null);
    Assert.assertNull(Helper.readStringArray(buf));
  }

  @Test
  public void stringArrayWithNullElement() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeStringArray(buf, new String[]{"a", null, "c"});
    String[] arr = Helper.readStringArray(buf);
    Assert.assertEquals(3, arr.length);
    Assert.assertEquals("a", arr[0]);
    Assert.assertNull(arr[1]);
    Assert.assertEquals("c", arr[2]);
  }

  @Test
  public void stringArrayEmpty() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeStringArray(buf, new String[]{});
    String[] arr = Helper.readStringArray(buf);
    Assert.assertEquals(0, arr.length);
  }

  @Test
  public void genericArrayRoundTrip() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeArray(buf, new String[]{"x", "y"}, s -> Helper.writeString(buf, s));
    String[] arr = Helper.readArray(buf, String[]::new, () -> Helper.readString(buf));
    Assert.assertEquals(2, arr.length);
    Assert.assertEquals("x", arr[0]);
    Assert.assertEquals("y", arr[1]);
  }

  @Test
  public void genericArrayNull() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeArray(buf, null, s -> {});
    Assert.assertNull(Helper.readArray(buf, String[]::new, () -> null));
  }

  @Test
  public void genericArrayEmpty() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeArray(buf, new String[]{}, s -> Helper.writeString(buf, s));
    String[] arr = Helper.readArray(buf, String[]::new, () -> Helper.readString(buf));
    Assert.assertEquals(0, arr.length);
  }

  @Test
  public void intArrayEmpty() {
    ByteBuf buf = Unpooled.buffer();
    Helper.writeIntArray(buf, new int[]{});
    int[] arr = Helper.readIntArray(buf);
    Assert.assertEquals(0, arr.length);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readArrayOversizedCount() {
    ByteBuf buf = Unpooled.buffer();
    buf.writeIntLE(Helper.MAX_ARRAY_SIZE + 2); // count = MAX_ARRAY_SIZE + 1 after -1
    Helper.readArray(buf, String[]::new, () -> "");
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readStringArrayOversizedCount() {
    ByteBuf buf = Unpooled.buffer();
    buf.writeIntLE(Helper.MAX_ARRAY_SIZE + 2);
    Helper.readStringArray(buf);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readIntArrayOversizedCount() {
    ByteBuf buf = Unpooled.buffer();
    buf.writeIntLE(Helper.MAX_ARRAY_SIZE + 2);
    Helper.readIntArray(buf);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readStringOversizedLength() {
    ByteBuf buf = Unpooled.buffer();
    buf.writeIntLE(Helper.MAX_STRING_SIZE + 2);
    Helper.readString(buf);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readArrayNegativeCount() {
    ByteBuf buf = Unpooled.buffer();
    buf.writeIntLE(-1); // count - 1 = -2, negative
    Helper.readArray(buf, String[]::new, () -> "");
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readStringNegativeLength() {
    ByteBuf buf = Unpooled.buffer();
    buf.writeIntLE(-1);
    Helper.readString(buf);
  }
}
