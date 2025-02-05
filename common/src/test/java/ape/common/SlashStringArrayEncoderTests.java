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
package ape.common;

import org.junit.Assert;
import org.junit.Test;

public class SlashStringArrayEncoderTests {
  @Test
  public void simple() {
    String packed = SlashStringArrayEncoder.encode("a", "b", "c");
    Assert.assertEquals("a/b/c", packed);
    String[] unpacked = SlashStringArrayEncoder.decode(packed);
    Assert.assertEquals("a", unpacked[0]);
    Assert.assertEquals("b", unpacked[1]);
    Assert.assertEquals("c", unpacked[2]);
  }

  @Test
  public void unicode() {
    String packed = SlashStringArrayEncoder.encode("abcdef", "猿も木から落ちる", "안 녕");
    Assert.assertEquals("abcdef/猿も木から落ちる/안 녕", packed);
    String[] unpacked = SlashStringArrayEncoder.decode(packed);
    Assert.assertEquals("abcdef", unpacked[0]);
    Assert.assertEquals("猿も木から落ちる", unpacked[1]);
    Assert.assertEquals("안 녕", unpacked[2]);
  }

  @Test
  public void escaping() {
    String packed = SlashStringArrayEncoder.encode("-/-/", "---", "///");
    Assert.assertEquals("---/---//------/-/-/-/", packed);
    String[] unpacked = SlashStringArrayEncoder.decode(packed);
    Assert.assertEquals("-/-/", unpacked[0]);
    Assert.assertEquals("---", unpacked[1]);
    Assert.assertEquals("///", unpacked[2]);
  }

  @Test
  public void empty() {
    String packed = SlashStringArrayEncoder.encode("", "", "");
    Assert.assertEquals("//", packed);
    String[] unpacked = SlashStringArrayEncoder.decode(packed);
    Assert.assertEquals("", unpacked[0]);
    Assert.assertEquals("", unpacked[1]);
    Assert.assertEquals("", unpacked[2]);
  }

  @Test
  public void solo() {
    String packed = SlashStringArrayEncoder.encode("");
    Assert.assertEquals("", packed);
    String[] unpacked = SlashStringArrayEncoder.decode(packed);
    Assert.assertEquals("", unpacked[0]);
  }
}
