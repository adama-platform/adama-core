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
package ape.translator.parser.token;

import org.junit.Assert;
import org.junit.Test;

public class TokenTests {
  @Test
  public void coverageEquals() {
    Token a1 = Token.WRAP("a");
    Token a2 = Token.WRAP("a");
    Token b = Token.WRAP("b");
    Assert.assertNotEquals(a1, "a");
    Assert.assertEquals(a1, a1);
    Assert.assertEquals(a1, a2);
    Assert.assertNotEquals(a1, b);
    Assert.assertNotEquals(a2, b);
  }

  @Test
  public void coverageCmp() {
    Token a = Token.WRAP("a");
    Token b = Token.WRAP("b");
    Assert.assertTrue(a.compareTo(b) < 0);
    Assert.assertTrue(b.compareTo(a) > 0);
    Assert.assertTrue(b.compareTo(b) == 0);
  }

  @Test
  public void coverageHashcode() {
    Token a1 = Token.WRAP("a");
    Token a2 = Token.WRAP("a");
    Assert.assertEquals(178794309, a1.hashCode());
    Assert.assertEquals(a1.hashCode(), a2.hashCode());
  }

  @Test
  public void isKeyword() {
    Token t = new Token("source", "key", MajorTokenType.Keyword, null, 0, 0, 0, 0, 0, 0);
    Assert.assertTrue(t.isKeyword());
    Assert.assertFalse(t.isNumberLiteral());
    Assert.assertFalse(t.isNumberLiteralDouble());
    Assert.assertFalse(t.isNumberLiteralInteger());
  }

  @Test
  public void isDouble() {
    Token t = new Token("source", "key", MajorTokenType.NumberLiteral, MinorTokenType.NumberIsDouble, 0, 0, 0, 0, 0, 0);
    Assert.assertFalse(t.isKeyword());
    Assert.assertTrue(t.isNumberLiteral());
    Assert.assertTrue(t.isNumberLiteralDouble());
    Assert.assertFalse(t.isNumberLiteralInteger());
  }

  @Test
  public void isInt() {
    Token t = new Token("source", "key", MajorTokenType.NumberLiteral, MinorTokenType.NumberIsInteger, 0, 0, 0, 0, 0, 0);
    Assert.assertFalse(t.isKeyword());
    Assert.assertTrue(t.isNumberLiteral());
    Assert.assertFalse(t.isNumberLiteralDouble());
    Assert.assertTrue(t.isNumberLiteralInteger());
  }
}
