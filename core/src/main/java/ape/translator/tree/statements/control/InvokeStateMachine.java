/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

/** invoke a state machine's state; useful for dynamic behavior */
public class InvokeStateMachine extends Statement {
  public final Expression expression;
  public final Token invokeToken;
  public final Token semicolonToken;

  public InvokeStateMachine(final Token invokeToken, final Expression expression, final Token semicolonToken) {
    this.invokeToken = invokeToken;
    this.expression = expression;
    this.semicolonToken = semicolonToken;
    ingest(invokeToken);
    ingest(expression);
    ingest(semicolonToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(invokeToken);
    expression.emit(yielder);
    yielder.accept(semicolonToken);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    final var expressionType = expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
    environment.rules.IsStateMachineRef(expressionType, false);
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("__invoke(");
    expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(");");
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
  }
}
