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
package ape.translator.tree;

import ape.translator.env.Environment;
import ape.translator.tree.expressions.InjectExpression;
import ape.translator.tree.types.natives.TyNativeVoid;
import org.junit.Test;

public class InjectExpressionTests {
  @Test
  public void coverage() {
    final InjectExpression ie =
        new InjectExpression(new TyNativeVoid()) {
          @Override
          public void writeJava(final StringBuilder sb, final Environment environment) {}
        };
    ie.typing(null, null);
    ie.emit(null);
    ie.writeJava((StringBuilder) null, null);
  }
}
