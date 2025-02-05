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
package ape.translator.tree.expressions.constants;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeList;

import java.util.function.Consumer;

/** a simple empty list with a given type */
public class EmptyListConstant extends Expression {
  public final Token emptyListToken;
  public final TokenizedItem<TyType> type;
  private TyType resolved = null;

  public EmptyListConstant(Token emptyListToken, TokenizedItem<TyType> type) {
    this.emptyListToken = emptyListToken;
    this.type = type;
    ingest(emptyListToken);
    ingest(type.item);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(emptyListToken);
    type.emitBefore(yielder);
    type.item.emit(yielder);
    type.emitAfter(yielder);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    resolved = environment.rules.Resolve(type.item, false);
    return new TyNativeList(TypeBehavior.ReadOnlyNativeValue, null, null, type);
  }

  @Override
  public void free(FreeEnvironment environment) {}

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    if (resolved != null) {
      sb.append("new EmptyNtList<").append(resolved.getJavaBoxType(environment)).append(">()");
    }
  }
}
