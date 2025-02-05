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
import ape.translator.tree.expressions.constants.EnumConstant;
import ape.translator.tree.expressions.constants.EnumValuesArray;
import ape.translator.tree.expressions.constants.IntegerConstant;
import ape.translator.tree.expressions.constants.StringConstant;
import ape.translator.tree.expressions.constants.*;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.ruleset.RuleSetEnums;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Case extends Statement {
  public final Token token;
  public final Expression value;
  public final Token colon;
  private ArrayList<Integer> enumArray;

  public Case(Token token, Expression value, Token colon) {
    this.token = token;
    this.value = value;
    this.colon = colon;
    this.enumArray = null;
    ingest(token);
    ingest(colon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(token);
    value.emit(yielder);
    yielder.accept(colon);
  }

  @Override
  public void format(Formatter formatter) {
    value.format(formatter);
  }

  @Override
  public ControlFlow typing(Environment environment) {
    value.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
    TyType caseType = environment.getCaseType();
    if (caseType == null) {
      environment.document.createError(this, String.format("case label should be within a switch statement"));
      return ControlFlow.Open;
    }
    if (environment.rules.IsInteger(caseType, true) && !(value instanceof IntegerConstant)) {
      environment.document.createError(this, String.format("case label should be an integer constant"));
    }
    if (environment.rules.IsString(caseType, true) && !(value instanceof StringConstant)) {
      environment.document.createError(this, String.format("case label should be an string constant"));
    }
    if (RuleSetEnums.IsEnum(environment, caseType, true)) {
      if (value instanceof EnumValuesArray) {
        enumArray = ((EnumValuesArray) value).values(environment);
        return ControlFlow.Open;
      } else if (value instanceof EnumConstant) {
        return ControlFlow.Open;
      }
      environment.document.createError(this, String.format("case label should be an enum constant or enum array reference"));
    }
    return ControlFlow.Open;
  }

  @Override
  public void free(FreeEnvironment environment) {
    value.free(environment);
  }

  @Override
  public void writeJava(StringBuilderWithTabs sb, Environment environment) {
    if (enumArray != null) {
      int countDown = enumArray.size();
      for (Integer val : enumArray) {
        countDown --;
        sb.append("case ").append("" + val).append(":");
        if (countDown > 0) {
          sb.writeNewline();
        }
      }
    } else {
      sb.append("case ");
      value.writeJava(sb, environment);
      sb.append(":");
    }
  }
}
