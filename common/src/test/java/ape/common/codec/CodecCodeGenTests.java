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

import org.junit.Assert;
import org.junit.Test;

public class CodecCodeGenTests {
  @Test
  public void flow() {
    String java = CodecCodeGen.assembleCodec("ape.common.codec", "GeneratedCodecMe", TestClassA.class, TestClassB.class);
    System.err.println(java);
  }

  @Test
  public void dupe() {
    try {
      CodecCodeGen.assembleCodec("ape.common.codec", "GeneratedCodecMe", TestClassA.class, TestClassA.class);
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("Duplicate type:123", re.getMessage());
    }
  }

  @Test
  public void problem_no_flow() {
    try {
      CodecCodeGen.assembleCodec("C", "C", NoFlow.class);
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("NoFlow has no @Flow", re.getMessage());
    }
  }

  @Test
  public void problem_no_type() {
    try {
      CodecCodeGen.assembleCodec("C", "C", NoType.class);
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("class ape.common.codec.CodecCodeGenTests$NoType has no @TypeId", re.getMessage());
    }
  }

  @Test
  public void problem_no_order() {
    try {
      CodecCodeGen.assembleCodec("C", "C", NoOrder.class);
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("NoOrder has field 'w' which has no order", re.getMessage());
    }
  }

  @Test
  public void problem_dupe_order() {
    try {
      CodecCodeGen.assembleCodec("C", "C", DupeOrder.class);
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("DupeOrder has two or more fields with order '1'", re.getMessage());
    }
  }

  @Test
  public void problem_bad_type() {
    try {
      CodecCodeGen.write(BadType.class.getFields()[0], "");
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("badType has a type we don't know about.. yet", re.getMessage());
    }
    try {
      CodecCodeGen.readerOf(BadType.class.getFields()[0]);
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("badType has a type we don't know about.. yet", re.getMessage());
    }
  }

  @TypeId(123)
  @PriorTypeId(42)
  @Flow("X")
  @MakeReadRegister
  public static class TestClassA {

    @FieldOrder(1)
    public int x;

    @FieldOrder(2)
    @FieldNew
    public String str;

    @FieldOrder(3)
    @FieldOld
    public double z;

    @FieldOrder(4)
    public double w;

    @FieldOrder(5)
    public short sssshort;

    @FieldOrder(6)
    public boolean bbb;

    @FieldOrder(7)
    public String[] strarr;

    @FieldOrder(8)
    public int[] intarr;
  }

  @TypeId(4242)
  @Flow("X|Y")
  public static class TestClassB {

    @FieldOrder(1)
    public int x;

    @FieldOrder(2)
    public TestClassA embed;

    @FieldOrder(3)
    public long lng;

    @FieldOrder(4)
    public TestClassA[] arr;
  }

  public static class NoFlow {
  }

  @Flow("X")
  public static class NoType {
  }

  @TypeId(42)
  @Flow("X")
  public static class NoOrder {
    public double w;
  }

  @TypeId(42)
  @Flow("X")
  public static class DupeOrder {
    @FieldOrder(1)
    public double w;

    @FieldOrder(1)
    public double z;
  }

  @TypeId(42)
  @Flow("X")
  public static class BadType {
    @FieldOrder(1)
    public CodecCodeGenTests badType;
  }
}
