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
package ape.translator.tree.definitions.config;

import ape.runtime.sys.CoreRequestContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.definitions.DocumentEvent;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.natives.TyInternalReadonlyClass;
import ape.translator.tree.types.natives.TyNativeAsset;
import ape.translator.tree.types.natives.TyNativeBoolean;

import java.util.function.Consumer;

/** defines an event for when connected or not */
public class DefineDocumentEvent extends StaticPiece {
  public final Token parameterNameToken;
  public final Token closeParen;
  public final Block code;
  public final Token eventToken;
  public final Token openParen;
  public final DocumentEvent which;
  public String contextVariable;

  public DefineDocumentEvent(final Token eventToken, final DocumentEvent which, final Token openParen, final Token parameterNameToken, final Token closeParen, final Block code) {
    this.eventToken = eventToken;
    this.which = which;
    this.openParen = openParen;
    this.parameterNameToken = parameterNameToken;
    this.closeParen = closeParen;
    this.code = code;
    this.contextVariable = null;
    ingest(code);
  }

  public void setContextVariable(String contextVariable) {
    this.contextVariable = contextVariable;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(eventToken);
    if (openParen != null) {
      yielder.accept(openParen);
      yielder.accept(parameterNameToken);
      yielder.accept(closeParen);
    }
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(eventToken);
    code.format(formatter);
  }

  @Override
  public void typing(final Environment environment) {
    internalTyping(environment);
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    if (parameterNameToken != null) {
      fe.define(parameterNameToken.text);
    }
    code.free(fe);
    checker.register(fe.free, (env) -> internalTyping(env.scopeAsPolicy()));
  }

  public void internalTyping(final Environment environment) {
    switch (which) {
      case AssetAttachment: {
        if (parameterNameToken == null) {
          environment.document.createError(this, String.format("The @attached a parameter (i.a. @attached(asset) {...})"));
        }
      }
    }
    ControlFlow codeControlFlow = code.typing(nextEnvironment(environment));
    if (codeControlFlow == ControlFlow.Open) {
      switch (which) {
        case ClientConnected:
          environment.document.createError(this, String.format("The @connected handler must return a boolean"));
          return;
        case Delete:
          environment.document.createError(this, String.format("The @delete handler must return a boolean"));
          return;
        case AskCreation:
          environment.document.createError(this, String.format("The 'create' policy must return a boolean"));
          return;
        case AskInvention:
          environment.document.createError(this, String.format("The 'invent' policy must return a boolean"));
          return;
        case AskSendWhileDisconnected:
          environment.document.createError(this, String.format("The 'send' policy must return a boolean"));
          return;
        case AskAssetAttachment:
          environment.document.createError(this, String.format("The @can_attach handler must return a boolean"));
          return;
      }
    }
  }

  public Environment nextEnvironment(final Environment environment) {
    if (which == DocumentEvent.AskCreation || which == DocumentEvent.AskInvention || which == DocumentEvent.AskSendWhileDisconnected) {
      Environment next = environment.staticPolicy().scopeStatic();
      next.setReturnType(new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, eventToken));
      if (contextVariable != null) {
        next.define(contextVariable, new TyInternalReadonlyClass(CoreRequestContext.class), true, this);
      }
      return next;
    }
    boolean readonly = which == DocumentEvent.AskAssetAttachment;
    final var next = readonly ? environment.scopeAsReadOnlyBoundary().scopeAsDocumentEvent() : environment.scopeAsDocumentEvent();
    if ("boolean".equals(which.returnType)) {
      next.setReturnType(new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, eventToken).withPosition(this));
    }
    if (which == DocumentEvent.AssetAttachment && parameterNameToken != null) {
      next.define(parameterNameToken.text, new TyNativeAsset(TypeBehavior.ReadOnlyNativeValue, null, parameterNameToken).withPosition(this), true, this);
    }
    return next;
  }
}
