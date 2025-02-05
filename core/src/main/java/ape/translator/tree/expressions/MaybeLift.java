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
