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

public class IntegerVariableEncodingTests {

  // ========== Int: basic round-trip ==========

  @Test
  public void intZero() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, 0);
    Assert.assertEquals(0, IntegerVariableEncoding.readIntVar(buf));
  }

  @Test
  public void intOne() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, 1);
    Assert.assertEquals(1, IntegerVariableEncoding.readIntVar(buf));
  }

  @Test
  public void intNegativeOne() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, -1);
    Assert.assertEquals(-1, IntegerVariableEncoding.readIntVar(buf));
  }

  @Test
  public void intMaxValue() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, Integer.MAX_VALUE);
    Assert.assertEquals(Integer.MAX_VALUE, IntegerVariableEncoding.readIntVar(buf));
  }

  @Test
  public void intMinValue() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, Integer.MIN_VALUE);
    Assert.assertEquals(Integer.MIN_VALUE, IntegerVariableEncoding.readIntVar(buf));
  }

  // ========== Int: exhaustive single-byte range (-64 to 63) ==========

  @Test
  public void intAllSingleByteValues() {
    ByteBuf buf = Unpooled.buffer();
    for (int i = -64; i <= 63; i++) {
      IntegerVariableEncoding.writeIntVar(buf, i);
    }
    for (int i = -64; i <= 63; i++) {
      Assert.assertEquals(i, IntegerVariableEncoding.readIntVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  @Test
  public void intSingleByteSize() {
    for (int i = -64; i <= 63; i++) {
      ByteBuf buf = Unpooled.buffer();
      IntegerVariableEncoding.writeIntVar(buf, i);
      Assert.assertEquals("value " + i + " should be 1 byte", 1, buf.readableBytes());
      buf.release();
    }
  }

  // ========== Int: encoding size boundaries ==========
  // 1 byte: zigzag 0-127      → signed -64 to 63
  // 2 bytes: zigzag 128-16383  → signed -8192 to 8191
  // 3 bytes: zigzag 16384-2097151 → signed -1048576 to 1048575
  // 4 bytes: zigzag 2097152-268435455 → signed -134217728 to 134217727
  // 5 bytes: zigzag 268435456+  → rest

  @Test
  public void intBoundary1to2bytes() {
    // last 1-byte: 63 and -64
    assertIntBytes(1, 63);
    assertIntBytes(1, -64);
    // first 2-byte: 64 and -65
    assertIntBytes(2, 64);
    assertIntBytes(2, -65);
  }

  @Test
  public void intBoundary2to3bytes() {
    assertIntBytes(2, 8191);
    assertIntBytes(2, -8192);
    assertIntBytes(3, 8192);
    assertIntBytes(3, -8193);
  }

  @Test
  public void intBoundary3to4bytes() {
    assertIntBytes(3, 1048575);
    assertIntBytes(3, -1048576);
    assertIntBytes(4, 1048576);
    assertIntBytes(4, -1048577);
  }

  @Test
  public void intBoundary4to5bytes() {
    assertIntBytes(4, 134217727);
    assertIntBytes(4, -134217728);
    assertIntBytes(5, 134217728);
    assertIntBytes(5, -134217729);
  }

  @Test
  public void intExtremesAre5bytes() {
    assertIntBytes(5, Integer.MAX_VALUE);
    assertIntBytes(5, Integer.MIN_VALUE);
  }

  // ========== Int: powers of 2 ==========

  @Test
  public void intPowersOfTwo() {
    ByteBuf buf = Unpooled.buffer();
    for (int shift = 0; shift < 31; shift++) {
      int val = 1 << shift;
      IntegerVariableEncoding.writeIntVar(buf, val);
      IntegerVariableEncoding.writeIntVar(buf, -val);
    }
    for (int shift = 0; shift < 31; shift++) {
      int val = 1 << shift;
      Assert.assertEquals(val, IntegerVariableEncoding.readIntVar(buf));
      Assert.assertEquals(-val, IntegerVariableEncoding.readIntVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  // ========== Int: sequential interleaved ==========

  @Test
  public void intSequentialMany() {
    ByteBuf buf = Unpooled.buffer();
    int[] values = {0, 1, -1, 2, -2, 100, -100, 10000, -10000, 1000000, -1000000,
        Integer.MAX_VALUE, Integer.MIN_VALUE, 42, -42, 127, -128, 255, -256, 65535, -65536};
    for (int v : values) {
      IntegerVariableEncoding.writeIntVar(buf, v);
    }
    for (int v : values) {
      Assert.assertEquals(v, IntegerVariableEncoding.readIntVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  // ========== Int: specific byte patterns ==========

  @Test
  public void intZeroEncodesAsOneByte() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, 0);
    Assert.assertEquals(1, buf.readableBytes());
    Assert.assertEquals(0x00, buf.readByte());
  }

  @Test
  public void intNegOneEncodesAsOneByte() {
    // zigzag(-1) = 1, varint(1) = 0x01
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, -1);
    Assert.assertEquals(1, buf.readableBytes());
    Assert.assertEquals(0x01, buf.readByte());
  }

  @Test
  public void intOneEncodesAsOneByte() {
    // zigzag(1) = 2, varint(2) = 0x02
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, 1);
    Assert.assertEquals(1, buf.readableBytes());
    Assert.assertEquals(0x02, buf.readByte());
  }

  @Test
  public void int64EncodesAsTwoBytes() {
    // zigzag(64) = 128, varint(128) = 0x80 0x01
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, 64);
    Assert.assertEquals(2, buf.readableBytes());
    Assert.assertEquals((byte) 0x80, buf.readByte());
    Assert.assertEquals((byte) 0x01, buf.readByte());
  }

  // ========== Int: corrupt data ==========

  @Test(expected = IndexOutOfBoundsException.class)
  public void intOverflowTooManyContinuationBytes() {
    ByteBuf buf = Unpooled.buffer();
    // write 5 continuation bytes (all with high bit set) + no terminator
    for (int i = 0; i < 5; i++) {
      buf.writeByte(0x80);
    }
    IntegerVariableEncoding.readIntVar(buf);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void intOverflowSixContinuationBytes() {
    ByteBuf buf = Unpooled.buffer();
    for (int i = 0; i < 6; i++) {
      buf.writeByte(0x80);
    }
    IntegerVariableEncoding.readIntVar(buf);
  }

  // ========== Long: basic round-trip ==========

  @Test
  public void longZero() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, 0L);
    Assert.assertEquals(0L, IntegerVariableEncoding.readLongVar(buf));
  }

  @Test
  public void longOne() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, 1L);
    Assert.assertEquals(1L, IntegerVariableEncoding.readLongVar(buf));
  }

  @Test
  public void longNegativeOne() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, -1L);
    Assert.assertEquals(-1L, IntegerVariableEncoding.readLongVar(buf));
  }

  @Test
  public void longMaxValue() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, Long.MAX_VALUE);
    Assert.assertEquals(Long.MAX_VALUE, IntegerVariableEncoding.readLongVar(buf));
  }

  @Test
  public void longMinValue() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, Long.MIN_VALUE);
    Assert.assertEquals(Long.MIN_VALUE, IntegerVariableEncoding.readLongVar(buf));
  }

  // ========== Long: exhaustive single-byte range ==========

  @Test
  public void longAllSingleByteValues() {
    ByteBuf buf = Unpooled.buffer();
    for (long i = -64; i <= 63; i++) {
      IntegerVariableEncoding.writeLongVar(buf, i);
    }
    for (long i = -64; i <= 63; i++) {
      Assert.assertEquals(i, IntegerVariableEncoding.readLongVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  @Test
  public void longSingleByteSize() {
    for (long i = -64; i <= 63; i++) {
      ByteBuf buf = Unpooled.buffer();
      IntegerVariableEncoding.writeLongVar(buf, i);
      Assert.assertEquals("value " + i + " should be 1 byte", 1, buf.readableBytes());
      buf.release();
    }
  }

  // ========== Long: encoding size boundaries ==========
  // 1 byte: signed -64 to 63
  // 2 bytes: signed -8192 to 8191
  // 3 bytes: signed -1048576 to 1048575
  // 4 bytes: signed -134217728 to 134217727
  // 5 bytes: signed -17179869184 to 17179869183
  // 6 bytes: signed -2199023255552 to 2199023255551
  // 7 bytes: signed -281474976710656 to 281474976710655
  // 8 bytes: signed -36028797018963968 to 36028797018963967
  // 9 bytes: signed -4611686018427387904 to 4611686018427387903
  // 10 bytes: rest

  @Test
  public void longBoundary1to2bytes() {
    assertLongBytes(1, 63L);
    assertLongBytes(1, -64L);
    assertLongBytes(2, 64L);
    assertLongBytes(2, -65L);
  }

  @Test
  public void longBoundary2to3bytes() {
    assertLongBytes(2, 8191L);
    assertLongBytes(2, -8192L);
    assertLongBytes(3, 8192L);
    assertLongBytes(3, -8193L);
  }

  @Test
  public void longBoundary3to4bytes() {
    assertLongBytes(3, 1048575L);
    assertLongBytes(3, -1048576L);
    assertLongBytes(4, 1048576L);
    assertLongBytes(4, -1048577L);
  }

  @Test
  public void longBoundary4to5bytes() {
    assertLongBytes(4, 134217727L);
    assertLongBytes(4, -134217728L);
    assertLongBytes(5, 134217728L);
    assertLongBytes(5, -134217729L);
  }

  @Test
  public void longBoundary5to6bytes() {
    assertLongBytes(5, 17179869183L);
    assertLongBytes(5, -17179869184L);
    assertLongBytes(6, 17179869184L);
    assertLongBytes(6, -17179869185L);
  }

  @Test
  public void longBoundary6to7bytes() {
    assertLongBytes(6, 2199023255551L);
    assertLongBytes(6, -2199023255552L);
    assertLongBytes(7, 2199023255552L);
    assertLongBytes(7, -2199023255553L);
  }

  @Test
  public void longBoundary7to8bytes() {
    assertLongBytes(7, 281474976710655L);
    assertLongBytes(7, -281474976710656L);
    assertLongBytes(8, 281474976710656L);
    assertLongBytes(8, -281474976710657L);
  }

  @Test
  public void longBoundary8to9bytes() {
    assertLongBytes(8, 36028797018963967L);
    assertLongBytes(8, -36028797018963968L);
    assertLongBytes(9, 36028797018963968L);
    assertLongBytes(9, -36028797018963969L);
  }

  @Test
  public void longBoundary9to10bytes() {
    assertLongBytes(9, 4611686018427387903L);
    assertLongBytes(9, -4611686018427387904L);
    assertLongBytes(10, 4611686018427387904L);
    assertLongBytes(10, -4611686018427387905L);
  }

  @Test
  public void longExtremesAre10bytes() {
    assertLongBytes(10, Long.MAX_VALUE);
    assertLongBytes(10, Long.MIN_VALUE);
  }

  // ========== Long: powers of 2 ==========

  @Test
  public void longPowersOfTwo() {
    ByteBuf buf = Unpooled.buffer();
    for (int shift = 0; shift < 63; shift++) {
      long val = 1L << shift;
      IntegerVariableEncoding.writeLongVar(buf, val);
      IntegerVariableEncoding.writeLongVar(buf, -val);
    }
    for (int shift = 0; shift < 63; shift++) {
      long val = 1L << shift;
      Assert.assertEquals(val, IntegerVariableEncoding.readLongVar(buf));
      Assert.assertEquals(-val, IntegerVariableEncoding.readLongVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  // ========== Long: sequential interleaved ==========

  @Test
  public void longSequentialMany() {
    ByteBuf buf = Unpooled.buffer();
    long[] values = {0, 1, -1, 2, -2, 100, -100, 10000, -10000, 1000000, -1000000,
        1000000000L, -1000000000L, 1000000000000L, -1000000000000L,
        Long.MAX_VALUE, Long.MIN_VALUE, 42, -42};
    for (long v : values) {
      IntegerVariableEncoding.writeLongVar(buf, v);
    }
    for (long v : values) {
      Assert.assertEquals(v, IntegerVariableEncoding.readLongVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  // ========== Long: specific byte patterns ==========

  @Test
  public void longZeroEncodesAsOneByte() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, 0L);
    Assert.assertEquals(1, buf.readableBytes());
    Assert.assertEquals(0x00, buf.readByte());
  }

  @Test
  public void longNegOneEncodesAsOneByte() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, -1L);
    Assert.assertEquals(1, buf.readableBytes());
    Assert.assertEquals(0x01, buf.readByte());
  }

  // ========== Long: corrupt data ==========

  @Test(expected = IndexOutOfBoundsException.class)
  public void longOverflowTooManyContinuationBytes() {
    ByteBuf buf = Unpooled.buffer();
    for (int i = 0; i < 10; i++) {
      buf.writeByte(0x80);
    }
    IntegerVariableEncoding.readLongVar(buf);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void longOverflowElevenContinuationBytes() {
    ByteBuf buf = Unpooled.buffer();
    for (int i = 0; i < 11; i++) {
      buf.writeByte(0x80);
    }
    IntegerVariableEncoding.readLongVar(buf);
  }

  // ========== Cross-type: int values round-trip through long ==========

  @Test
  public void intValuesRoundTripThroughLong() {
    ByteBuf buf = Unpooled.buffer();
    int[] values = {0, 1, -1, 127, -128, 255, Integer.MAX_VALUE, Integer.MIN_VALUE};
    for (int v : values) {
      IntegerVariableEncoding.writeLongVar(buf, v);
    }
    for (int v : values) {
      Assert.assertEquals((long) v, IntegerVariableEncoding.readLongVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  // ========== Mixed: int and long interleaved in same buffer ==========

  @Test
  public void mixedIntAndLongInSameBuffer() {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, 42);
    IntegerVariableEncoding.writeLongVar(buf, 9999999999L);
    IntegerVariableEncoding.writeIntVar(buf, -1);
    IntegerVariableEncoding.writeLongVar(buf, Long.MIN_VALUE);
    Assert.assertEquals(42, IntegerVariableEncoding.readIntVar(buf));
    Assert.assertEquals(9999999999L, IntegerVariableEncoding.readLongVar(buf));
    Assert.assertEquals(-1, IntegerVariableEncoding.readIntVar(buf));
    Assert.assertEquals(Long.MIN_VALUE, IntegerVariableEncoding.readLongVar(buf));
    Assert.assertEquals(0, buf.readableBytes());
  }

  // ========== Size advantage: small values use fewer bytes than fixed-width ==========

  @Test
  public void smallIntsSmallerThanFixedWidth() {
    // Fixed: always 4 bytes. Variable: 1 byte for small values.
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, 0);
    IntegerVariableEncoding.writeIntVar(buf, 1);
    IntegerVariableEncoding.writeIntVar(buf, -1);
    IntegerVariableEncoding.writeIntVar(buf, 63);
    IntegerVariableEncoding.writeIntVar(buf, -64);
    // 5 values * 1 byte = 5 bytes (vs 5 * 4 = 20 bytes fixed)
    Assert.assertEquals(5, buf.readableBytes());
  }

  @Test
  public void smallLongsSmallerThanFixedWidth() {
    // Fixed: always 8 bytes. Variable: 1 byte for small values.
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, 0L);
    IntegerVariableEncoding.writeLongVar(buf, 1L);
    IntegerVariableEncoding.writeLongVar(buf, -1L);
    IntegerVariableEncoding.writeLongVar(buf, 63L);
    IntegerVariableEncoding.writeLongVar(buf, -64L);
    // 5 values * 1 byte = 5 bytes (vs 5 * 8 = 40 bytes fixed)
    Assert.assertEquals(5, buf.readableBytes());
  }

  // ========== Symmetry: zigzag maps positive and negative symmetrically ==========

  @Test
  public void intSymmetricEncoding() {
    // positive n and -(n+1) should produce same size encoding
    for (int n = 0; n < 10000; n++) {
      ByteBuf bufPos = Unpooled.buffer();
      ByteBuf bufNeg = Unpooled.buffer();
      IntegerVariableEncoding.writeIntVar(bufPos, n);
      IntegerVariableEncoding.writeIntVar(bufNeg, -(n + 1));
      Assert.assertEquals("size mismatch at n=" + n, bufPos.readableBytes(), bufNeg.readableBytes());
      bufPos.release();
      bufNeg.release();
    }
  }

  @Test
  public void longSymmetricEncoding() {
    for (long n = 0; n < 10000; n++) {
      ByteBuf bufPos = Unpooled.buffer();
      ByteBuf bufNeg = Unpooled.buffer();
      IntegerVariableEncoding.writeLongVar(bufPos, n);
      IntegerVariableEncoding.writeLongVar(bufNeg, -(n + 1));
      Assert.assertEquals("size mismatch at n=" + n, bufPos.readableBytes(), bufNeg.readableBytes());
      bufPos.release();
      bufNeg.release();
    }
  }

  // ========== Bulk: sweep a wide range of int values ==========

  @Test
  public void intSweepRange() {
    ByteBuf buf = Unpooled.buffer();
    // write -100000 to 100000
    for (int i = -100000; i <= 100000; i++) {
      IntegerVariableEncoding.writeIntVar(buf, i);
    }
    for (int i = -100000; i <= 100000; i++) {
      Assert.assertEquals(i, IntegerVariableEncoding.readIntVar(buf));
    }
    Assert.assertEquals(0, buf.readableBytes());
  }

  // ========== helpers ==========

  private static void assertIntBytes(int expectedBytes, int value) {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeIntVar(buf, value);
    Assert.assertEquals("int " + value + " should encode to " + expectedBytes + " bytes",
        expectedBytes, buf.readableBytes());
    Assert.assertEquals(value, IntegerVariableEncoding.readIntVar(buf));
    buf.release();
  }

  private static void assertLongBytes(int expectedBytes, long value) {
    ByteBuf buf = Unpooled.buffer();
    IntegerVariableEncoding.writeLongVar(buf, value);
    Assert.assertEquals("long " + value + " should encode to " + expectedBytes + " bytes",
        expectedBytes, buf.readableBytes());
    Assert.assertEquals(value, IntegerVariableEncoding.readLongVar(buf));
    buf.release();
  }
}
