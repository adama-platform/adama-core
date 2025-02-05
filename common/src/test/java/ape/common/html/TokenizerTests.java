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
package ape.common.html;

import org.junit.Assert;
import org.junit.Test;

public class TokenizerTests {
  @Test
  public void code_escaping() {
    Tokenizer t = Tokenizer.of("<code escape>Hi<b>there</b></code>");
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("<code escape>", tok.text);
      Assert.assertEquals("0;0;0;13", tok.coords());
      Assert.assertEquals(Type.ElementOpen, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hi&lt;b&gt;there&lt;/b&gt;", tok.text);
      Assert.assertEquals("0;13;0;27", tok.coords());
      Assert.assertEquals(Type.EmbeddedText, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("</code>", tok.text);
      Assert.assertEquals("0;27;0;34", tok.coords());
      Assert.assertEquals(Type.ElementClose, tok.type);
    }
  }

  @Test
  public void script_empty() {
    Tokenizer t = Tokenizer.of("Hello<script />Hi there");
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hello", tok.text);
      Assert.assertEquals("0;0;0;5", tok.coords());
      Assert.assertEquals(Type.Text, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("<script />", tok.text);
      Assert.assertEquals("0;5;0;15", tok.coords());
      Assert.assertEquals(Type.ElementOpen, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hi there", tok.text);
      Assert.assertEquals("0;15;0;23", tok.coords());
      Assert.assertEquals(Type.Text, tok.type);
    }
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void script_happy() {
    Tokenizer t = Tokenizer.of("Hello<script x=\"happy\">Hi 1<sc2 there goes \n> there</script>Good buhbye!");
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hello", tok.text);
      Assert.assertEquals("0;0;0;5", tok.coords());
      Assert.assertEquals(Type.Text, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("<script x=\"happy\">", tok.text);
      Assert.assertEquals("0;5;0;23", tok.coords());
      Assert.assertEquals(Type.ElementOpen, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hi 1<sc2 there goes \n> there", tok.text);
      Assert.assertEquals("0;23;1;7", tok.coords());
      Assert.assertEquals(Type.EmbeddedText, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("</script>", tok.text);
      Assert.assertEquals("1;7;1;16", tok.coords());
      Assert.assertEquals(Type.ElementClose, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Good buhbye!", tok.text);
      Assert.assertEquals("1;16;1;28", tok.coords());
      Assert.assertEquals(Type.Text, tok.type);
    }
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void script_open() {
    Tokenizer t = Tokenizer.of("Hello<script>Hi there");
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hello", tok.text);
      Assert.assertEquals("0;0;0;5", tok.coords());
      Assert.assertEquals(Type.Text, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("<script>", tok.text);
      Assert.assertEquals("0;5;0;13", tok.coords());
      Assert.assertEquals(Type.ElementOpen, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hi there", tok.text);
      Assert.assertEquals("0;13;0;21", tok.coords());
      Assert.assertEquals(Type.EmbeddedText, tok.type);
    }
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void script_open_partial_cancel() {
    Tokenizer t = Tokenizer.of("Hello<script>Hi there <x");
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hello", tok.text);
      Assert.assertEquals("0;0;0;5", tok.coords());
      Assert.assertEquals(Type.Text, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("<script>", tok.text);
      Assert.assertEquals("0;5;0;13", tok.coords());
      Assert.assertEquals(Type.ElementOpen, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hi there <x", tok.text);
      Assert.assertEquals("0;13;0;24", tok.coords());
      Assert.assertEquals(Type.EmbeddedText, tok.type);
    }
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void script_open_partial_keep() {
    Tokenizer t = Tokenizer.of("Hello<script>Hi there </scr");
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hello", tok.text);
      Assert.assertEquals("0;0;0;5", tok.coords());
      Assert.assertEquals(Type.Text, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("<script>", tok.text);
      Assert.assertEquals("0;5;0;13", tok.coords());
      Assert.assertEquals(Type.ElementOpen, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("Hi there ", tok.text);
      Assert.assertEquals("0;13;0;22", tok.coords());
      Assert.assertEquals(Type.EmbeddedText, tok.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token tok = t.next();
      Assert.assertEquals("</scr", tok.text);
      Assert.assertEquals("0;22;0;27", tok.coords());
      Assert.assertEquals(Type.ElementClose, tok.type);
    }
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void just_text() {
    Tokenizer t = Tokenizer.of("Hello");
    Assert.assertTrue(t.hasNext());
    Token t0 = t.next();
    Assert.assertEquals("Hello", t0.text);
    Assert.assertEquals("0;0;0;5", t0.coords());
    Assert.assertEquals(Type.Text, t0.type);
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void just_element_open() {
    Tokenizer t = Tokenizer.of("<Hello>");
    Assert.assertTrue(t.hasNext());
    Token t0 = t.next();
    Assert.assertEquals("<Hello>", t0.text);
    Assert.assertEquals("0;0;0;7", t0.coords());
    Assert.assertEquals(Type.ElementOpen, t0.type);
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void just_element_close() {
    Tokenizer t = Tokenizer.of("</Hello>");
    Assert.assertTrue(t.hasNext());
    Token t0 = t.next();
    Assert.assertEquals("</Hello>", t0.text);
    Assert.assertEquals("0;0;0;8", t0.coords());
    Assert.assertEquals(Type.ElementClose, t0.type);
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void text_element_text_close() {
    Tokenizer t = Tokenizer.of("Hello <b>Mr</b>");
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("Hello ", t0.text);
      Assert.assertEquals("0;0;0;6", t0.coords());
      Assert.assertEquals(Type.Text, t0.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("<b>", t0.text);
      Assert.assertEquals("0;6;0;9", t0.coords());
      Assert.assertEquals(Type.ElementOpen, t0.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("Mr", t0.text);
      Assert.assertEquals("0;9;0;11", t0.coords());
      Assert.assertEquals(Type.Text, t0.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("</b>", t0.text);
      Assert.assertEquals("0;11;0;15", t0.coords());
      Assert.assertEquals(Type.ElementClose, t0.type);
    }
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void multi_line() {
    Tokenizer t = Tokenizer.of("Hello\n<b>\nWorld\n</b>");
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("Hello\n", t0.text);
      Assert.assertEquals("0;0;1;0", t0.coords());
      Assert.assertEquals(Type.Text, t0.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("<b>", t0.text);
      Assert.assertEquals("1;0;1;3", t0.coords());
      Assert.assertEquals(Type.ElementOpen, t0.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("\nWorld\n", t0.text);
      Assert.assertEquals("1;3;3;0", t0.coords());
      Assert.assertEquals(Type.Text, t0.type);
    }
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("</b>", t0.text);
      Assert.assertEquals("3;0;3;4", t0.coords());
      Assert.assertEquals(Type.ElementClose, t0.type);
    }
    Assert.assertFalse(t.hasNext());
  }

  @Test
  public void attr() {
    Tokenizer t = Tokenizer.of("<b attr=\"x<yz\">");
    {
      Assert.assertTrue(t.hasNext());
      Token t0 = t.next();
      Assert.assertEquals("<b attr=\"x<yz\">", t0.text);
      Assert.assertEquals("0;0;0;15", t0.coords());
      Assert.assertEquals(Type.ElementOpen, t0.type);
    }
    Assert.assertFalse(t.hasNext());
  }
}
