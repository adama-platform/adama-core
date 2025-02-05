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

import ape.translator.env2.Scope;
import ape.translator.parser.Parser;
import ape.translator.tree.SymbolIndex;
import ape.translator.tree.expressions.constants.TimeSpanConstant;
import org.junit.Assert;
import org.junit.Test;

public class TokenEngineTests {
  @Test
  public void coverage() {
    String xml = "<X>";
    new TokenEngine("demo", xml.codePoints().iterator()).position();
  }

  @Test
  public void timespan_neg() throws Exception {
    TokenEngine engine = new TokenEngine("demo", "@timespan -1 min".codePoints().iterator());
    Scope rootScope = Scope.makeRootDocument();
    Parser p = new Parser(engine, new SymbolIndex(), rootScope);
    TimeSpanConstant tsc = (TimeSpanConstant) p.atomic(rootScope);
    StringBuilder sb = new StringBuilder();
    tsc.emit((t) -> sb.append("[" + t.text + "]"));
    Assert.assertEquals("[@timespan][-1][min]", sb.toString());
  }

  @Test
  public void timespan_neg_space() throws Exception {
    TokenEngine engine = new TokenEngine("demo", "@timespan - 1 min".codePoints().iterator());
    Scope rootScope = Scope.makeRootDocument();
    Parser p = new Parser(engine, new SymbolIndex(), rootScope);
    TimeSpanConstant tsc = (TimeSpanConstant) p.atomic(rootScope);
    StringBuilder sb = new StringBuilder();
    tsc.emit((t) -> sb.append("[" + t.text + "]"));
    Assert.assertEquals("[@timespan][- 1][min]", sb.toString());
  }
}
