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
