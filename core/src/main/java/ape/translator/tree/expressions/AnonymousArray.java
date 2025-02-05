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
package ape.translator.tree.expressions;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.natives.TyNativeArray;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.structures.StorageSpecialization;
import ape.translator.tree.types.structures.StructureStorage;
import ape.translator.tree.types.traits.SupportsTwoPhaseTyping;

import java.util.ArrayList;
import java.util.function.Consumer;

/** an anonymous array of items [item1, item2, ..., itemN] */
public class AnonymousArray extends Expression implements SupportsTwoPhaseTyping {
  private static final TyNativeMessage EMPTY_MESSAGE = new TyNativeMessage(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("__EmptyMessageNoArgs_"), new StructureStorage(Token.WRAP("empty"), StorageSpecialization.Message, true, false, null));
  public final ArrayList<TokenizedItem<Expression>> elements;
  public Token closeBracketToken;
  public Token openBracketToken;

  public AnonymousArray(final Token openBracketToken) {
    elements = new ArrayList<>();
    this.openBracketToken = openBracketToken;
    ingest(openBracketToken);
  }

  /** add an anonymous object to the array */
  public void add(final TokenizedItem<Expression> aobject) {
    elements.add(aobject);
    ingest(aobject.item);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(openBracketToken);
    for (final TokenizedItem<Expression> element : elements) {
      element.emitBefore(yielder);
      element.item.emit(yielder);
      element.emitAfter(yielder);
    }
    yielder.accept(closeBracketToken);
  }

  @Override
  public void format(Formatter formatter) {
    boolean multiline = elements.size() > 1;
    if (multiline) {
      formatter.endLine(openBracketToken);
      formatter.tabUp();
      formatter.tabUp();
    }
    int n = elements.size();
    for (int k = 0; k < n; k++) {
      final TokenizedItem<Expression> element = elements.get(k);
      element.item.format(formatter);
      if (multiline) {
        Formatter.FirstAndLastToken fal = new Formatter.FirstAndLastToken();
        element.emitBefore(fal);
        element.item.emit(fal);
        element.emitAfter(fal);
        if (fal.last != null) {
          formatter.startLine(fal.first);
          formatter.endLine(fal.last);
        }
      }
    }
    if (multiline) {
      formatter.tabDown();
      formatter.startLine(closeBracketToken);
      formatter.tabDown();
    }
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    environment.mustBeComputeContext(this);
    if (suggestion != null) {
      if (environment.rules.IsNativeArray(suggestion, false)) {
        final var elementType = environment.rules.ExtractEmbeddedType(suggestion, false);
        for (final TokenizedItem<Expression> elementExpr : elements) {
          final var computedType = elementExpr.item.typing(environment, elementType);
          environment.rules.CanTypeAStoreTypeB(elementType, computedType, StorageTweak.None, false);
        }
        return suggestion;
      }
      return null;
    } else {
      var proposal = estimateType(environment, suggestion);
      if (proposal != null) {
        proposal = environment.rules.EnsureRegisteredAndDedupe(proposal, false);
        upgradeType(environment, proposal);
      }
      return proposal;
    }
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    final var me = (TyNativeArray) cachedType;
    if (me != null) {
      sb.append("new ").append(me.getJavaConcreteType(environment)).append(" {");
      var first = true;
      for (final TokenizedItem<Expression> element : elements) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        element.item.writeJava(sb, environment);
      }
      sb.append("}");
    }
  }

  public TyType estimateType(final Environment environment, final TyType suggestion) {
    TyType proposal = null;
    if (elements.size() > 0) {
      final var firstExpr = elements.get(0).item;
      if (firstExpr instanceof SupportsTwoPhaseTyping) {
        proposal = ((SupportsTwoPhaseTyping) firstExpr).estimateType(environment);
      } else {
        proposal = firstExpr.typing(environment, suggestion instanceof TyNativeArray ? environment.rules.ExtractEmbeddedType(suggestion, false) : null);
      }
    }
    if (proposal == null) {
      proposal = EMPTY_MESSAGE;
    }
    for (final TokenizedItem<Expression> elementExpr : elements) {
      TyType candidate = null;
      if (elementExpr.item instanceof SupportsTwoPhaseTyping) {
        candidate = ((SupportsTwoPhaseTyping) elementExpr.item).estimateType(environment);
      } else {
        candidate = elementExpr.item.typing(environment, null);
      }
      proposal = environment.rules.GetMaxType(proposal, candidate, false);
    }
    if (proposal != null) {
      return new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, proposal.withPosition(this), null).withPosition(this);
    } else {
      return null;
    }
  }

  public void end(final Token closeBracketToken) {
    this.closeBracketToken = closeBracketToken;
    ingest(closeBracketToken);
  }

  @Override
  public TyType estimateType(final Environment environment) {
    return estimateType(environment, null);
  }

  @Override
  public void upgradeType(final Environment environment, final TyType proposalArray) {
    cachedType = proposalArray.withPosition(this);
    if (proposalArray != null && proposalArray instanceof TyNativeArray) {
      final var proposalElement = ((TyNativeArray) proposalArray).getEmbeddedType(environment);
      for (final TokenizedItem<Expression> elementExpr : elements) {
        if (elementExpr.item instanceof SupportsTwoPhaseTyping) {
          ((SupportsTwoPhaseTyping) elementExpr.item).upgradeType(environment, proposalElement);
        }
        // TODO: introduce an expression to coerce the type into the proposal element
      }
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    for (var expr : elements) {
      expr.item.free(environment);
    }
  }
}
