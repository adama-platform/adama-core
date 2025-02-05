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
import ape.translator.tree.definitions.web.Uri;
import ape.translator.tree.definitions.web.UriAction;
import ape.translator.tree.definitions.web.WebGuard;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.TreeMap;
import java.util.function.Consumer;

/** defines a URI to get a web resource */
public class DefineWebDelete extends Definition implements UriAction {

  public final Token webToken;
  public final Token deleteToken;
  public final Uri uri;
  public final WebGuard guard;
  public final Block code;

  public DefineWebDelete(Token webToken, Token deleteToken, Uri uri, WebGuard guard, Block code) {
    this.webToken = webToken;
    this.deleteToken = deleteToken;
    this.uri = uri;
    this.guard = guard;
    this.code = code;
    ingest(webToken);
    ingest(code);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(webToken);
    yielder.accept(deleteToken);
    uri.emit(yielder);
    if (guard != null) {
      guard.emit(yielder);
    }
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(webToken);
    uri.format(formatter);
    if (guard != null) {
      guard.format(formatter);
    }
    code.format(formatter);
  }

  @Override
  public TreeMap<String, TyType> parameters() {
    return uri.variables;
  }

  public Environment next(Environment environment) {
    Environment env = environment.scopeAsAbortable().scopeAsWeb("delete");
    uri.extendInto(env);
    uri.typing(env);
    return env.scopeWithCache("__currentWebCache");
  }

  public void typing(TypeCheckerRoot checker) {
    if (guard != null) {
      guard.typing(checker);
    }
    FreeEnvironment fe = FreeEnvironment.root();
    code.free(fe);
    checker.register(fe.free, (environment) -> {
      Environment env = next(environment);
      if (code.typing(env) == ControlFlow.Open) {
        environment.document.createError(this, String.format("The @web handlers must return a message"));
      }
    });
  }
}
