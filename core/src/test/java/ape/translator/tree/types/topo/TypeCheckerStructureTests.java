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
package ape.translator.tree.types.topo;

import ape.translator.env.*;
import ape.translator.env.*;
import ape.translator.parser.token.Token;
import ape.translator.tree.Document;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.structures.StorageSpecialization;
import org.junit.Test;

import java.util.Collections;

public class TypeCheckerStructureTests {
  @Test
  public void sanity() {
    TypeCheckerStructure tcs = new TypeCheckerStructure();
    tcs.issueError(DocumentPosition.ZERO, "message");
    tcs.define(Token.WRAP("x"), Collections.emptySet(), (env) -> {});
    tcs.register(Collections.singleton("x"), (env) -> {});
    TypeCheckerRoot root = new TypeCheckerRoot();
    tcs.transferInto("R", StorageSpecialization.Record, root);
    EnvironmentState es = new EnvironmentState(GlobalObjectPool.createPoolWithStdLib(RuntimeEnvironment.Beta), CompilerOptions.start().make());
    root.check(Environment.fresh(new Document(), es));
  }
}
