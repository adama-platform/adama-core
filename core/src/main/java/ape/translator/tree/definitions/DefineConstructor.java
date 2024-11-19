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
import ape.translator.tree.statements.Block;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.natives.TyNativeMessage;

import java.util.function.Consumer;

/** defines a constructor which runs when the document is created */
public class DefineConstructor extends Definition {
  public final Block code;
  public final Token constructToken;
  public final Token endParenToken;
  public final Token messageNameToken;
  public final Token messageTypeToken;
  public final Token openParenToken;
  public TyType unifiedMessageType;
  public String unifiedMessageTypeNameToUse;

  public DefineConstructor(final Token constructToken, final Token openParenToken, final Token messageTypeToken, final Token messageNameToken, final Token endParenToken, final Block code) {
    this.constructToken = constructToken;
    this.openParenToken = openParenToken;
    this.messageTypeToken = messageTypeToken;
    this.messageNameToken = messageNameToken;
    this.endParenToken = endParenToken;
    this.code = code;
    ingest(constructToken);
    ingest(code);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(constructToken);
    if (messageNameToken != null) {
      yielder.accept(openParenToken);
      yielder.accept(messageTypeToken);
      yielder.accept(messageNameToken);
      yielder.accept(endParenToken);
    }
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(constructToken);
    code.format(formatter);
  }

  // TODO: move to typing()
  @Deprecated
  public void internalTyping(final Environment environment) {
    final var next = environment.scopeAsPolicy().scopeAsConstructor();
    if (messageNameToken != null && messageTypeToken != null && unifiedMessageType != null) {
      next.define(messageNameToken.text, unifiedMessageType, false, unifiedMessageType);
      unifiedMessageTypeNameToUse = ((TyNativeMessage) unifiedMessageType).name;
    }
    next.setReturnType(null);
    if (code != null) {
      code.typing(next);
    }
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    code.free(fe);
    checker.register(fe.free, (env) -> {
      internalTyping(env);
    });
  }
}
