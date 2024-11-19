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

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeMaybe;

import java.util.function.Consumer;

public class MaybeLift extends Expression {
  public final Token closeParen;
  public final Token maybeToken;
  public final Token openParen;
  public final TokenizedItem<TyType> type;
  public final Expression value;

  public MaybeLift(final Token maybeToken, final TokenizedItem<TyType> type, final Token openParen, final Expression value, final Token closeParen) {
    this.maybeToken = maybeToken;
    this.type = type;
    this.openParen = openParen;
    this.value = value;
    this.closeParen = closeParen;
    ingest(maybeToken);
    if (type != null) {
      ingest(type.item);
    } else {
      ingest(closeParen);
    }
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(maybeToken);
    if (type != null) {
      type.emitBefore(yielder);
      type.item.emit(yielder);
      type.emitAfter(yielder);
    } else {
      yielder.accept(openParen);
      value.emit(yielder);
      yielder.accept(closeParen);
    }
  }

  @Override
  public void format(Formatter formatter) {
    if (type != null) {
      type.item.format(formatter);
    } else {
      value.format(formatter);
    }
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    environment.mustBeComputeContext(this);
    if (type != null) {
      return new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, maybeToken, type);
    } else {
      final var valueType = value.typing(environment, null);
      if (valueType != null) {
        return new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, maybeToken, new TokenizedItem<>(valueType));
      }
      return null;
    }
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    sb.append("new ");
    sb.append(environment.rules.Resolve(cachedType, true).getJavaBoxType(environment));
    sb.append("(");
    if (value != null) {
      value.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    }
    sb.append(")");
  }

  @Override
  public void free(FreeEnvironment environment) {
    if (value != null) {
      value.free(environment);
    }
  }
}
