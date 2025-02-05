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

public class AlphaHexTests {
  @Test
  public void coverage() {
    Assert.assertEquals("AA", AlphaHex.encode(new byte[]{0}));
    Assert.assertEquals("AB", AlphaHex.encode(new byte[]{1}));
    Assert.assertEquals("AC", AlphaHex.encode(new byte[]{2}));
    Assert.assertEquals("AD", AlphaHex.encode(new byte[]{3}));
    Assert.assertEquals("AE", AlphaHex.encode(new byte[]{4}));
    Assert.assertEquals("AF", AlphaHex.encode(new byte[]{5}));
    Assert.assertEquals("AG", AlphaHex.encode(new byte[]{6}));
    Assert.assertEquals("AH", AlphaHex.encode(new byte[]{7}));
    Assert.assertEquals("AI", AlphaHex.encode(new byte[]{8}));
    Assert.assertEquals("AJ", AlphaHex.encode(new byte[]{9}));
    Assert.assertEquals("AK", AlphaHex.encode(new byte[]{10}));
    Assert.assertEquals("AL", AlphaHex.encode(new byte[]{11}));
    Assert.assertEquals("AM", AlphaHex.encode(new byte[]{12}));
    Assert.assertEquals("AN", AlphaHex.encode(new byte[]{13}));
    Assert.assertEquals("AO", AlphaHex.encode(new byte[]{14}));
    Assert.assertEquals("AP", AlphaHex.encode(new byte[]{15}));
    Assert.assertEquals("BA", AlphaHex.encode(new byte[]{16}));
    Assert.assertEquals("BAAEHNBIAMDEHLDDAB", AlphaHex.encode(new byte[]{16, 4, 125, 24, 12, 52, 123, 51, 1}));
  }
}
