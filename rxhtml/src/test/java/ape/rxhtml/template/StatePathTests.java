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
package ape.rxhtml.template;

import org.junit.Assert;
import org.junit.Test;

public class StatePathTests {
  @Test
  public void simple() {
    StatePath sp = StatePath.resolve("simple", "S");
    Assert.assertEquals("S", sp.command);
    Assert.assertEquals("simple", sp.name);
    Assert.assertTrue(sp.simple);
  }

  @Test
  public void view_simple() {
    StatePath sp = StatePath.resolve("view:name", "S");
    Assert.assertEquals("$.pV(S)", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void dive() {
    StatePath sp = StatePath.resolve("path/simple", "S");
    Assert.assertEquals("$.pI(S,'path')", sp.command);
    Assert.assertEquals("simple", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void multidive() {
    StatePath sp = StatePath.resolve("path1/path2/name", "S");
    Assert.assertEquals("$.pI($.pI(S,'path1'),'path2')", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void multidive_with_dots1() {
    StatePath sp = StatePath.resolve("path1.path2/name", "S");
    Assert.assertEquals("$.pI($.pI(S,'path1'),'path2')", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void multidive_with_dots2() {
    StatePath sp = StatePath.resolve("path1.path2.name", "S");
    Assert.assertEquals("$.pI($.pI(S,'path1'),'path2')", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void multidive_with_dots3() {
    StatePath sp = StatePath.resolve("path1/path2.name", "S");
    Assert.assertEquals("$.pI($.pI(S,'path1'),'path2')", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void root() {
    StatePath sp = StatePath.resolve("/name", "S");
    Assert.assertEquals("$.pR(S)", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void root_down_up() {
    StatePath sp = StatePath.resolve("/down/../name", "S");
    Assert.assertEquals("$.pU($.pI($.pR(S),'down'))", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }

  @Test
  public void dive_up() {
    StatePath sp = StatePath.resolve("path1/../name", "S");
    Assert.assertEquals("$.pU($.pI(S,'path1'))", sp.command);
    Assert.assertEquals("name", sp.name);
    Assert.assertFalse(sp.simple);
  }
}
