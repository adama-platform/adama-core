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

public class DefineWebOptions extends Definition implements UriAction {
  public final Token webToken;
  public final Token optionsToken;
  public final Uri uri;
  public final WebGuard guard;
  public final Block code;

  public DefineWebOptions(Token webToken, Token optionsToken, Uri uri, WebGuard guard, Block code) {
    this.webToken = webToken;
    this.optionsToken = optionsToken;
    this.uri = uri;
    this.guard = guard;
    this.code = code;
    ingest(webToken);
    ingest(code);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(webToken);
    yielder.accept(optionsToken);
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
    Environment env = environment.scopeAsWeb("options");
    uri.extendInto(env);
    uri.typing(env);
    return env;
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
