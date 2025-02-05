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
package ape.rxhtml.routing;

import org.junit.Assert;
import org.junit.Test;

public class InstructionsTests {
  @Test
  public void root() {
    Instructions instructions = Instructions.parse("/");
    Assert.assertEquals("/", instructions.normalized);
    Assert.assertEquals("/", instructions.formula);
    Assert.assertEquals("['fixed','']", instructions.javascript);
  }
  @Test
  public void simple() {
    Instructions instructions = Instructions.parse("/xyz/abc");
    Assert.assertEquals("/xyz/abc", instructions.normalized);
    Assert.assertEquals("/xyz/abc", instructions.formula);
    Assert.assertEquals("['fixed','xyz','fixed','abc']", instructions.javascript);
  }
  @Test
  public void simple_introduce() {
    Instructions instructions = Instructions.parse("xyz/abc");
    Assert.assertEquals("/xyz/abc", instructions.normalized);
    Assert.assertEquals("/xyz/abc", instructions.formula);
    Assert.assertEquals("['fixed','xyz','fixed','abc']", instructions.javascript);
  }
  @Test
  public void var_number() {
    Instructions instructions = Instructions.parse("/xyz/$s:double");
    Assert.assertEquals("/xyz/$number", instructions.normalized);
    Assert.assertEquals("/xyz/{s}", instructions.formula);
    Assert.assertEquals("['fixed','xyz','number','s']", instructions.javascript);
  }
  @Test
  public void var_text() {
    Instructions instructions = Instructions.parse("/xyz/$s:str");
    Assert.assertEquals("/xyz/$text", instructions.normalized);
    Assert.assertEquals("/xyz/{s}", instructions.formula);
    Assert.assertEquals("['fixed','xyz','text','s']", instructions.javascript);
  }
  @Test
  public void var_text_is_default() {
    Instructions instructions = Instructions.parse("/xyz/$s");
    Assert.assertEquals("/xyz/$text", instructions.normalized);
    Assert.assertEquals("/xyz/{s}", instructions.formula);
    Assert.assertEquals("['fixed','xyz','text','s']", instructions.javascript);
  }
  @Test
  public void var_ignore_star() {
    Instructions instructions = Instructions.parse("/xyz/$s*/path");
    Assert.assertEquals("/xyz/$text/path", instructions.normalized);
    Assert.assertEquals("/xyz/{s}/path", instructions.formula);
    Assert.assertEquals("['fixed','xyz','text','s','fixed','path']", instructions.javascript);
  }
  @Test
  public void suffix() {
    Instructions instructions = Instructions.parse("/xyz/$s*");
    Assert.assertEquals("/xyz/$text", instructions.normalized);
    Assert.assertEquals("/xyz/{s}", instructions.formula);
    Assert.assertEquals("['fixed','xyz','suffix','s']", instructions.javascript);
  }
}
