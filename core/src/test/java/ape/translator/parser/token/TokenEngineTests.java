/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
