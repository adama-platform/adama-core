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
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.definitions.DefineAssoc;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetCommon;
import ape.translator.tree.types.natives.TyNativeList;
import ape.translator.tree.types.reactive.TyReactiveRecord;

import java.util.function.Consumer;

public class Traverse extends LinqExpression{
  private final Token traverse;
  private final Token assocToUse;

  public Traverse(Expression sql, Token traverse, Token assocToUse) {
    super(sql);
    this.traverse = traverse;
    this.assocToUse = assocToUse;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    sql.emit(yielder);
    yielder.accept(traverse);
    yielder.accept(assocToUse);
  }

  @Override
  public void format(Formatter formatter) {
    sql.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    DefineAssoc da = environment.document.assocs.get(assocToUse.text);
    if (da == null) {
      environment.document.createError(this, String.format("The assoc '%s' being traversed doesn't exist", assocToUse.text));
    } else {
      environment.lookup_assoc(assocToUse.text);
    }
    TyType base = sql.typing(environment, suggestion);
    if (base != null && environment.rules.IsNativeListOfStructure(base, false)) {
      TyType embedTest = RuleSetCommon.Resolve(environment, environment.rules.ExtractEmbeddedType(base, false), false);
      if (environment.rules.IsRxStructure(embedTest, false) && da != null) {
        TyReactiveRecord recordType = (TyReactiveRecord) embedTest;
        if (!recordType.name.equals(da.fromTypeName.text)) {
          environment.document.createError(this, String.format("The assoc '%s' has an incompatible domain type; expected '%s', but have '%s'", assocToUse.text, da.fromTypeName.text, recordType.name));
        }
        TyType toType = environment.document.types.get(da.toTypeName.text);
        if (toType != null) {
          return new TyNativeList(TypeBehavior.ReadOnlyNativeValue, null, traverse, new TokenizedItem<>(toType));
        }
      }
    }
    return null;
  }

  @Override
  public void free(FreeEnvironment environment) {
    sql.free(environment);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    sb.append("___assoc_").append(assocToUse.text).append(".map(");
    sql.writeJava(sb, environment);
    sb.append(")");
  }
}
