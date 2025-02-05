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
package ape.translator.tree.expressions.linq;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeString;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.function.Consumer;

/** order the given sql expression result by a string containing dynamic compare instructions */
public class OrderDyn extends LinqExpression {
  public final Token dynOrderToken;
  private TyReactiveRecord elementType;
  private final Expression expr;

  public OrderDyn(final Expression sql, final Token dynOrderToken, final Expression expr) {
    super(sql);
    this.dynOrderToken = dynOrderToken;
    this.expr = expr;
    ingest(sql);
    ingest(expr);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    sql.emit(yielder);
    yielder.accept(dynOrderToken);
    expr.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    sql.format(formatter);
    expr.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    TyType base = sql.typing(environment, suggestion);
    TyType str = expr.typing(environment, new TyNativeString(TypeBehavior.ReadWriteWithSetGet, null, dynOrderToken));
    environment.rules.IsString(str, false);
    if (environment.rules.IsNativeListOfStructure(base, false)) {
      TyType embedType = ((DetailContainsAnEmbeddedType) base).getEmbeddedType(environment);
      if (embedType instanceof TyReactiveRecord) {
        elementType = (TyReactiveRecord) embedType;
      } else {
        environment.document.createError(this, "order_dyn requires the list to contain reactive records");
      }
      return base;
    }
    return null;
  }

  @Override
  public void free(FreeEnvironment environment) {
    sql.free(environment);
    expr.free(environment);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    sql.writeJava(sb, environment);
    sb.append(".orderBy(").append(intermediateExpression ? "false" : "true").append(",new DynCmp_RTx").append(elementType.name).append("(");
    expr.writeJava(sb, environment);
    sb.append("))");
  }
}
