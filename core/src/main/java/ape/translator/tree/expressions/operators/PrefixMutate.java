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
import ape.translator.tree.operands.PrefixMutateOp;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.CanBumpResult;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;

import java.util.function.Consumer;

/** prefix mutation ($e--, $e++) and prefix change (!, -) */
public class PrefixMutate extends Expression {
  public final Expression expression;
  public final PrefixMutateOp op;
  public final Token opToken;
  private final boolean addGet;
  private CanBumpResult bumpResult;

  public PrefixMutate(final Expression expression, final Token opToken) {
    this.expression = expression;
    this.opToken = opToken;
    op = PrefixMutateOp.fromText(opToken.text);
    ingest(opToken);
    ingest(expression);
    addGet = false;
    bumpResult = CanBumpResult.No;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(opToken);
    expression.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    final var newContext = op.requiresAssignment ? ComputeContext.Assignment : ComputeContext.Computation;
    TyType result = null;
    if (op == PrefixMutateOp.BumpUp || op == PrefixMutateOp.BumpDown) {
      result = expression.typing(environment.scopeWithComputeContext(newContext), null);
      bumpResult = environment.rules.CanBumpNumeric(result, false);
    } else if (op == PrefixMutateOp.NegateNumber) {
      result = expression.typing(environment.scopeWithComputeContext(newContext), null);
      bumpResult = environment.rules.CanBumpNumeric(result, false);
    } else if (op == PrefixMutateOp.NegateBool) {
      result = expression.typing(environment.scopeWithComputeContext(newContext), null);
      bumpResult = environment.rules.CanBumpBool(result, false);
    }
    if (bumpResult == CanBumpResult.No) {
      return null;
    }
    if (result instanceof DetailComputeRequiresGet && bumpResult.reactive) {
      return ((DetailComputeRequiresGet) result).typeAfterGet(environment).makeCopyWithNewPosition(this, result.behavior);
    }
    return result.makeCopyWithNewPosition(this, result.behavior);
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    final var newContext = op.requiresAssignment ? ComputeContext.Assignment : ComputeContext.Computation;
    switch (bumpResult) {
      case YesWithNative:
        sb.append(op.javaOp);
        expression.writeJava(sb, environment.scopeWithComputeContext(newContext));
        break;
      case YesWithSetter:
        expression.writeJava(sb, environment.scopeWithComputeContext(newContext));
        sb.append(op.functionCall);
        break;
      case YesWithListTransformSetter:
        expression.writeJava(sb, environment.scopeWithComputeContext(newContext));
        sb.append(".transform((item) -> item").append(op.functionCall).append(")");
        break;
      case YesWithListTransformNative:
        expression.writeJava(sb, environment.scopeWithComputeContext(newContext));
        sb.append(".transform((item) -> ").append(op.javaOp).append("item)");
        break;
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
  }
}
