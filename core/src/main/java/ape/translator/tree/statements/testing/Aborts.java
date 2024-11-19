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
