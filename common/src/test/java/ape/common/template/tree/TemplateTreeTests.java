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
package ape.common.template.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.template.Settings;
import org.junit.Assert;
import org.junit.Test;

public class TemplateTreeTests {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  // --- TText tests ---

  @Test
  public void textRendersLiteral() {
    TText t = new TText("hello world");
    StringBuilder sb = new StringBuilder();
    t.render(new Settings(), MAPPER.createObjectNode(), sb);
    Assert.assertEquals("hello world", sb.toString());
  }

  @Test
  public void textMemory() {
    TText t = new TText("abc");
    Assert.assertEquals(64 + 3 * 2, t.memory());
  }

  @Test
  public void textEmpty() {
    TText t = new TText("");
    StringBuilder sb = new StringBuilder();
    t.render(new Settings(), MAPPER.createObjectNode(), sb);
    Assert.assertEquals("", sb.toString());
    Assert.assertEquals(64, t.memory());
  }

  // --- TVariable tests ---

  @Test
  public void variableRendersValue() {
    TVariable v = new TVariable("name", false);
    ObjectNode node = MAPPER.createObjectNode();
    node.put("name", "Alice");
    StringBuilder sb = new StringBuilder();
    v.render(new Settings(false), node, sb);
    Assert.assertEquals("Alice", sb.toString());
  }

  @Test
  public void variableMissingProducesNothing() {
    TVariable v = new TVariable("missing", false);
    ObjectNode node = MAPPER.createObjectNode();
    StringBuilder sb = new StringBuilder();
    v.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }

  @Test
  public void variableHtmlEscaping() {
    TVariable v = new TVariable("data", false);
    ObjectNode node = MAPPER.createObjectNode();
    node.put("data", "<script>alert('xss')</script>");
    StringBuilder sb = new StringBuilder();
    v.render(new Settings(true), node, sb);
    String result = sb.toString();
    Assert.assertFalse(result.contains("<script>"));
    Assert.assertTrue(result.contains("&lt;"));
  }

  @Test
  public void variableUnescaped() {
    TVariable v = new TVariable("data", true);
    ObjectNode node = MAPPER.createObjectNode();
    node.put("data", "<b>bold</b>");
    StringBuilder sb = new StringBuilder();
    v.render(new Settings(true), node, sb);
    Assert.assertEquals("<b>bold</b>", sb.toString());
  }

  @Test
  public void variableNonTextualIgnored() {
    TVariable v = new TVariable("num", false);
    ObjectNode node = MAPPER.createObjectNode();
    node.put("num", 42);
    StringBuilder sb = new StringBuilder();
    v.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }

  @Test
  public void variableMemory() {
    TVariable v = new TVariable("test", false);
    Assert.assertEquals(64 + 4, v.memory());
  }

  // --- TConcat tests ---

  @Test
  public void concatRendersChildren() {
    TConcat c = new TConcat();
    c.add(new TText("Hello, "));
    c.add(new TText("World!"));
    StringBuilder sb = new StringBuilder();
    c.render(new Settings(), MAPPER.createObjectNode(), sb);
    Assert.assertEquals("Hello, World!", sb.toString());
  }

  @Test
  public void concatEmpty() {
    TConcat c = new TConcat();
    StringBuilder sb = new StringBuilder();
    c.render(new Settings(), MAPPER.createObjectNode(), sb);
    Assert.assertEquals("", sb.toString());
    Assert.assertEquals(64, c.memory());
  }

  @Test
  public void concatMemoryAccumulates() {
    TConcat c = new TConcat();
    long base = c.memory();
    TText child = new TText("x");
    c.add(child);
    Assert.assertTrue(c.memory() > base);
  }

  // --- TIf tests ---

  @Test
  public void ifTrueRendersChild() {
    TIf tif = new TIf("show", new TText("visible"));
    ObjectNode node = MAPPER.createObjectNode();
    node.put("show", true);
    StringBuilder sb = new StringBuilder();
    tif.render(new Settings(), node, sb);
    Assert.assertEquals("visible", sb.toString());
  }

  @Test
  public void ifFalseRendersNothing() {
    TIf tif = new TIf("show", new TText("visible"));
    ObjectNode node = MAPPER.createObjectNode();
    node.put("show", false);
    StringBuilder sb = new StringBuilder();
    tif.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }

  @Test
  public void ifMissingRendersNothing() {
    TIf tif = new TIf("show", new TText("visible"));
    ObjectNode node = MAPPER.createObjectNode();
    StringBuilder sb = new StringBuilder();
    tif.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }

  @Test
  public void ifNonBooleanRendersNothing() {
    TIf tif = new TIf("show", new TText("visible"));
    ObjectNode node = MAPPER.createObjectNode();
    node.put("show", "yes");
    StringBuilder sb = new StringBuilder();
    tif.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }

  @Test
  public void ifMemory() {
    TText child = new TText("x");
    TIf tif = new TIf("flag", child);
    Assert.assertEquals(64 + 4 + child.memory(), tif.memory());
  }

  // --- TIfNot tests ---

  @Test
  public void ifNotFalseRendersChild() {
    TIfNot tifn = new TIfNot("hide", new TText("shown"));
    ObjectNode node = MAPPER.createObjectNode();
    node.put("hide", false);
    StringBuilder sb = new StringBuilder();
    tifn.render(new Settings(), node, sb);
    Assert.assertEquals("shown", sb.toString());
  }

  @Test
  public void ifNotTrueRendersNothing() {
    TIfNot tifn = new TIfNot("hide", new TText("shown"));
    ObjectNode node = MAPPER.createObjectNode();
    node.put("hide", true);
    StringBuilder sb = new StringBuilder();
    tifn.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }

  @Test
  public void ifNotMissingRendersNothing() {
    TIfNot tifn = new TIfNot("hide", new TText("shown"));
    ObjectNode node = MAPPER.createObjectNode();
    StringBuilder sb = new StringBuilder();
    tifn.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }

  @Test
  public void ifNotMemory() {
    TText child = new TText("y");
    TIfNot tifn = new TIfNot("flag", child);
    Assert.assertEquals(64 + 4 + child.memory(), tifn.memory());
  }

  // --- Nested tests ---

  @Test
  public void nestedConditionals() {
    TConcat root = new TConcat();
    root.add(new TText("start-"));
    root.add(new TIf("a", new TIfNot("b", new TText("inner"))));
    root.add(new TText("-end"));
    ObjectNode node = MAPPER.createObjectNode();
    node.put("a", true);
    node.put("b", false);
    StringBuilder sb = new StringBuilder();
    root.render(new Settings(), node, sb);
    Assert.assertEquals("start-inner-end", sb.toString());
  }

  @Test
  public void nestedConditionalBlockedByOuter() {
    TConcat root = new TConcat();
    root.add(new TIf("a", new TIfNot("b", new TText("inner"))));
    ObjectNode node = MAPPER.createObjectNode();
    node.put("a", false);
    node.put("b", false);
    StringBuilder sb = new StringBuilder();
    root.render(new Settings(), node, sb);
    Assert.assertEquals("", sb.toString());
  }
}
