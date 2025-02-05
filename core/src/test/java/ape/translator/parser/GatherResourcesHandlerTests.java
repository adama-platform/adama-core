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
