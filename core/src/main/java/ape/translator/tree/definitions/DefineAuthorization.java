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

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeString;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.function.Consumer;

/** defines an authorization handler */
@Deprecated
public class DefineAuthorization extends Definition {
  public final Token authorize;
  public final Token openParen;
  public final Token username;
  public final Token comma;
  public final Token password;
  public final Token endParen;
  public final Block code;

  public DefineAuthorization(Token authorize, Token openParen, Token username, Token comma, Token password, Token endParen, Block code) {
    this.authorize = authorize;
    this.openParen = openParen;
    this.username = username;
    this.comma = comma;
    this.password = password;
    this.endParen = endParen;
    this.code = code;
    ingest(authorize);
    ingest(code);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(authorize);
    yielder.accept(openParen);
    yielder.accept(username);
    yielder.accept(comma);
    yielder.accept(password);
    yielder.accept(endParen);
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(authorize);
    code.format(formatter);
  }

  public Environment next(Environment environment) {
    Environment env = environment.scopeAsAuthorize();
    TyNativeString tyStr = new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, authorize);
    env.define(username.text, tyStr, true, this);
    env.define(password.text, tyStr, true, this);
    return env;
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    code.free(fe);
    checker.register(fe.free, (env) -> {
      Environment toUse = next(env);
      toUse.setReturnType(new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, authorize));
      ControlFlow flow = code.typing(toUse);
      if (flow == ControlFlow.Open) {
        checker.issueError(this, "@authorize must either return a string or abort");
      }
    });
  }
}
