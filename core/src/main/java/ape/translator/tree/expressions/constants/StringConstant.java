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
import ape.translator.tree.types.natives.TyNativeString;

import java.util.function.Consumer;

/** utf-8 strings constant ("ninja") */
public class StringConstant extends Expression {
  public final Token token;

  /** @param token the token containing the value along with prior/after */
  public StringConstant(final Token token) {
    this.token = token;
    ingest(token);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(token);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    environment.interns.add(token.text);
    environment.mustBeComputeContext(this);
    return new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, token).withPosition(this);
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    sb.append(token.text);
  }

  @Override
  public void free(FreeEnvironment environment) {
  }
}