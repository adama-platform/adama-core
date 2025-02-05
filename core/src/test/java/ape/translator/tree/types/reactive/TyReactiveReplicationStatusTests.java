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
package ape.translator.tree.types.reactive;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.structures.ReplicationDefinition;
import org.junit.Assert;
import org.junit.Test;

public class TyReactiveReplicationStatusTests {
  @Test
  public void coverage() {
    ReplicationDefinition defn = new ReplicationDefinition(null, null, Token.WRAP("service"), null, Token.WRAP("method"), null, Token.WRAP("name"), null, null, null);
    TyReactiveReplicationStatus ty = new TyReactiveReplicationStatus(defn);
    ty.format(null);
    ty.emitInternal((t) -> {});
    Assert.assertEquals("replication", ty.getAdamaType());
    Assert.assertEquals("RxReplicationStatus", ty.getJavaBoxType(null));
    Assert.assertEquals("RxReplicationStatus", ty.getJavaConcreteType(null));
    ty.makeCopyWithNewPositionInternal(DocumentPosition.ZERO, TypeBehavior.ReadWriteNative);
    ty.typing(null);
    JsonStreamWriter writer = new JsonStreamWriter();
    ty.writeTypeReflectionJson(writer, ReflectionSource.Root);
    Assert.assertEquals("{\"nature\":\"reactive_value\",\"type\":\"replication_status\"}", writer.toString());
    Assert.assertEquals("DReplicationStatus", ty.getDeltaType(null));
    Assert.assertNull(ty.lookupMethod("nope", null));
  }
}
