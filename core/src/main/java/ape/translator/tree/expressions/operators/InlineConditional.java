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
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.WrapInstruction;
import ape.translator.tree.types.traits.SupportsTwoPhaseTyping;

import java.util.function.Consumer;

/** ternary operator / inline condition (bool ? tExpr : fExpr) */
public class InlineConditional extends Expression implements SupportsTwoPhaseTyping {
  public final Token colonToken;
  public final Expression condition;
  public final Expression falseValue;
  public final Token questionToken;
  public final Expression trueValue;
  private WrapInstruction wrapInstruction;

  /**
   * ternary operator (https://en.wikipedia.org/wiki/%3F:)
   * @param condition the condition to check
   * @param questionToken the token for the ?
   * @param trueValue value when condition is true
   * @param colonToken the token for the :
   * @param falseValue value when condition is false
   */
  public InlineConditional(final Expression condition, final Token questionToken, final Expression trueValue, final Token colonToken, final Expression falseValue) {
    this.condition = condition;
    this.questionToken = questionToken;
    this.trueValue = trueValue;
    this.colonToken = colonToken;
    this.falseValue = falseValue;
    this.ingest(condition);
    this.ingest(trueValue);
    this.ingest(falseValue);
    wrapInstruction = WrapInstruction.None;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    condition.emit(yielder);
    yielder.accept(questionToken);
    trueValue.emit(yielder);
    yielder.accept(colonToken);
    falseValue.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    condition.format(formatter);
    trueValue.format(formatter);
    falseValue.format(formatter);
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    return typingReal(environment, suggestion, true);
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    sb.append("(");
    condition.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(" ? ");
    if (wrapInstruction == WrapInstruction.WrapAWithMaybe) {
      sb.append("new NtMaybe<>(");
      trueValue.writeJava(sb, environment);
      sb.append(")");
    } else {
      trueValue.writeJava(sb, environment);
    }
    sb.append(" : ");
    if (wrapInstruction == WrapInstruction.WrapBWithMaybe) {
      sb.append("new NtMaybe<>(");
      falseValue.writeJava(sb, environment);
      sb.append(")");
    } else {
      falseValue.writeJava(sb, environment);
    }
    sb.append(")");
  }

  protected TyType typingReal(final Environment environment, final TyType suggestion, final boolean commit) {
    final var conditionType = condition.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
    environment.rules.IsBoolean(conditionType, false);
    TyType trueType;
    TyType falseType;
    if (trueValue instanceof SupportsTwoPhaseTyping) {
      trueType = ((SupportsTwoPhaseTyping) trueValue).estimateType(environment);
    } else {
      trueType = trueValue.typing(environment, suggestion);
    }
    if (falseValue instanceof SupportsTwoPhaseTyping) {
      falseType = ((SupportsTwoPhaseTyping) falseValue).estimateType(environment);
    } else {
      falseType = falseValue.typing(environment, suggestion);
    }
    wrapInstruction = environment.rules.GetMaxTypeBasedWrappingInstruction(trueType, falseType);
    var result = environment.rules.GetMaxType(trueType, falseType, false);
    if (commit) {
      result = environment.rules.EnsureRegisteredAndDedupe(result, false);
      if (trueValue instanceof SupportsTwoPhaseTyping) {
        ((SupportsTwoPhaseTyping) trueValue).upgradeType(environment, result);
      }
      if (falseValue instanceof SupportsTwoPhaseTyping) {
        ((SupportsTwoPhaseTyping) falseValue).upgradeType(environment, result);
      }
      return result;
    }
    return result;
  }

  @Override
  public TyType estimateType(final Environment environment) {
    return typingReal(environment, null, false);
  }

  @Override
  public void upgradeType(final Environment environment, final TyType newType) {
    cachedType = newType;
    if (trueValue instanceof SupportsTwoPhaseTyping) {
      ((SupportsTwoPhaseTyping) trueValue).upgradeType(environment, newType);
    }
    if (falseValue instanceof SupportsTwoPhaseTyping) {
      ((SupportsTwoPhaseTyping) falseValue).upgradeType(environment, newType);
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    condition.free(environment);
    trueValue.free(environment);
    falseValue.free(environment);
  }
}
