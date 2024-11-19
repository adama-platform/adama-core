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

import ape.runtime.json.JsonStreamWriter;
import ape.translator.parser.token.Token;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.natives.TyNativeTuple;
import org.junit.Assert;
import org.junit.Test;

public class SillyTupleCoverage {
  @Test
  public void coverage() {
    TyNativeTuple tuple = new TyNativeTuple(TypeBehavior.ReadOnlyNativeValue, Token.WRAP("tuple"), Token.WRAP("tuple"));
    tuple.add(Token.WRAP("HI"), new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, Token.WRAP("tuple"), Token.WRAP("int")));
    tuple.emit((token) -> {});
    try {
      tuple.getJavaBoxType(null);
      Assert.fail();
    } catch (UnsupportedOperationException uso) {
    }
    try {
      tuple.getJavaConcreteType(null);
      Assert.fail();
    } catch (UnsupportedOperationException uso) {
    }
    tuple.getAdamaType();
    tuple.typing(null);
    tuple.makeCopyWithNewPosition(tuple, TypeBehavior.ReadOnlyNativeValue);
    JsonStreamWriter writer = new JsonStreamWriter();
    tuple.writeTypeReflectionJson(writer, ReflectionSource.Root);


  }
}
