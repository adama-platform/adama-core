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

import ape.runtime.sys.CoreRequestContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.definitions.config.DefineDocumentEvent;
import ape.translator.tree.definitions.config.DocumentConfig;
import ape.translator.tree.definitions.config.StaticPiece;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.natives.TyInternalReadonlyClass;

import java.util.ArrayList;
import java.util.function.Consumer;

/** group all the static methods and properties here */
public class DefineStatic extends Definition {
  public final Token openContext;
  public final Token contextName;
  public final Token closeContext;
  public final ArrayList<DefineDocumentEvent> events;
  public final ArrayList<DocumentConfig> configs;
  private final Token staticToken;
  private final Token openToken;
  private final ArrayList<StaticPiece> definitions;
  private final Token closeToken;

  public DefineStatic(Token staticToken, Token openContext, Token contextName, Token closeContext, Token openToken, ArrayList<StaticPiece> definitions, Token closeToken) {
    this.staticToken = staticToken;
    this.openContext = openContext;
    this.contextName = contextName;
    this.closeContext = closeContext;
    this.openToken = openToken;
    this.definitions = definitions;
    this.closeToken = closeToken;
    this.events = new ArrayList<>();
    this.configs = new ArrayList<>();
    for (StaticPiece definition : definitions) {
      if (definition instanceof DefineDocumentEvent) {
        events.add((DefineDocumentEvent) definition);
        if (contextName != null) {
          ((DefineDocumentEvent) definition).setContextVariable(contextName.text);
        }
      }
      if (definition instanceof DocumentConfig) {
        configs.add((DocumentConfig) definition);
      }
    }
    ingest(staticToken, openToken, closeToken);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(staticToken);
    if (openContext != null) {
      yielder.accept(openContext);
      yielder.accept(contextName);
      yielder.accept(closeContext);
    }
    yielder.accept(openToken);
    for (StaticPiece definition : definitions) {
      definition.emit(yielder);
    }
    yielder.accept(closeToken);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(staticToken);
    formatter.endLine(openToken);
    formatter.tabUp();
    for (StaticPiece defn : definitions) {
      defn.format(formatter);
    }
    formatter.tabDown();
    formatter.endLine(closeToken);
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    if (contextName != null) {
      fe.define(contextName.text);
    }
    checker.register(fe.free, (environment) -> {
      Environment next = environment.staticPolicy().scopeStatic();
      if (contextName != null) {
        next.define(contextName.text, new TyInternalReadonlyClass(CoreRequestContext.class), true, this);
      }
      for (StaticPiece definition : definitions) {
        definition.typing(next);
      }
    });
  }
}
