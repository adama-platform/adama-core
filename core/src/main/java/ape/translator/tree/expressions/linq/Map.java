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
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetCommon;
import ape.translator.tree.types.checking.ruleset.RuleSetLists;
import ape.translator.tree.types.natives.TyNativeFunctional;
import ape.translator.tree.types.natives.TyNativeList;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;

import java.util.ArrayList;
import java.util.function.Consumer;

/** map a function over the elements in a list to produce a new list L' = f(L) */
public class Map extends LinqExpression {
  public final Token mapToken;
  public final Expression func;
  private FunctionOverloadInstance functionInstance;
  private TyNativeFunctional functionalType;

  public Map(final Expression sql, final Token mapToken, final Expression func) {
    super(sql);
    ingest(sql);
    this.mapToken = mapToken;
    this.func = func;
    ingest(func);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    sql.emit(yielder);
    yielder.accept(mapToken);
    func.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    sql.format(formatter);
    func.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    final var typeSql = sql.typing(environment, null);
    if (typeSql != null && RuleSetLists.IsNativeList(environment, typeSql, false)) {
      ArrayList<TyType> guessInputTypes = new ArrayList<>();
      guessInputTypes.add(RuleSetCommon.ExtractEmbeddedType(environment, typeSql, false));
      FunctionOverloadInstance guess = new FunctionOverloadInstance("unknown", null, guessInputTypes, FunctionPaint.READONLY_NORMAL);
      TyType guessType = new TyNativeFunctional("unknown", FunctionOverloadInstance.WRAP(guess), FunctionStyleJava.None);
      TyType funcType = func.typing(environment, guessType);
      if (environment.rules.IsFunction(funcType, false)) {
        functionalType = (TyNativeFunctional) funcType;
        functionInstance = functionalType.find(this, guessInputTypes, environment);
        if (functionInstance.aborts) {
          environment.document.createError(this, String.format("Function '%s' must not abort within a map function", funcType.getAdamaType()));
        }
        if (functionInstance.returnType == null) {
          environment.document.createError(this, String.format("Function '%s' must return value", funcType.getAdamaType()));
        }
        return new TyNativeList(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem<>(functionInstance.returnType));
      }
    }
    return null;
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    sql.writeJava(sb, environment);
    sb.append(".mapFunction(");
    if (functionalType != null) {
      switch (functionalType.style) {
        case ExpressionThenArgs:
        case ExpressionThenNameWithArgs:
          func.writeJava(sb, environment);
          break;
        default:
          sb.append("(__item) -> ").append(functionInstance.javaFunction).append("(__item)");
          break;
      }
    } else {
      sb.append("(__list) -> (__list)");
    }
    sb.append(")");
  }

  @Override
  public void free(FreeEnvironment environment) {
    sql.free(environment);
    func.free(environment);
  }
}
