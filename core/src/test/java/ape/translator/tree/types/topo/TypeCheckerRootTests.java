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
package ape.translator.tree.types.topo;

import ape.translator.env.*;
import ape.translator.env.*;
import ape.translator.parser.token.Token;
import ape.translator.tree.Document;
import ape.translator.tree.common.DocumentPosition;
import org.junit.Test;

import java.util.Collections;

public class TypeCheckerRootTests {
  @Test
  public void sanity() {
    TypeCheckerRoot tcr = new TypeCheckerRoot();
    tcr.issueError(DocumentPosition.ZERO, "message");
    tcr.define(Token.WRAP("x"), Collections.emptySet(), (env) -> {});
    tcr.register(Collections.singleton("x"), (env) -> {});
    tcr.alias("::x", "x");
    EnvironmentState es = new EnvironmentState(GlobalObjectPool.createPoolWithStdLib(RuntimeEnvironment.Beta), CompilerOptions.start().make(), RuntimeEnvironment.Tooling);
    tcr.check(Environment.fresh(new Document(), es));
  }
}
