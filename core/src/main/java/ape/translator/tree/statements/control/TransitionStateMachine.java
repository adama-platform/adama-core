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
package ape.translator.tree.statements.control;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;

import java.util.function.Consumer;

/** transition the state machine, and make sure we transact the current state */
public class TransitionStateMachine extends Statement {
  public final Token inToken;
  public final Expression next;
  public final Token semicolonToken;
  public final Token transitionToken;
  private final Expression evaluateIn;

  public TransitionStateMachine(final Token transitionToken, final Expression next, final Token inToken, final Expression evaluateIn, final Token semicolonToken) {
    this.transitionToken = transitionToken;
    ingest(transitionToken);
    this.next = next;
    ingest(next);
    this.inToken = inToken;
    this.evaluateIn = evaluateIn;
    this.semicolonToken = semicolonToken;
    ingest(semicolonToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(transitionToken);
    next.emit(yielder);
    if (inToken != null) {
      yielder.accept(inToken);
      evaluateIn.emit(yielder);
    }
    yielder.accept(semicolonToken);
  }

  @Override
  public void format(Formatter formatter) {
    next.format(formatter);
    if (inToken != null) {
      evaluateIn.format(formatter);
    }
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    final var scoped = environment.scopeWithComputeContext(ComputeContext.Computation);
    final var nextType = next.typing(scoped, null);
    scoped.rules.IsStateMachineRef(nextType, false);
    if (evaluateIn != null) {
      final var evaluateInType = evaluateIn.typing(scoped, null);
      scoped.rules.IsNumeric(evaluateInType, false);
    }
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    final var scoped = environment.scopeWithComputeContext(ComputeContext.Computation);
    sb.append("__transitionStateMachine(");
    next.writeJava(sb, scoped);
    if (evaluateIn != null) {
      sb.append(", ");
      evaluateIn.writeJava(sb, scoped);
      sb.append(");");
    } else {
      sb.append(", 0);");
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    next.free(environment);
    if (evaluateIn != null) {
      evaluateIn.free(environment);
    }
  }
}
