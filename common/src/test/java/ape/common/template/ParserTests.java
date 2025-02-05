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
package ape.common.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.template.tree.T;
import org.junit.Assert;
import org.junit.Test;

public class ParserTests {

  @Test
  public void constant() {
    Assert.assertEquals("Hello World", eval(Parser.parse("Hello World")));
  }

  public String eval(T template, String... args) {
    Settings settings = new Settings(true);
    ObjectNode node = new ObjectMapper().createObjectNode();
    for (int k = 0; k + 1 < args.length; k += 2) {
      String value = args[k + 1];
      if ("true".equals(value)) {
        node.put(args[k], true);
      } else if ("false".equals(value)) {
        node.put(args[k], false);
      } else {
        node.put(args[k], value);
      }
    }
    StringBuilder sb = new StringBuilder();
    template.render(settings, node, sb);
    return sb.toString();
  }

  @Test
  public void memory() {
    Assert.assertEquals(171, Parser.parse("[[msg]]").memory());
    Assert.assertEquals(178, Parser.parse("hello").memory());
    Assert.assertEquals(397, Parser.parse("hello[[a]]world").memory());
    Assert.assertEquals(575, Parser.parse("hello[[#a]]noice[[/a]]world").memory());
    Assert.assertEquals(575, Parser.parse("hello[[^a]]noice[[/a]]world").memory());
  }

  @Test
  public void single_var() {
    Assert.assertEquals("Hi", eval(Parser.parse("[[msg]]"), "msg", "Hi"));
  }

  @Test
  public void compound_constant_var_constant() {
    Assert.assertEquals("Oh Hi There", eval(Parser.parse("Oh [[msg]] There"), "msg", "Hi"));
  }

  @Test
  public void simple_if_true() {
    Assert.assertEquals("You! Oh Hi There", eval(Parser.parse("You! Oh [[#guard]][[msg]][[/guard]] There"), "msg", "Hi", "guard", "true"));
  }

  @Test
  public void simple_if_false() {
    Assert.assertEquals("You! Oh  There", eval(Parser.parse("You! Oh [[#guard]][[msg]][[/guard]] There"), "msg", "Hi", "guard", "false"));
  }

  @Test
  public void simple_ifnot_true() {
    Assert.assertEquals("You! Oh  There", eval(Parser.parse("You! Oh [[^guard]][[msg]][[/guard]] There"), "msg", "Hi", "guard", "true"));
  }

  @Test
  public void simple_ifnot_false() {
    Assert.assertEquals("You! Oh Hi There", eval(Parser.parse("You! Oh [[^guard]][[msg]][[/guard]] There"), "msg", "Hi", "guard", "false"));
  }

  @Test
  public void nested_if() {
    Assert.assertEquals("{{ACD}}", eval(Parser.parse("{{[[#g0]]A[[#g1]]B[[/g1]][[^g1]]C[[^g1]]D[[/g1]][[/g1]][[/g0]]}}"), "msg", "Hi", "g0", "true", "g1", "false"));
  }

  @Test
  public void html_escaped() {
    Assert.assertEquals("&lt;b&gt;Bold&lt;/b&gt;", eval(Parser.parse("[[html]]"), "html", "<b>Bold</b>"));
  }

  @Test
  public void html_unescaped() {
    Assert.assertEquals("<b>Bold</b>", eval(Parser.parse("[[html|unescape]]"), "html", "<b>Bold</b>"));
  }
}
