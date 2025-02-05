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
package ape.translator.tree.expressions.operators;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.operands.BinaryOp;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.checking.ruleset.RuleSetAssignment;
import ape.translator.tree.types.checking.ruleset.RuleSetEnums;
import ape.translator.tree.types.checking.ruleset.RuleSetMaybe;
import ape.translator.tree.types.natives.TyNativeBoolean;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.natives.TyNativeMaybe;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsMap;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.function.Consumer;

/** binary operation (left $OP right) */
public class BinaryExpression extends Expression {
  public final Expression left;
  public final BinaryOp op;
  public final Token opToken;
  public final Expression right;
  public BinaryOperatorResult operatorResult;

  public BinaryExpression(final Expression left, final Token opToken, final Expression right) {
    this.left = left;
    this.opToken = opToken;
    op = BinaryOp.fromText(opToken.text);
    this.right = right;
    this.ingest(left);
    this.ingest(right);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    left.emit(yielder);
    yielder.accept(opToken);
    right.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    left.format(formatter);
    right.format(formatter);
  }

  private boolean areBothTypesEnums(final Environment environment, TyType leftPreMaybe, TyType rightPreMaybe) {
    final TyType left;
    if (RuleSetMaybe.IsMaybe(environment, leftPreMaybe, true)) {
      left = ((DetailContainsAnEmbeddedType) leftPreMaybe).getEmbeddedType(environment);
    } else {
      left = leftPreMaybe;
    }

    final TyType right;
    if (RuleSetMaybe.IsMaybe(environment, rightPreMaybe, true)) {
      right = ((DetailContainsAnEmbeddedType) rightPreMaybe).getEmbeddedType(environment);
    } else {
      right = rightPreMaybe;
    }

    final var aEnum = RuleSetEnums.IsEnum(environment, left, true);
    final var bEnum = RuleSetEnums.IsEnum(environment, right, true);
    if (aEnum && bEnum) {
      if (((IsEnum) left).name().equals(((IsEnum) right).name())) {
        return true;
      } else {
        environment.document.createError(DocumentPosition.sum(left, right), String.format("Type check failure: enum types are incompatible '%s' vs '%s'.", left.getAdamaType(), right.getAdamaType()));
        return false;
      }
    }
    return false;
  }

  private TyType getEnumTypeToUse(final Environment environment, TyType type) {
    TyType tyInt = new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("int")).withPosition(type);
    if (RuleSetMaybe.IsMaybe(environment, type, true)) {
      return new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("maybe"), new TokenizedItem<>(tyInt)).withPosition(tyInt);
    } else {
      return tyInt;
    }
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    environment.mustBeComputeContext(this);
    Environment rightEnv = environment.scopeWithComputeContext(ComputeContext.Computation);
    Environment leftEnv = op.leftAssignment ? environment.scopeWithComputeContext(ComputeContext.Assignment) : rightEnv;
    TyType typeLeft = left.typing(leftEnv, null);
    typeLeft = environment.rules.Resolve(typeLeft, false);
    TyType typeRight = right.typing(rightEnv, null);
    typeRight = environment.rules.Resolve(typeRight, false);

    if (typeLeft != null && typeRight != null) {
      if (op == BinaryOp.NotInside || op == BinaryOp.Inside) {
        TyType tyBool = new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, opToken);
        if (environment.rules.IsMap(typeRight)) {
          IsMap imap = (IsMap) typeRight;
          TyType domainType = environment.rules.Resolve(imap.getDomainType(environment), false);
          // just make sure the right side types
          TyType rangeType = environment.rules.Resolve(imap.getRangeType(environment), false);
          rangeType.typing(environment);
          // let the table do the magic
          boolean fromYes = RuleSetAssignment.CanTypeAStoreTypeB(environment, typeLeft, domainType, StorageTweak.None, false);
          boolean toYes = RuleSetAssignment.CanTypeAStoreTypeB(environment, domainType, typeLeft, StorageTweak.None, false);
          if (fromYes && toYes) {
            this.operatorResult = new BinaryOperatorResult(tyBool, "%s.has(%s)", true);
            return tyBool;
          } else {
            return null;
          }
        } else {
          environment.document.createError(DocumentPosition.sum(left, right), String.format("'%s' is unable to accept an containment check with '%s' as the domain.", typeRight.getAdamaType(), typeLeft.getAdamaType()));

        }
      }
      if (op.leftAssignment && typeLeft.behavior.isReadOnly) {
        environment.document.createError(DocumentPosition.sum(left, right), String.format("'%s' is unable to accept an assignment of '%s'.", typeLeft.getAdamaType(), typeRight.getAdamaType()));
      }
    }
    if (op == BinaryOp.Equal || op == BinaryOp.NotEqual) {
      if (areBothTypesEnums(environment, typeLeft, typeRight)) {
        typeLeft = getEnumTypeToUse(environment, typeLeft);
        typeRight = getEnumTypeToUse(environment, typeRight);
      }
    }

    operatorResult = BinaryOperatorTable.INSTANCE.find(typeLeft, op.javaOp, typeRight, environment);
    if (operatorResult != null) {
      return operatorResult.type.makeCopyWithNewPosition(typeLeft, TypeBehavior.ReadOnlyNativeValue).withPosition(typeRight);
    }
    return null;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    final var leftStr = new StringBuilder();
    final var rightStr = new StringBuilder();
    Environment rightEnv = environment.scopeWithComputeContext(ComputeContext.Computation);
    Environment leftEnv = op.leftAssignment ? environment.scopeWithComputeContext(ComputeContext.Assignment) : rightEnv;
    left.writeJava(leftStr, leftEnv);
    right.writeJava(rightStr, rightEnv);
    if (operatorResult != null) {
      if (operatorResult.reverse) {
        sb.append(String.format("%s", String.format(operatorResult.javaPattern, rightStr, leftStr)));
      } else {
        sb.append(String.format("%s", String.format(operatorResult.javaPattern, leftStr, rightStr)));
      }
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    left.free(environment);
    right.free(environment);
  }
}
