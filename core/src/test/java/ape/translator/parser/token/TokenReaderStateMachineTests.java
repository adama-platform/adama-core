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

import java.util.ArrayList;

public class TokenReaderStateMachineTests {
  @Test
  public void symbol_cluster() throws Exception {
    final var list = new ArrayList<Token>();
    final var trsm = new TokenReaderStateMachine("Source", list::add);
    trsm.consume('+');
    trsm.consume('+');
    trsm.consume('+');
    trsm.consume('+');
    trsm.consume('1');
    Assert.assertEquals(4, list.size());
  }

  @Test
  public void template_1() throws Exception {
    final var list = new ArrayList<Token>();
    final var trsm = new TokenReaderStateMachine("Source", list::add);
    for (int cp : "`a`HI\nTHERE`a`".codePoints().toArray()) {
      trsm.consume(cp);
    }
    Assert.assertEquals(1, list.size());
    Token token = list.get(0);
    Assert.assertEquals(MajorTokenType.Template, token.majorType);
    Assert.assertEquals("`a`HI\nTHERE`a`", token.text);
  }

  @Test
  public void template_not_closed() throws Exception {
    final var list = new ArrayList<Token>();
    final var trsm = new TokenReaderStateMachine("Source", list::add);
    for (int cp : "`a`HI THERE`b`".codePoints().toArray()) {
      trsm.consume(cp);
    }
    Assert.assertEquals(0, list.size());
  }
}
