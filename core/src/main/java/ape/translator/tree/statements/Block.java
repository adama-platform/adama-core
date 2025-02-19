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
package ape.translator.tree.statements;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.statements.control.AlterControlFlow;
import ape.translator.tree.statements.control.AlterControlFlowMode;
import ape.translator.tree.statements.control.Case;
import ape.translator.tree.statements.control.DefaultCase;

import java.util.ArrayList;
import java.util.function.Consumer;

/** {statements*} */
public class Block extends Statement {
  public final ArrayList<Statement> statements;
  private final Token openBraceToken;
  private Token closeBraceToken;

  public Block(final Token openBraceToken) {
    this.openBraceToken = openBraceToken;
    statements = new ArrayList<>();
    if (openBraceToken != null) {
      ingest(openBraceToken);
    }
  }

  public void add(final Statement statement) {
    if (statement != null) {
      statements.add(statement);
      ingest(statement);
    }
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    if (openBraceToken != null) {
      yielder.accept(openBraceToken);
    }
    for (final Statement statement : statements) {
      statement.emit(yielder);
    }
    if (closeBraceToken != null) {
      yielder.accept(closeBraceToken);
    }
  }

  @Override
  public void format(Formatter formatter) {
    if (openBraceToken != null) {
      formatter.endLine(openBraceToken);
      formatter.tabUp();
    }
    for (final Statement statement : statements) {
      Formatter.FirstAndLastToken fal = new Formatter.FirstAndLastToken();
      statement.emit(fal);
      if (fal.first != null) {
        formatter.startLine(fal.first);
        formatter.endLine(fal.last);
      }
      statement.format(formatter);
    }
    if (closeBraceToken != null) {
      formatter.tabDown();
      formatter.startLine(closeBraceToken);
      formatter.endLine(closeBraceToken);
    }
  }

  private boolean isBreak(Statement stmt ) {
    if (stmt instanceof AlterControlFlow) {
      return ((AlterControlFlow) stmt).how == AlterControlFlowMode.Break;
    }
    return false;
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    boolean hasCases = environment.getCaseType() != null;
    if (hasCases) {
      int casesThatBreak = 0;
      int casesThatReturn = 0;
      boolean inDefault = false;
      boolean hasDefault = false;
      boolean defaultReturns = false;
      boolean detectDeadCode = false;
      for (final Statement stmt : statements) {
        if (stmt instanceof Case || stmt instanceof DefaultCase) {
          inDefault = stmt instanceof DefaultCase;
          if (inDefault) {
            if (hasDefault) {
              environment.document.createError(stmt, String.format("Switch has too many defaults."));
            }
            hasDefault = true;
          }
          detectDeadCode = false;
        }
        if (detectDeadCode) {
          environment.document.createError(stmt, String.format("This code is unreachable."));
        }
        ControlFlow flow = stmt.typing(environment);
        if (flow == ControlFlow.Returns) {
          casesThatReturn++;
          if (inDefault) {
            defaultReturns = true;
          }
          detectDeadCode = true;
        }
        if (isBreak(stmt)) {
          casesThatBreak++;
        }
      }
      /*
       * if any case breaks, then it is open
       * if there are cases that return, then we really need a default that returns to indicate a returning control flow
       */
      if (casesThatBreak == 0 && casesThatReturn > 0 && hasDefault && defaultReturns) {
        return ControlFlow.Returns;
      } else {
        return ControlFlow.Open;
      }
    } else {
      var flow = ControlFlow.Open;
      for (final Statement stmt : statements) {
        // check that it must be Open
        if (flow == ControlFlow.Returns) {
          environment.document.createError(stmt, String.format("This code is unreachable."));
        }
        if (stmt.typing(environment) == ControlFlow.Returns) {
          flow = ControlFlow.Returns;
        }
      }
      return flow;
    }
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    specialWriteJava(sb, environment, true, true);
  }

  public void specialWriteJava(final StringBuilderWithTabs sb, final Environment environment, final boolean brace, final boolean tabDownOnEnd) {
    boolean hasCases = environment.getCaseType() != null;
    final var child = environment.scope();
    final var n = statements.size();
    if (n == 0 && brace) {
      sb.append("{}");
      return;
    }
    if (brace) {
      sb.tabUp().append("{").writeNewline();
      if (!environment.state.hasNoCost() && !environment.state.isStatic() && !hasCases) {
        sb.append(String.format("__code_cost += %d;", 1 + statements.size()));
        if (n == 0 && (brace || tabDownOnEnd)) {
          sb.tabDown();
        }
        sb.writeNewline();
      } else {
        if (n == 0 && (brace || tabDownOnEnd)) {
          sb.tabDown().append("/* empty */").writeNewline();
        }
      }
    } else if (!environment.state.hasNoCost() && !environment.state.isStatic() && !hasCases) {
      sb.append(String.format("__code_cost += %d;", 1 + statements.size()));
      if (n == 0 && (brace || tabDownOnEnd)) {
        sb.tabDown();
      }
      sb.writeNewline();
    } else {
      if (n == 0 && (brace || tabDownOnEnd)) {
        sb.tabDown().append("/* empty */").writeNewline();
      }
    }

    for (var k = 0; k < n; k++) {
      final var s = statements.get(k);
      final var codeCoverageIndex = environment.codeCoverageTracker.register(s);
      if (environment.state.options.produceCodeCoverage && !environment.state.isStatic() && !hasCases) {
        sb.append(String.format("__track(%d);", codeCoverageIndex)).writeNewline();
      }
      boolean tabbed = hasCases && !(s instanceof Case || s instanceof DefaultCase);
      if (tabbed) {
        sb.tab();
        sb.tabUp();
      }
      s.writeJava(sb, child);
      if (k == n - 1 && (brace || tabDownOnEnd)) {
        sb.tabDown();
      }
      if (tabbed) {
        sb.tabDown();
      }
      sb.writeNewline();
    }
    if (brace) {
      sb.append("}");
    }
  }

  public void end(final Token closeBraceToken) {
    this.closeBraceToken = closeBraceToken;
    ingest(closeBraceToken);
  }

  @Override
  public void free(FreeEnvironment environment) {
    FreeEnvironment next = environment.push();
    for (Statement stmt : statements) {
      stmt.free(next);
    }
  }
}
