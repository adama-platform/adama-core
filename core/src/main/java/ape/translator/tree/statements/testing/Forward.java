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
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeDouble;

import java.util.function.Consumer;

/** march forward time (in seconds) */
public class Forward extends Statement {
  public final Token token;
  public final Expression expression;
  public final Token semicolonToken;

  public Forward(final Token token, final Expression expression, final Token semicolonToken) {
    this.token = token;
    this.expression = expression;
    this.semicolonToken = semicolonToken;
    ingest(token);
    ingest(semicolonToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(token);
    expression.emit(yielder);
    yielder.accept(semicolonToken);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }

  @Override
  public ControlFlow typing(final Environment prior) {
    Environment environment = prior.scopeWithComputeContext(ComputeContext.Computation);
    if (!environment.state.isTesting()) {
      environment.document.createError(this, String.format("Forward is exclusively for testing"));
    }
    environment.rules.IsNumeric(expression.typing(environment, new TyNativeDouble(TypeBehavior.ReadOnlyNativeValue, null, null)), false);
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("__forward(");
    expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(");");
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
  }
}
