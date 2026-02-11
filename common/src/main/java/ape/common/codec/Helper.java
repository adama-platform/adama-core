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

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Codec helper for reading/writing arrays and strings to Netty ByteBuf buffers. */
public class Helper {
  /** Maximum number of elements allowed when deserializing arrays. */
  public static final int MAX_ARRAY_SIZE = 4 * 1024 * 1024;
  /** Maximum byte length allowed when deserializing strings (16 MB). */
  public static final int MAX_STRING_SIZE = 16 * 1024 * 1024;

  public static <T> T[] readArray(ByteBuf buf, Function<Integer, T[]> maker, Supplier<T> read) {
    int count = buf.readIntLE();
    if (count == 0) {
      return null;
    }
    int size = count - 1;
    if (size < 0 || size > MAX_ARRAY_SIZE) {
      throw new IndexOutOfBoundsException("array size out of bounds: " + size);
    }
    T[] arr = maker.apply(size);
    for (int k = 0; k < arr.length; k++) {
      arr[k] = read.get();
    }
    return arr;
  }

  public static <T> void writeArray(ByteBuf buf, T[] arr, Consumer<T> write) {
    if (arr == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(arr.length + 1);
    for (int k = 0; k < arr.length; k++) {
      write.accept(arr[k]);
    }
  }

  public static String[] readStringArray(ByteBuf buf) {
    int count = buf.readIntLE();
    if (count == 0) {
      return null;
    }
    int size = count - 1;
    if (size < 0 || size > MAX_ARRAY_SIZE) {
      throw new IndexOutOfBoundsException("string array size out of bounds: " + size);
    }
    String[] arr = new String[size];
    for (int k = 0; k < arr.length; k++) {
      arr[k] = readString(buf);
    }
    return arr;
  }

  public static String readString(ByteBuf buf) {
    int count = buf.readIntLE();
    if (count == 0) {
      return null;
    }
    int length = count - 1;
    if (length < 0 || length > MAX_STRING_SIZE) {
      throw new IndexOutOfBoundsException("string length out of bounds: " + length);
    }
    byte[] bytes = new byte[length];
    buf.readBytes(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public static void writeStringArray(ByteBuf buf, String[] strs) {
    if (strs == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(strs.length + 1);
    for (int k = 0; k < strs.length; k++) {
      writeString(buf, strs[k]);
    }
  }

  public static void writeString(ByteBuf buf, String str) {
    if (str == null) {
      buf.writeIntLE(0);
      return;
    }
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    buf.writeIntLE(bytes.length + 1);
    buf.writeBytes(bytes);
  }


  public static int[] readIntArray(ByteBuf buf) {
    int count = buf.readIntLE();
    if (count == 0) {
      return null;
    }
    int size = count - 1;
    if (size < 0 || size > MAX_ARRAY_SIZE) {
      throw new IndexOutOfBoundsException("int array size out of bounds: " + size);
    }
    int[] arr = new int[size];
    for (int k = 0; k < arr.length; k++) {
      arr[k] = buf.readIntLE();
    }
    return arr;
  }

  public static void writeIntArray(ByteBuf buf, int[] nums) {
    if (nums == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(nums.length + 1);
    for (int k = 0; k < nums.length; k++) {
      buf.writeIntLE(nums[k]);
    }
  }
}
