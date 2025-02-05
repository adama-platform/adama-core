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

/** classical for(statement;condition;statement) block loop */
public class For extends Statement {
  public final Token endConditionSemicolon;
  public final Token endParen;
  public final Token forToken;
  public final Token noInitialSemicolon;
  public final Token openParen;
  public Statement advance;
  public Block code;
  public Expression condition;
  public Statement initial;

  public For(final Token forToken, final Token openParen, final Statement initial, final Token noInitialSemicolon, final Expression condition, final Token endConditionSemicolon, final Statement advance, final Token endParen, final Block code) {
    this.forToken = forToken;
    ingest(forToken);
    this.openParen = openParen;
    this.initial = initial;
    this.noInitialSemicolon = noInitialSemicolon;
    this.condition = condition;
    this.endConditionSemicolon = endConditionSemicolon;
    this.advance = advance;
    this.endParen = endParen;
    this.code = code;
    ingest(code);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(forToken);
    yielder.accept(openParen);
    if (initial != null) {
      initial.emit(yielder);
    } else {
      yielder.accept(noInitialSemicolon);
    }
    if (condition != null) {
      condition.emit(yielder);
    }
    yielder.accept(endConditionSemicolon);
    if (advance != null) {
      advance.emit(yielder);
    }
    yielder.accept(endParen);
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    if (initial != null) {
      initial.format(formatter);
    }
    if (condition != null) {
      condition.format(formatter);
    }
    if (advance != null) {
      advance.format(formatter);
    }
    code.format(formatter);
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    final var next = environment.scope();
    if (initial != null) {
      initial.typing(next);
    }
    if (condition != null) {
      final var conditionType = condition.typing(next.scopeWithComputeContext(ComputeContext.Computation), null);
      environment.rules.IsBoolean(conditionType, false);
    }
    if (advance != null) {
      advance.typing(next);
    }
    code.typing(next);
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    final var next = environment.scope();
    sb.append("{").tabUp().writeNewline();
    if (initial != null) {
      initial.writeJava(sb, next);
      sb.writeNewline();
    }
    sb.append("for (");
    sb.append(";");
    if (condition != null) {
      if (environment.state.isStatic()) {
        sb.append("__static_state.__goodwill(").append(condition.toArgs(true)).append(") && (");
      } else {
        sb.append("__goodwill(").append(condition.toArgs(true)).append(") && (");
      }
      condition.writeJava(sb, next.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(")");
    } else {
      if (environment.state.isStatic()) {
        sb.append("__static_state.__goodwill(").append(toArgs(true)).append(")");
      } else {
        sb.append("__goodwill(").append(toArgs(true)).append(")");
      }
    }
    sb.append(";");
    if (advance != null) {
      advance.writeJava(sb, next);
    }
    sb.append(") ");
    code.writeJava(sb, next);
    sb.tabDown().writeNewline().append("}");
  }

  @Override
  public void free(FreeEnvironment environment) {
    if (initial != null) {
      initial.free(environment);
    }
    if (condition != null) {
      condition.free(environment);
    }
    if (advance != null) {
      advance.free(environment);
    }
    code.free(environment.push());
  }
}
