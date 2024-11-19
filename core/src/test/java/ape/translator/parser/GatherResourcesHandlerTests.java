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
package ape.translator.parser;

import ape.translator.env2.Scope;
import ape.translator.parser.token.Token;
import ape.translator.tree.definitions.*;
import ape.translator.tree.definitions.*;
import ape.translator.tree.definitions.config.DefineDocumentEvent;
import ape.translator.tree.privacy.DefineCustomPolicy;
import ape.translator.tree.types.structures.BubbleDefinition;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsStructure;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class GatherResourcesHandlerTests {

  @Test
  public void flow() {
    HashMap<String, String> includes = new HashMap<>();
    GatherResourcesHandler gi = new GatherResourcesHandler((in) -> {
      return includes.get(in);
    });
    gi.add((BubbleDefinition) null);
    gi.add((DefineConstructor) null);
    gi.add((DefineCustomPolicy) null);
    gi.add((DefineDispatcher) null);
    gi.add((DefineDocumentEvent) null);
    gi.add((DefineFunction) null);
    gi.add((DefineHandler) null);
    gi.add((DefineStateTransition) null);
    gi.add((DefineTest) null);
    gi.add((FieldDefinition) null);
    gi.add((IsEnum) null);
    gi.add((IsStructure) null);
    gi.add((Token) null);
    gi.add((AugmentViewerState) null);
    gi.add((DefineRPC) null);
    gi.add((DefineStatic) null);
    gi.add((DefineWebGet) null);
    gi.add((DefineWebPut) null);

    includes.put("bad", "public int ");
    includes.put("good", "public int x = 123;");
    includes.put("recurse", "@include good;");
    gi.add(new Include(null, new Token[]{Token.WRAP("recurse")}, null), Scope.makeRootDocument());
    Assert.assertEquals(0, gi.errors.size());
    Assert.assertTrue(gi.includes.contains("good"));
    Assert.assertTrue(gi.includes.contains("recurse"));

    gi.add(new Include(null, new Token[]{Token.WRAP("nope")}, null), Scope.makeRootDocument());
    Assert.assertEquals(1, gi.errors.size());
    gi.add(new Include(null, new Token[]{Token.WRAP("bad")}, null), Scope.makeRootDocument());
    Assert.assertEquals(2, gi.errors.size());
    gi.add((DefineService) null);
  }
}
