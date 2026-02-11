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
package ape.translator.tree.definitions;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.watcher.WatchSet;
import ape.translator.tree.watcher.WatchSetWatcher;

import java.util.function.Consumer;

public class DefineExport extends Definition {
  public final Token open;
  public final Token name;
  public final Token equals;
  public final Expression expression;
  public final Token semicolon;
  public final WatchSet watching;

  public DefineExport(Token open, Token name, Token equals, Expression expression, Token semicolon) {
    this.open = open;
    this.name = name;
    this.equals = equals;
    this.expression = expression;
    this.semicolon = semicolon;
    this.watching = new WatchSet();
    ingest(open);
    ingest(semicolon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(open);
    yielder.accept(name);
    yielder.accept(equals);
    expression.emit(yielder);
    yielder.accept(semicolon);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }
  public Environment next(Environment environment) {
    Environment env = environment.scopeAsReadOnlyBoundary().scopeAsExport().scopeWithComputeContext(ComputeContext.Computation);
    return env;
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    expression.free(fe);
    checker.register(fe.free, (environment) -> {
      Environment env = next(environment).watch(new WatchSetWatcher(environment, watching));
      TyType type = expression.typing(env, null);
      if (type == null) {
        environment.document.createError(this, String.format("The @export handler failed to type"));
      } else {
        type = env.rules.Resolve(type, true);
        env.rules.IsNativeMessage(type, false);
      }
    });
  }
}
