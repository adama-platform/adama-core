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

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TyType;

import java.util.function.Consumer;

public class DefaultCase extends Statement {
  public final Token token;
  public final Token colon;

  public DefaultCase(Token token, Token colon) {
    this.token = token;
    this.colon = colon;
    ingest(token);
    ingest(colon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(token);
    yielder.accept(colon);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public ControlFlow typing(Environment environment) {
    TyType caseType = environment.getCaseType();
    if (caseType == null) {
      environment.document.createError(this, String.format("default: requires being in a switch statement"));
    }
    if (environment.checkDefaultReturnTrueIfMultiple()) {
      environment.document.createError(this, String.format("there can be only one default case"));
    }
    return ControlFlow.Open;
  }

  @Override
  public void free(FreeEnvironment environment) {
  }

  @Override
  public void writeJava(StringBuilderWithTabs sb, Environment environment) {
    sb.append("default:");
  }
}
