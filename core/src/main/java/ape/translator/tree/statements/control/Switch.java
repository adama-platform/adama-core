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
