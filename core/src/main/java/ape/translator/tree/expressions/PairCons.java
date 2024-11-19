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
package ape.translator.tree.expressions;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativePair;

import java.util.function.Consumer;

public class PairCons extends Expression {

  public final Token pairIntro;
  public final Expression key;
  public final Token arrow;
  public final Expression value;

  public PairCons(Token pairIntro, Expression key, Token arrow, Expression value) {
    this.pairIntro = pairIntro;
    this.key = key;
    this.arrow = arrow;
    this.value = value;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(pairIntro);
    key.emit(yielder);
    yielder.accept(arrow);
    value.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    key.format(formatter);
    value.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    TyType keyType = null;
    TyType valueType = null;
    if (suggestion instanceof TyNativePair) {
      keyType = ((TyNativePair) suggestion).domainType;
      valueType = ((TyNativePair) suggestion).rangeType;
    }
    keyType = key.typing(environment, keyType);
    valueType = value.typing(environment, valueType);
    return new TyNativePair(TypeBehavior.ReadOnlyNativeValue, null, pairIntro, null, keyType, null, valueType, arrow);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    sb.append("new NtPair<>(");
    key.writeJava(sb, environment);
    sb.append(",");
    value.writeJava(sb, environment);
    sb.append(")");
  }

  @Override
  public void free(FreeEnvironment environment) {
    key.free(environment);
    value.free(environment);
  }
}
