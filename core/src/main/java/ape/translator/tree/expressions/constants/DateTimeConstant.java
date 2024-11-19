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
package ape.translator.tree.expressions.constants;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeDateTime;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

/** a date and a time with the time zone in the typical gregorian calendar as a constant within source */
public class DateTimeConstant extends Expression {
  public final Token[] tokens;
  public final ZonedDateTime dateTime;

  public DateTimeConstant(ZonedDateTime dateTime, Token... tokens) {
    this.dateTime = dateTime;
    this.tokens = tokens;
    for (Token token : tokens) {
      ingest(token);
    }
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    for (Token token : tokens) {
      yielder.accept(token);
    }
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    environment.mustBeComputeContext(this);
    return new TyNativeDateTime(TypeBehavior.ReadOnlyNativeValue, null, tokens[0]).withPosition(this);
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    sb.append("new NtDateTime(ZonedDateTime.parse(\"").append(dateTime.toString()).append("\"))");
  }

  @Override
  public void free(FreeEnvironment environment) {
  }
}
