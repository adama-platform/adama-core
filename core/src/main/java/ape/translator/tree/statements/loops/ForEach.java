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
package ape.translator.tree.statements.loops;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.function.Consumer;

/** a modern and safe foreach(V in EXPR) code */
public class ForEach extends Statement {
  public final Block code;
  public final Token endParen;
  public final Token foreachToken;
  public final Token inToken;
  public final Expression iterable;
  public final Token openParen;
  public final String variable;
  public final Token variableToken;
  private TyType elementType;

  public ForEach(final Token foreachToken, final Token openParen, final Token variableToken, final Token inToken, final Expression iterable, final Token endParen, final Block code) {
    this.foreachToken = foreachToken;
    this.openParen = openParen;
    variable = variableToken.text;
    this.variableToken = variableToken;
    this.inToken = inToken;
    this.iterable = iterable;
    this.endParen = endParen;
    this.code = code;
    elementType = null;
    ingest(foreachToken);
    ingest(iterable);
    ingest(code);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(foreachToken);
    yielder.accept(openParen);
    yielder.accept(variableToken);
    yielder.accept(inToken);
    iterable.emit(yielder);
    yielder.accept(endParen);
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    iterable.format(formatter);
    code.format(formatter);
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    if (environment.defined(variable)) {
      environment.document.createError(this, String.format("The variable '" + variable + "' is already defined"));
    }
    final var type = iterable.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null /* we know nothing to suggest */);
    if (type != null) {
        if (environment.rules.IsIterable(type, false)) {
          elementType = ((DetailContainsAnEmbeddedType) type).getEmbeddedType(environment);
        }
      if (elementType != null) {
        final var next = environment.scopeWithComputeContext(ComputeContext.Computation);
        next.define(variable, elementType, false, elementType);
        code.typing(next);
      }
    }
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    if (elementType != null) {
      sb.append("for(").append(elementType.getJavaBoxType(environment)).append(" ").append(variable).append(" : ");
      iterable.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(") ");
      final var next = environment.scopeWithComputeContext(ComputeContext.Computation);
      next.define(variable, elementType, false, elementType);
      code.writeJava(sb, next);
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    iterable.free(environment);
    code.free(environment.push());
  }
}
