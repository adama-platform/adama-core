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

/** Variable-length integer encoding using ZigZag + unsigned varint for compact wire representation. */
public class IntegerVariableEncoding {

  public static void writeIntVar(ByteBuf buf, int value) {
    int n = (value << 1) ^ (value >> 31);
    while ((n & ~0x7F) != 0) {
      buf.writeByte((n & 0x7F) | 0x80);
      n >>>= 7;
    }
    buf.writeByte(n);
  }

  public static int readIntVar(ByteBuf buf) {
    int result = 0;
    int shift = 0;
    for (int i = 0; i < 5; i++) {
      byte b = buf.readByte();
      result |= (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        return (result >>> 1) ^ -(result & 1);
      }
      shift += 7;
    }
    throw new IndexOutOfBoundsException("varint overflow: too many bytes for int");
  }

  public static void writeLongVar(ByteBuf buf, long value) {
    long n = (value << 1) ^ (value >> 63);
    while ((n & ~0x7FL) != 0) {
      buf.writeByte((int) (n & 0x7F) | 0x80);
      n >>>= 7;
    }
    buf.writeByte((int) n);
  }

  public static long readLongVar(ByteBuf buf) {
    long result = 0;
    int shift = 0;
    for (int i = 0; i < 10; i++) {
      byte b = buf.readByte();
      result |= (long) (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        return (result >>> 1) ^ -(result & 1);
      }
      shift += 7;
    }
    throw new IndexOutOfBoundsException("varint overflow: too many bytes for long");
  }
}
