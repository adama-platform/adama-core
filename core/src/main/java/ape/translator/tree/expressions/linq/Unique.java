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
import ape.translator.tree.types.checking.ruleset.RuleSetCommon;
import ape.translator.tree.types.checking.ruleset.RuleSetLists;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.DetailCanExtractForUnique;
import ape.translator.tree.types.traits.IsStructure;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;

import java.util.function.Consumer;

public class Unique extends LinqExpression {
  public final Token unique;
  public final Token mode;
  public final Token key;
  private TyType elementType;
  private boolean addGet;
  private String modeToUse = "ListUniqueMode.First";

  public Unique(Expression sql, Token unique, Token mode, Token key) {
    super(sql);
    this.unique = unique;
    this.mode = mode;
    if (mode != null) {
      if ("last".equals(mode.text)) {
        modeToUse = "ListUniqueMode.Last";
      }
    }
    this.key = key;
    ingest(sql);
    ingest(unique);
    if (key != null) {
      ingest(key);
    }
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    sql.emit(yielder);
    yielder.accept(unique);
    if (mode != null) {
      yielder.accept(mode);
      yielder.accept(key);
    }
  }

  @Override
  public void format(Formatter formatter) {
    sql.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    TyType typeSql = sql.typing(environment, suggestion);
    if (key != null) {
      if (typeSql != null && environment.rules.IsNativeListOfStructure(typeSql, false)) {
        elementType = RuleSetCommon.ExtractEmbeddedType(environment, typeSql, false);
        elementType = RuleSetCommon.ResolvePtr(environment, elementType, false);
        if (elementType != null && elementType instanceof IsStructure) {
          FieldDefinition fd = ((IsStructure) elementType).storage().fields.get(key.text);
          if (fd != null) {
            TyType fieldType = environment.rules.Resolve(fd.type, false);
            if (fieldType != null) {
              addGet = fieldType instanceof DetailComputeRequiresGet;
              if (addGet) {
                fieldType = ((DetailComputeRequiresGet) fieldType).typeAfterGet(environment);
              }
            }
            if (!(fieldType instanceof DetailCanExtractForUnique)) {
              environment.document.createError(this, "the key '" + key.text + "' must be capable of being compared, hashed, and equality tested for uniqueness");
            }
          } else {
            environment.document.createError(this, "the key '" + key.text + "' is not a field of '" + elementType.getAdamaType() + "'");
          }
        }
      } else {
        environment.document.createError(this, "unique with a key requires the list to contain records or messages");
      }
    } else {
      if (RuleSetLists.IsNativeList(environment, typeSql, false)) {
        elementType = RuleSetCommon.ExtractEmbeddedType(environment, typeSql, false);
        if (elementType != null) {
          addGet = elementType instanceof DetailComputeRequiresGet;
          if (addGet) {
            elementType = ((DetailComputeRequiresGet) elementType).typeAfterGet(environment);
          }
        }
        if (elementType != null && !(elementType instanceof DetailCanExtractForUnique)) {
          environment.document.createError(this, "the element has a type of'" + elementType.getAdamaType() + "' which is not capable of being compared, hashed, and equality tested for uniqueness");
        }
      } else {
        environment.document.createError(this, "unique requires a list");
      }
    }
    return typeSql;
  }

  @Override
  public void free(FreeEnvironment environment) {
    sql.free(environment);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    sql.writeJava(sb, environment);
    sb.append(".unique(").append(modeToUse).append(", (__x) -> ");
    if (key != null) {
      sb.append("__x.").append(key.text);
    } else {
      sb.append("__x");
    }
    if (addGet) {
      sb.append(".get()");
    }
    sb.append(")");
  }
}
