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

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;

import java.util.function.Consumer;

public class AlterControlFlow extends Statement {
  public final AlterControlFlowMode how;
  public final Token semicolonToken;
  public final Token token;

  public AlterControlFlow(final Token token, final AlterControlFlowMode how, final Token semicolonToken) {
    this.token = token;
    this.how = how;
    this.semicolonToken = semicolonToken;
    ingest(token);
    ingest(semicolonToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(token);
    yielder.accept(semicolonToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    if (how == AlterControlFlowMode.Abort && !(environment.state.isMessageHandler() || environment.state.isAbortable() || environment.state.isAuthorize() || environment.state.isTesting() )) {
      environment.document.createError(this, String.format("Can only 'abort' from a message handler or an abortable procedure/method"));
    }
    if (how == AlterControlFlowMode.Block && !environment.state.isStateMachineTransition()) {
      environment.document.createError(this, String.format("Can only 'block' from a state machine transition"));
    }
    if (how == AlterControlFlowMode.Abort || how == AlterControlFlowMode.Block) {
      return ControlFlow.Returns;
    }
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    if (how == AlterControlFlowMode.Break) {
      sb.append("break;");
    } else if (how == AlterControlFlowMode.Continue) {
      sb.append("continue;");
    } else if (how == AlterControlFlowMode.Abort) {
      sb.append("throw new AbortMessageException();");
    } else if (how == AlterControlFlowMode.Block) {
      sb.append("throw new ComputeBlockedException(null, null);");
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
  }
}
