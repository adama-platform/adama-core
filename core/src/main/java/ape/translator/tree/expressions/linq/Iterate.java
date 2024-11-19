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
import ape.translator.tree.types.natives.TyNativeList;

import java.util.function.Consumer;

/** begin a linq query to convert a table into a list which may be filtered, ordered, limited */
public class Iterate extends LinqExpression {
  public final Expression expression;
  public final Token iterateToken;

  public Iterate(final Token iterateToken, final Expression expression) {
    super(null);
    this.iterateToken = iterateToken;
    this.expression = expression;
    ingest(iterateToken);
    ingest(expression);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(iterateToken);
    expression.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    final var exprType = expression.typing(environment, null /* no suggestion makes sense */);
    if (exprType != null && environment.rules.IsTable(exprType, false)) {
      final var recordType = environment.rules.ExtractEmbeddedType(exprType, false);
      if (recordType != null) {
        return TyNativeList.WRAP(recordType).withPosition(this);
      }
    }
    return null;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    expression.writeJava(sb, environment);
    sb.append(".iterate(").append(intermediateExpression ? "false" : "true").append(")");
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
  }
}
