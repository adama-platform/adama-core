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

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeLong;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.function.Consumer;

/** define a metric to emit a numeric value periodically */
public class DefineMetric extends Definition {
  public final Token metricToken;
  public final Token nameToken;
  public final Token equalsToken;
  public final Expression expression;
  public final Token semicolonToken;
  public TyType metricType;

  public DefineMetric(Token metricToken, Token nameToken, Token equalsToken, Expression expression, Token semicolonToken) {
    this.metricToken = metricToken;
    this.nameToken = nameToken;
    this.equalsToken = equalsToken;
    this.expression = expression;
    this.semicolonToken = semicolonToken;
    this.metricType = null;
    ingest(semicolonToken);
    ingest(metricToken);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(metricToken);
    yielder.accept(nameToken);
    yielder.accept(equalsToken);
    expression.emit(yielder);
    yielder.accept(semicolonToken);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(metricToken);
    expression.format(formatter);
    formatter.endLine(semicolonToken);
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    expression.free(fe);
    checker.register(fe.free, (environment) -> {
      Environment next = environment.scopeWithComputeContext(ComputeContext.Computation).scopeAsReadOnlyBoundary();
      metricType = expression.typing(next, new TyNativeLong(TypeBehavior.ReadOnlyNativeValue, null, null).withPosition(this));
      // we only support numeric types (for now))
      boolean good = environment.rules.IsLong(metricType, true) || environment.rules.IsNumeric(metricType, true);
      if (!good && metricType != null) {
        environment.document.createError(this, String.format("Type check failure: must have a type of int, long, or double; instead, but the type is actually '%s'", metricType.getAdamaType()));
      }
    });
  }
}
