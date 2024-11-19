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
package ape.translator.tree.expressions.linq;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;

import java.util.function.Consumer;

public class Shuffle extends LinqExpression {
  public final Token shuffleToken;

  public Shuffle(final Token shuffleToken, final Expression sql) {
    super(sql);
    this.shuffleToken = shuffleToken;
    ingest(shuffleToken);
    ingest(sql);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    sql.emit(yielder);
    yielder.accept(shuffleToken);
  }

  @Override
  public void format(Formatter formatter) {
    sql.format(formatter);
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    final var type = sql.typing(environment, null);
    if (environment.rules.IsNativeListOfStructure(type, false)) {
      return type;
    }
    return null;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    sql.writeJava(sb, environment);
    sb.append(".shuffle(").append(intermediateExpression ? "false, " : "true, ").append("__random)");
  }

  @Override
  public void free(FreeEnvironment environment) {
    sql.free(environment);
  }
}
