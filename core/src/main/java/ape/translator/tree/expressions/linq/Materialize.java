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
import ape.translator.tree.types.traits.IsStructure;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.function.Consumer;

public class Materialize extends LinqExpression {
  private final Token token;
  private boolean actuallyMaterialize;
  private String subType;
  private int indexCount;

  public Materialize(final Expression sql, final Token token) {
    super(sql);
    this.token = token;
    ingest(token);
    this.actuallyMaterialize = false;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    sql.emit(yielder);
    yielder.accept(token);
  }

  @Override
  public void format(Formatter formatter) {
    sql.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    TyType baseType = sql.typing(environment, suggestion);
    if (environment.rules.IsNativeListOfStructure(baseType, false)) {
      TyType elementTypeRaw = environment.rules.Resolve(((DetailContainsAnEmbeddedType) baseType).getEmbeddedType(environment), false);
      IsStructure elementType = (IsStructure) elementTypeRaw;
      subType = elementTypeRaw.getJavaBoxType(environment);
      this.indexCount = elementType.storage().indexSet.size();
      this.actuallyMaterialize = indexCount > 0;
    }
    return baseType;
  }

  @Override
  public void free(FreeEnvironment environment) {
    sql.free(environment);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    if (actuallyMaterialize) {
      sb.append("new MaterializedNtList<").append(subType).append(">(");
      sql.writeJava(sb, environment);
      sb.append(",").append(indexCount).append(")");
    } else {
      sql.writeJava(sb, environment);
    }
  }
}
