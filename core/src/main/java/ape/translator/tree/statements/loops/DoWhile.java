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

import java.util.function.Consumer;

/** classic do {} while (cond); loop */
public class DoWhile extends Statement {
  public final Token closeParen;
  public final Block code;
  public final Expression condition;
  public final Token doToken;
  public final Token endToken;
  public final Token openParen;
  public final Token whileToken;

  public DoWhile(final Token doToken, final Block code, final Token whileToken, final Token openParen, final Expression condition, final Token closeParen, final Token endToken) {
    this.doToken = doToken;
    this.code = code;
    this.whileToken = whileToken;
    this.openParen = openParen;
    this.condition = condition;
    this.closeParen = closeParen;
    this.endToken = endToken;
    ingest(doToken);
    ingest(endToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(doToken);
    code.emit(yielder);
    yielder.accept(whileToken);
    yielder.accept(openParen);
    condition.emit(yielder);
    yielder.accept(closeParen);
    yielder.accept(endToken);
  }

  @Override
  public void format(Formatter formatter) {
    code.format(formatter);
    condition.format(formatter);
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    final var flow = code.typing(environment.scope());
    final var conditionType = condition.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
    environment.rules.IsBoolean(conditionType, false);
    return flow;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("do ");
    code.writeJava(sb, environment.scope());
    if (environment.state.isStatic()) {
      sb.append(" while (__static_state.__goodwill(").append(condition.toArgs(true)).append(") && (");
    } else {
      sb.append(" while (__goodwill(").append(condition.toArgs(true)).append(") && (");
    }
    condition.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append("));");
  }

  @Override
  public void free(FreeEnvironment environment) {
    condition.free(environment);
    code.free(environment.push());
  }
}
