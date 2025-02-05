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
package ape.translator.tree.expressions.operators;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeBoolean;
import ape.translator.tree.types.natives.TyNativeDouble;

import java.util.function.Consumer;

/** provides 1 <= x <= 3 for numeric types */
public class RangeOperator extends Expression {
  public final Expression low;
  public final Token opLow;
  public final Expression squeeze;
  public final Token opHi;
  public final Expression high;

  public RangeOperator(Expression low, Token opLow, Expression squeeze, Token opHi, Expression high) {
    this.low = low;
    this.opLow = opLow;
    this.squeeze = squeeze;
    this.opHi = opHi;
    this.high = high;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    low.emit(yielder);
    yielder.accept(opLow);
    squeeze.emit(yielder);
    yielder.accept(opHi);
    high.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    low.format(formatter);
    squeeze.format(formatter);
    high.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    TyType lowTy = low.typing(environment, new TyNativeDouble(TypeBehavior.ReadOnlyNativeValue, null, null));
    TyType sqeeuzeTy = squeeze.typing(environment, new TyNativeDouble(TypeBehavior.ReadOnlyNativeValue, null, null));
    TyType highTy = high.typing(environment, new TyNativeDouble(TypeBehavior.ReadOnlyNativeValue, null, null));
    environment.rules.IsNumeric(lowTy, false);
    environment.rules.IsNumeric(sqeeuzeTy, false);
    environment.rules.IsNumeric(highTy, false);
    return new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, null);
  }

  @Override
  public void free(FreeEnvironment environment) {
    low.free(environment);
    squeeze.free(environment);
    high.free(environment);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    String a = opLow.isSymbolWithTextEq("<") ? "E" : "I";
    String b = opHi.isSymbolWithTextEq("<") ? "E" : "I";
    sb.append("LibMath.dRange").append(a).append(b).append("(");
    low.writeJava(sb, environment);
    sb.append(",");
    squeeze.writeJava(sb, environment);;
    sb.append(",");
    high.writeJava(sb, environment);
    sb.append(")");
  }
}
