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
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.ruleset.RuleSetEnums;

import java.util.function.Consumer;

public class Switch extends Statement {
  public final Token token;
  public final Token openParen;
  public final Expression expression;
  public final Token closeParen;
  public final Block code;
  public TyType caseType;

  public Switch(Token token, Token openParen, Expression expression, Token closeParen, Block code) {
    this.token = token;
    this.openParen = openParen;
    this.expression = expression;
    this.closeParen = closeParen;
    this.code = code;
    ingest(token);
    ingest(code);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(token);
    yielder.accept(openParen);
    expression.emit(yielder);
    yielder.accept(closeParen);
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
    code.format(formatter);
  }

  @Override
  public ControlFlow typing(Environment environment) {
    Environment next = environment.scope();
    caseType = expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
    boolean good = environment.rules.IsInteger(caseType, true) || environment.rules.IsString(caseType, true) || RuleSetEnums.IsEnum(environment, caseType, true);
    if (!good) {
      environment.document.createError(this, String.format("switch statements work with integer, string, or enum types"));
    }
    next.setCaseType(caseType);
    return code.typing(next);
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
    code.free(environment);
  }

  @Override
  public void writeJava(StringBuilderWithTabs sb, Environment environment) {
    sb.append("switch (");
    expression.writeJava(sb, environment);
    sb.append(") ");
    TyType priorCaseType = environment.getCaseType();
    environment.setCaseType(caseType);
    code.writeJava(sb, environment);
    environment.setCaseType(priorCaseType);
  }
}
