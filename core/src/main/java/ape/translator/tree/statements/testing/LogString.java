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
package ape.translator.tree.statements.testing;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;

import java.util.function.Consumer;

/**
 * Logs a string to the test builder report
 */
public class LogString extends Statement {
  public final Token logToken;
  public final Expression expression;
  public final Token semiColonToken;

  public LogString(final Token logToken, final Expression expression, final Token semiColonToken) {
    this.logToken = logToken;
    this.expression = expression;
    this.semiColonToken = semiColonToken;
    ingest(logToken);
    ingest(expression);
    ingest(semiColonToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(logToken);
    expression.emit(yielder);
    yielder.accept(semiColonToken);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    if (!environment.state.isTesting()) {
      environment.document.createError(this, "logs are only applicable within a test.");
    }
    final var expressionType = expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
    environment.rules.IsString(expressionType, false);
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("__report.log(");
    expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(toArgs(false));
    sb.append(");");
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
  }
}
