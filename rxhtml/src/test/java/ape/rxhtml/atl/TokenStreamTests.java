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
package ape.rxhtml.atl;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class TokenStreamTests {

  @Test
  public void simple_text() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("xyz").iterator();
    assertNextIsText(it, "xyz");
    assertNoNext(it);
  }

  private void assertNextIsText(Iterator<TokenStream.Token> it, String value) throws Exception {
    Assert.assertTrue(it.hasNext());
    TokenStream.Token next = it.next();
    Assert.assertEquals(next.type, TokenStream.Type.Text);
    Assert.assertEquals(next.base, value);
  }

  public void assertNoNext(Iterator<TokenStream.Token> it) {
    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void simple_variable() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("{x}").iterator();
    assertNextIsVariable(it, "x", TokenStream.Modifier.None);
    assertNoNext(it);
  }

  private void assertNextIsVariable(Iterator<TokenStream.Token> it, String value, TokenStream.Modifier mod, String... transforms) throws Exception {
    Assert.assertTrue(it.hasNext());
    TokenStream.Token next = it.next();
    Assert.assertEquals(TokenStream.Type.Variable, next.type);
    Assert.assertEquals(mod, next.mod);
    Assert.assertEquals(value, next.base);
    Assert.assertEquals(transforms.length, next.transforms.length);
    for (int k = 0; k < transforms.length; k++) {
      Assert.assertEquals(transforms[k], next.transforms[k]);
    }
  }

  @Test
  public void variable_mods() throws Exception {
    {
      assertNextIsVariable(TokenStream.tokenize("{x}").iterator(), "x", TokenStream.Modifier.None);
      assertNextIsVariable(TokenStream.tokenize("{  x  }").iterator(), "x", TokenStream.Modifier.None);
      assertNextIsVariable(TokenStream.tokenize("{! x }").iterator(), "x", TokenStream.Modifier.Not);
      assertNextIsVariable(TokenStream.tokenize("{!x}").iterator(), "x", TokenStream.Modifier.Not);
      assertNextIsVariable(TokenStream.tokenize("{  # x  }").iterator(), "# x", TokenStream.Modifier.None);
      assertNextIsVariable(TokenStream.tokenize("{# x}").iterator(), "# x", TokenStream.Modifier.None);
      assertNextIsVariable(TokenStream.tokenize("{  / x  }").iterator(), "/ x", TokenStream.Modifier.None);
      assertNextIsVariable(TokenStream.tokenize("{/ x}").iterator(), "/ x", TokenStream.Modifier.None);
    }
  }

  @Test
  public void simple_variable_w_1_transform() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("{x|y}").iterator();
    assertNextIsVariable(it, "x", TokenStream.Modifier.None, "y");
    assertNoNext(it);
  }

  @Test
  public void simple_variable_w_2_transform() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("{x|y|z}").iterator();
    assertNextIsVariable(it, "x", TokenStream.Modifier.None, "y", "z");
    assertNoNext(it);
  }

  @Test
  public void simple_condition() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("[x]").iterator();
    assertNextIsCondition(it, "x", TokenStream.Modifier.None);
    assertNoNext(it);
  }

  private void assertNextIsCondition(Iterator<TokenStream.Token> it, String value, TokenStream.Modifier mod, String... transforms) throws Exception {
    Assert.assertTrue(it.hasNext());
    TokenStream.Token next = it.next();
    Assert.assertEquals(next.type, TokenStream.Type.Condition);
    Assert.assertEquals(next.mod, mod);
    Assert.assertEquals(next.base, value);
    Assert.assertEquals(next.transforms.length, transforms.length);
    for (int k = 0; k < transforms.length; k++) {
      Assert.assertEquals(next.transforms[k], transforms[k]);
    }
  }

  @Test
  public void condition_mods() throws Exception {
    {
      assertNextIsCondition(TokenStream.tokenize("[x]").iterator(), "x", TokenStream.Modifier.None);
      assertNextIsCondition(TokenStream.tokenize("[!x]").iterator(), "x", TokenStream.Modifier.Not);
      assertNextIsCondition(TokenStream.tokenize("[#x]").iterator(), "x", TokenStream.Modifier.Else);
      assertNextIsCondition(TokenStream.tokenize("[ x ]").iterator(), "x", TokenStream.Modifier.None);
      assertNextIsCondition(TokenStream.tokenize("[# x ]").iterator(), "x", TokenStream.Modifier.Else);
      assertNextIsCondition(TokenStream.tokenize("[! x ]").iterator(), "x", TokenStream.Modifier.Not);
      assertNextIsCondition(TokenStream.tokenize("[/x]").iterator(), "x", TokenStream.Modifier.End);
      assertNextIsCondition(TokenStream.tokenize("[/ x ]").iterator(), "x", TokenStream.Modifier.End);
    }
  }

  @Test
  public void simple_condition_w_1_transform() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("[x|y]").iterator();
    assertNextIsCondition(it, "x", TokenStream.Modifier.None, "y");
    assertNoNext(it);
  }

  @Test
  public void simple_condition_w_2_transform() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("[x|y|z]").iterator();
    assertNextIsCondition(it, "x", TokenStream.Modifier.None, "y", "z");
    assertNoNext(it);
  }

  @Test
  public void compound_1() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("hi {name}, how are you? [good]a[#good]b[/good]c").iterator();
    assertNextIsText(it, "hi ");
    assertNextIsVariable(it, "name", TokenStream.Modifier.None);
    assertNextIsText(it, ", how are you? ");
    assertNextIsCondition(it, "good", TokenStream.Modifier.None);
    assertNextIsText(it, "a");
    assertNextIsCondition(it, "good", TokenStream.Modifier.Else);
    assertNextIsText(it, "b");
    assertNextIsCondition(it, "good", TokenStream.Modifier.End);
    assertNextIsText(it, "c");
    assertNoNext(it);
  }

  @Test
  public void escaping1() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("`[`]`{`}").iterator();
    assertNextIsText(it, "[]{}");
    assertNoNext(it);
  }

  @Test
  public void escaping2() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("a`{`}b").iterator();
    assertNextIsText(it, "a{}b");
    assertNoNext(it);
  }

  @Test
  public void escaping3() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("a`[`]b").iterator();
    assertNextIsText(it, "a[]b");
    assertNoNext(it);
  }

  @Test
  public void escaping4() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("[a`{]").iterator();
    assertNextIsCondition(it, "a`{", TokenStream.Modifier.None);
    assertNoNext(it);
  }

  @Test
  public void escaping5() throws Exception {
    Iterator<TokenStream.Token> it = TokenStream.tokenize("{a`{}").iterator();
    assertNextIsVariable(it, "a`{", TokenStream.Modifier.None);
    assertNoNext(it);
  }
}
