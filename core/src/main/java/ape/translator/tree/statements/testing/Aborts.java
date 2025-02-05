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
package ape.translator.tree.statements.testing;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;

import java.util.function.Consumer;

public class Aborts extends Statement {
  private final Token aborts;
  private final Block code;

  public Aborts(Token aborts, Block code) {
    this.aborts = aborts;
    this.code = code;
    ingest(aborts);
    ingest(code);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(aborts);
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    code.format(formatter);
  }

  @Override
  public ControlFlow typing(Environment environment) {
    if (!environment.state.isTesting()) {
      environment.document.createError(this, "@aborts is only applicable within tests");
    }
    return code.typing(environment.scopeAsAbortable());
  }

  @Override
  public void free(FreeEnvironment environment) {
    code.free(environment);
  }

  @Override
  public void writeJava(StringBuilderWithTabs sb, Environment environment) {
    String variable = "__aborts_" + environment.autoVariable();
    sb.append("boolean ").append(variable).append(" = false;").writeNewline();
    sb.append("try");
    code.writeJava(sb, environment);
    sb.append(" catch (AbortMessageException __ame) {").tabUp().writeNewline();
    sb.append(variable).append(" = true;").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("__assert_truth(").append(variable).append(toArgs(false)).append(");");
  }
}
