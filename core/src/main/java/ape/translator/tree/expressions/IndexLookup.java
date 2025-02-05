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
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.checking.ruleset.*;
import ape.translator.tree.types.checking.ruleset.*;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.natives.TyNativeList;
import ape.translator.tree.types.natives.TyNativeLong;
import ape.translator.tree.types.natives.TyNativeMaybe;
import ape.translator.tree.types.traits.IsMap;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;
import ape.translator.tree.types.traits.details.DetailIndexLookup;
import ape.translator.tree.types.traits.details.IndexLookupStyle;

import java.util.function.Consumer;

/**
 * return a maybe type from an index lookup. The maybe forces a range check, so it is always valid.
 */
public class IndexLookup extends Expression {
  public final Expression arg;
  public final Token bracketCloseToken;
  public final Token bracketOpenToken;
  public final Expression expression;
  private IndexLookupStyle lookupStyle;
  private String castArg;

  public IndexLookup(final Expression expression, final Token bracketOpenToken, final Expression arg, final Token bracketCloseToken) {
    this.expression = expression;
    this.bracketOpenToken = bracketOpenToken;
    this.arg = arg;
    this.bracketCloseToken = bracketCloseToken;
    this.ingest(expression);
    this.ingest(arg);
    this.ingest(bracketCloseToken);
    lookupStyle = IndexLookupStyle.Unknown;
    this.castArg = null;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    expression.emit(yielder);
    yielder.accept(bracketOpenToken);
    arg.emit(yielder);
    yielder.accept(bracketCloseToken);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
    arg.format(formatter);
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    // come up with a more specific form
    final var typeExpr = expression.typing(environment, null /* no suggestion */);
    TyType resultType = null;
    if (RuleSetTable.IsReactiveTable(environment, typeExpr)) {
      lookupStyle = IndexLookupStyle.ExpressionLookupMethod;
      final var typeArg = arg.typing(environment.scopeWithComputeContext(ComputeContext.Computation), new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null));
      final var elementType = ((DetailContainsAnEmbeddedType) typeExpr).getEmbeddedType(environment);
      final var maybeElementType = new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem(elementType));
      if (RuleSetLists.IsNativeListOfInt(environment, typeArg, true)) {
        resultType = new TyNativeList(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem<>(maybeElementType));
      } else {
        if (RuleSetMaybe.IsMaybeIntegerOrJustInteger(environment, typeArg, false)) {
          resultType = maybeElementType;
        }
      }
    } else if (RuleSetCommon.IsJson(environment, typeExpr, true)) {
      lookupStyle = IndexLookupStyle.DerefJson;
      final var typeArg = arg.typing(environment.scopeWithComputeContext(ComputeContext.Computation), new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null));
      RuleSetCommon.IsInteger(environment, typeArg, false);
      resultType = typeExpr;
    } else if (environment.rules.IsMap(typeExpr)) {
      final var mapType = (IsMap) typeExpr;
      final var typeArg = arg.typing(environment.scopeWithComputeContext(ComputeContext.Computation), mapType.getDomainType(environment));
      if (environment.state.isContextAssignment() && RuleSetMap.IsReactiveMap(environment, typeExpr)) {
        TyType domainType = mapType.getDomainType(environment);
        if (domainType instanceof TyNativeLong && typeArg instanceof TyNativeInteger) {
          castArg = "long";
        }
        lookupStyle = IndexLookupStyle.ExpressionGetOrCreateMethod;
        if (environment.rules.CanTypeAStoreTypeB(mapType.getDomainType(environment), typeArg, StorageTweak.None, false)) {
          resultType = mapType.getRangeType(environment);
        }
      } else {
        lookupStyle = IndexLookupStyle.ExpressionLookupMethod;
        if (environment.rules.CanTypeAStoreTypeB(mapType.getDomainType(environment), typeArg, StorageTweak.None, false)) {
          resultType = new TyNativeMaybe(TypeBehavior.ReadWriteWithSetGet, null, null, new TokenizedItem<>(mapType.getRangeType(environment))).withPosition(this);
        }
      }
    } else {
      environment.rules.IsIterable(typeExpr, false);
      if (typeExpr instanceof DetailIndexLookup) {
        lookupStyle = ((DetailIndexLookup) typeExpr).getLookupStyle(environment);
      }
      final var typeArg = arg.typing(environment.scopeWithComputeContext(ComputeContext.Computation), new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null));
      RuleSetMaybe.IsMaybeIntegerOrJustInteger(environment, typeArg, false);
      if (typeExpr != null && typeExpr instanceof DetailContainsAnEmbeddedType) {
        final var elementType = ((DetailContainsAnEmbeddedType) typeExpr).getEmbeddedType(environment);
        if (elementType != null) {
          resultType = new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem<>(elementType)).withPosition(this);
        }
      }
    }
    return resultType;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    if (lookupStyle == IndexLookupStyle.ExpressionLookupMethod) {
      expression.writeJava(sb, environment);
      sb.append(".lookup(");
      arg.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(")");
    } else if (lookupStyle == IndexLookupStyle.DerefJson) {
      expression.writeJava(sb, environment);
      sb.append(".deref(");
      arg.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(")");
    } else if (lookupStyle == IndexLookupStyle.ExpressionGetOrCreateMethod) {
      expression.writeJava(sb, environment);
      sb.append(".getOrCreate(");
      if (castArg != null) {
        sb.append("(").append(castArg).append(") ");
      }
      arg.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(")");
    } else if (lookupStyle == IndexLookupStyle.UtilityFunction) {
      sb.append("Utility.lookup(");
      expression.writeJava(sb, environment);
      sb.append(", ");
      arg.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(")");
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
    arg.free(environment);
  }
}
