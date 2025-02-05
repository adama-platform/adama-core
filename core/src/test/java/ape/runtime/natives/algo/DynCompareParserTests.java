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
package ape.runtime.natives.algo;

import org.junit.Assert;
import org.junit.Test;

public class DynCompareParserTests {
  @Test
  public void empty() {
    CompareField[] result = DynCompareParser.parse("");
    Assert.assertEquals(0, result.length);
  }
  @Test
  public void zero_content() {
    CompareField[] result = DynCompareParser.parse(",,   ,,   ,,,");
    Assert.assertEquals(0, result.length);
  }

  @Test
  public void single() {
    CompareField[] result = DynCompareParser.parse("a");
    Assert.assertEquals(1, result.length);
    Assert.assertEquals("a", result[0].name);
    Assert.assertFalse(result[0].desc);
  }
  @Test
  public void single_ws() {
    CompareField[] result = DynCompareParser.parse("   a   ");
    Assert.assertEquals(1, result.length);
    Assert.assertEquals("a", result[0].name);
    Assert.assertFalse(result[0].desc);
  }
  @Test
  public void single_desc() {
    CompareField[] result = DynCompareParser.parse("-a");
    Assert.assertEquals(1, result.length);
    Assert.assertEquals("a", result[0].name);
    Assert.assertTrue(result[0].desc);
  }
  @Test
  public void single_desc_ws() {
    CompareField[] result = DynCompareParser.parse(" -  a   ");
    Assert.assertEquals(1, result.length);
    Assert.assertEquals("a", result[0].name);
    Assert.assertTrue(result[0].desc);
  }

  @Test
  public void trimix() {
    CompareField[] result = DynCompareParser.parse("+abc,-def,gh");
    Assert.assertEquals(3, result.length);
    Assert.assertEquals("abc", result[0].name);
    Assert.assertEquals("def", result[1].name);
    Assert.assertEquals("gh", result[2].name);
    Assert.assertFalse(result[0].desc);
    Assert.assertTrue(result[1].desc);
    Assert.assertFalse(result[2].desc);
  }
}
