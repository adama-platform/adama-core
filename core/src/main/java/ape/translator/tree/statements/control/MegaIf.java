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
import ape.translator.parser.WhiteSpaceNormalizeTokenDocumentHandler;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.ArrayList;
import java.util.function.Consumer;

/** classical if statement with else if and else */
public class MegaIf extends Statement {
  public final ArrayList<If> branches;
  public Block elseBranch;
  public Token elseToken;

  public MegaIf(final Token ifToken, final Condition condition, final Block code) {
    branches = new ArrayList<>();
    branches.add(new If(ifToken, null, condition, code));
    ingest(ifToken);
    ingest(condition);
    ingest(code);
  }

  /** add an 'else if' branch */
  public void add(final Token elseToken, final Token ifToken, final Condition condition, final Block code) {
    branches.add(new If(elseToken, ifToken, condition, code));
    ingest(condition);
    ingest(code);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    for (final If branch : branches) {
      branch.emit(yielder);
    }
    if (elseToken != null) {
      yielder.accept(elseToken);
      elseBranch.emit(yielder);
    }
  }

  @Override
  public void format(Formatter formatter) {
    int n = branches.size();
    for (int k = 0; k < branches.size(); k++) {
      If branch = branches.get(k);
      branch.format(formatter, elseToken != null || k < n - 1);
    }
    if (elseToken != null) {
      elseBranch.format(formatter);
    }
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    var flow = ControlFlow.Returns;
    for (final If branch : branches) {
      if (branch.condition.name != null) {
        final var compute = environment.scopeWithComputeContext(ComputeContext.Computation);
        branch.condition.maybeType = branch.condition.expression.typing(compute, null /* no suggestion */);
        if (environment.rules.IsMaybe(branch.condition.maybeType, false)) {
          branch.condition.elementType = ((DetailContainsAnEmbeddedType) branch.condition.maybeType).getEmbeddedType(compute);
          if (branch.condition.elementType != null) {
            compute.define(branch.condition.name, branch.condition.elementType, false, branch.condition.elementType);
          }
        } else {
          branch.condition.maybeType = null;
        }
        if (branch.code.typing(compute.scope()) == ControlFlow.Open) {
          flow = ControlFlow.Open;
        }
      } else {
        final var expressionType = branch.condition.expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null /* nope */);
        if (environment.rules.IsMaybe(expressionType, true)) {
          final var compute = environment.scopeWithComputeContext(ComputeContext.Computation);
          final var subExpressionType = ((DetailContainsAnEmbeddedType) expressionType).getEmbeddedType(compute);
          environment.rules.IsBoolean(subExpressionType, false);
          branch.condition.extractBooleanMaybe = true;
        } else {
          environment.rules.IsBoolean(expressionType, false);
        }
        if (branch.code.typing(environment.scope()) == ControlFlow.Open) {
          flow = ControlFlow.Open;
        }
      }
    }
    if (elseBranch != null) {
      if (elseBranch.typing(environment.scope()) == ControlFlow.Open) {
        flow = ControlFlow.Open;
      }
    } else {
      flow = ControlFlow.Open;
    }
    return flow;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    var first = true;
    for (final If branch : branches) {
      if (branch.condition.name != null && branch.condition.maybeType != null) {
        branch.condition.generatedVariable = "_AutoCondition" + branch.condition.name + "_" + environment.autoVariable();
        sb.append(branch.condition.maybeType.getJavaConcreteType(environment)).append(" ").append(branch.condition.generatedVariable).append(";").writeNewline();
      }
    }
    for (final If branch : branches) {
      if (first) {
        sb.append("if (");
        first = false;
      } else {
        sb.append(" else if (");
      }
      if (branch.condition.name != null && branch.condition.maybeType != null) {
        sb.append("(").append(branch.condition.generatedVariable).append(" = ");
        branch.condition.expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
        sb.append(").has()) {").tabUp().writeNewline();
        sb.append(branch.condition.elementType.getJavaConcreteType(environment)).append(" ").append(branch.condition.name).append(" = ").append(branch.condition.generatedVariable).append(".get();").writeNewline();
        final var next = environment.scope();
        next.define(branch.condition.name, branch.condition.elementType, false, branch.condition.elementType);
        branch.code.specialWriteJava(sb, next, false, true);
        sb.append("}");
      } else if (branch.condition.extractBooleanMaybe) {
        sb.append("LibMath.isTrue(");
        branch.condition.expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
        sb.append(")) ");
        branch.code.writeJava(sb, environment.scope());
      } else {
        branch.condition.expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
        sb.append(") ");
        branch.code.writeJava(sb, environment.scope());
      }
    }
    if (elseBranch != null) {
      sb.append(" else ");
      elseBranch.writeJava(sb, environment.scope());
    }
  }

  /** add the else branch */
  public void setElse(final Token elseToken, final Block elseBranch) {
    this.elseToken = elseToken;
    this.elseBranch = elseBranch;
    ingest(elseBranch);
  }

  public static class Condition extends DocumentPosition {
    public final Token asToken;
    public final Token closeParen;
    public final Expression expression;
    public final String name;
    public final Token nameToken;
    public final Token openParen;
    private TyType elementType;
    private String generatedVariable;
    private TyType maybeType;
    public boolean extractBooleanMaybe;

    public Condition(final Token openParen, final Expression expression, final Token asToken, final Token nameToken, final Token closeParen) {
      this.openParen = openParen;
      this.expression = expression;
      this.asToken = asToken;
      this.nameToken = nameToken;
      if (nameToken != null) {
        name = nameToken.text;
      } else {
        name = null;
      }
      this.closeParen = closeParen;
      this.maybeType = null;
      this.extractBooleanMaybe = false;
      ingest(openParen);
      ingest(closeParen);
    }

    public void emit(final Consumer<Token> yielder) {
      yielder.accept(openParen);
      expression.emit(yielder);
      if (asToken != null) {
        yielder.accept(asToken);
        yielder.accept(nameToken);
      }
      yielder.accept(closeParen);
    }

    public void format(Formatter formatter) {
      expression.format(formatter);
    }
  }

  public static class If extends DocumentPosition {
    public final Block code;
    public final Condition condition;
    public final Token elseToken;
    public final Token ifToken;

    public If(final Token elseToken, final Token ifToken, final Condition condition, final Block code) {
      this.elseToken = elseToken;
      this.ifToken = ifToken;
      this.condition = condition;
      this.code = code;
      if (elseToken != null) {
        ingest(elseToken);
      }
      if (ifToken != null) {
        ingest(ifToken);
      }
      ingest(code);
    }

    public void emit(final Consumer<Token> yielder) {
      if (elseToken != null) {
        yielder.accept(elseToken);
      }
      if (ifToken != null) {
        yielder.accept(ifToken);
      }
      condition.emit(yielder);
      code.emit(yielder);
    }

    public void format(Formatter formatter, boolean hasTraiingElse) {
      condition.format(formatter);
      code.format(formatter);
      Formatter.FirstAndLastToken fal = new Formatter.FirstAndLastToken();
      code.emit(fal);
      WhiteSpaceNormalizeTokenDocumentHandler.remove(fal.last);
      if (fal.last != null && !hasTraiingElse) {
        formatter.endLine(fal.last);
      }
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    for (final If branch : branches) {
      branch.condition.expression.free(environment);
      branch.code.free(environment.push());
    }
    if (elseBranch != null) {
      elseBranch.free(environment.push());
    }
  }
}
