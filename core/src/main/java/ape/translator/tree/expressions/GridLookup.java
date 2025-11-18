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

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.checking.ruleset.*;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.natives.TyNativeList;
import ape.translator.tree.types.natives.TyNativeLong;
import ape.translator.tree.types.natives.TyNativeMaybe;
import ape.translator.tree.types.traits.IsGrid;
import ape.translator.tree.types.traits.IsMap;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;
import ape.translator.tree.types.traits.details.DetailIndexLookup;
import ape.translator.tree.types.traits.details.IndexLookupStyle;

import java.util.function.Consumer;

/**
 * lookups/creates a item in a grid; grids are "infinite" in a sense.
 */
public class GridLookup extends Expression {
  public final Expression arg1;
  public final Token bracketCloseToken;
  public final Token bracketOpenToken;
  public final Expression expression;
  public final Token comma;
  public final Expression arg2;
  private String castArg1;
  private String castArg2;

  public GridLookup(final Expression expression, final Token bracketOpenToken, final Expression arg1, final Token comma, final Expression arg2, final Token bracketCloseToken) {
    this.expression = expression;
    this.bracketOpenToken = bracketOpenToken;
    this.arg1 = arg1;
    this.comma = comma;
    this.arg2 = arg2;
    this.bracketCloseToken = bracketCloseToken;
    this.ingest(expression);
    this.ingest(arg1);
    this.ingest(arg2);
    this.ingest(bracketCloseToken);
    this.castArg1 = null;
    this.castArg2 = null;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    expression.emit(yielder);
    yielder.accept(bracketOpenToken);
    arg1.emit(yielder);
    yielder.accept(comma);
    arg2.emit(yielder);
    yielder.accept(bracketCloseToken);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
    arg1.format(formatter);
    arg2.format(formatter);
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    // come up with a more specific form
    final var typeExpr = expression.typing(environment, null /* no suggestion */);
    TyType resultType = null;
    if (environment.rules.IsGrid(typeExpr)) {
      final var gridType = (IsGrid) typeExpr;
      TyType domainType = gridType.getDomainType(environment);
      final var typeArg1 = arg1.typing(environment.scopeWithComputeContext(ComputeContext.Computation), domainType);
      final var typeArg2 = arg2.typing(environment.scopeWithComputeContext(ComputeContext.Computation), domainType);
      if (domainType instanceof TyNativeLong && typeArg1 instanceof TyNativeInteger) {
        castArg1 = "long";
      }
      if (domainType instanceof TyNativeLong && typeArg2 instanceof TyNativeInteger) {
        castArg2 = "long";
      }
      boolean good1 = environment.rules.CanTypeAStoreTypeB(domainType, typeArg1, StorageTweak.None, false);
      boolean good2 = environment.rules.CanTypeAStoreTypeB(domainType, typeArg2, StorageTweak.None, false);
      if (good1 && good2) {
        if (RuleSetGrid.IsNativeGrid(environment, typeExpr)) {
          resultType = new TyNativeMaybe(TypeBehavior.ReadWriteWithSetGet, null, null, new TokenizedItem<>(gridType.getRangeType(environment))).withPosition(this);
        } else {
          resultType = gridType.getRangeType(environment);
        }
      }
    }
    return resultType;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    expression.writeJava(sb, environment);
    sb.append(".lookup(");
    if (castArg1 != null) {
      sb.append("(").append(castArg1).append(")");
    }
    arg1.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(", ");
    if (castArg2 != null) {
      sb.append("(").append(castArg2).append(")");
    }
    arg2.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(")");
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
    arg1.free(environment);
    arg2.free(environment);
  }
}
