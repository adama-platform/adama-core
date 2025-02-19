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
import ape.translator.tree.common.LatentCodeSnippet;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.ruleset.RuleSetCommon;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.reactive.TyReactiveLazy;
import ape.translator.tree.types.reactive.TyReactiveMaybe;
import ape.translator.tree.types.reactive.TyReactiveString;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.IsOrderable;
import ape.translator.tree.types.traits.IsStructure;

import java.util.ArrayList;
import java.util.function.Consumer;

/** order the given sql expression result by the list of keys */
public class OrderBy extends LinqExpression implements LatentCodeSnippet {
  public final Token byToken;
  public final ArrayList<OrderPair> keys;
  public final Token orderToken;
  private final ArrayList<String> compareLines;
  private String comparatorName;
  private IsStructure elementType;

  public OrderBy(final Expression sql, final Token orderToken, final Token byToken, final ArrayList<OrderPair> keys) {
    super(sql);
    this.orderToken = orderToken;
    this.byToken = byToken;
    this.keys = keys;
    ingest(sql);
    ingest(orderToken);
    for (final OrderPair key : keys) {
      ingest(key);
    }
    elementType = null;
    compareLines = new ArrayList<>();
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    sql.emit(yielder);
    yielder.accept(orderToken);
    if (byToken != null) {
      yielder.accept(byToken);
    }
    for (final OrderPair op : keys) {
      op.emit(yielder);
    }
  }

  @Override
  public void format(Formatter formatter) {
    sql.format(formatter);
    for (final OrderPair op : keys) {
      op.format(formatter);
    }
  }

  public static TyType getOrderableType(FieldDefinition fd, Environment environment) {
    var fieldType = fd.type;
    if (fieldType instanceof TyReactiveLazy) {
      fieldType = environment.rules.ExtractEmbeddedType(fieldType, false);
    }
    fieldType = RuleSetCommon.Resolve(environment, fieldType, false);
    if (fieldType instanceof TyReactiveMaybe || fieldType instanceof TyNativeMaybe) {
      fieldType = environment.rules.ExtractEmbeddedType(fieldType, false);
      if (fieldType instanceof TyReactiveLazy) {
        fieldType = environment.rules.ExtractEmbeddedType(fieldType, false);
      }
      fieldType = RuleSetCommon.Resolve(environment, fieldType, false);
    }
    return fieldType;
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    final var typeSql = sql.typing(environment, null /* no suggestion makes sense */);
    if (typeSql != null && environment.rules.IsNativeListOfStructure(typeSql, false)) {
      var element = RuleSetCommon.ExtractEmbeddedType(environment, typeSql, false);
      element = RuleSetCommon.ResolvePtr(environment, element, false);
      if (element != null && element instanceof IsStructure) {
        elementType = (IsStructure) element;
        for (final OrderPair key : keys) {
          final var fd = ((IsStructure) element).storage().fields.get(key.name);
          if (fd != null && fd.type != null) {
            var fieldType = getOrderableType(fd, environment);
            if (!(fieldType instanceof IsOrderable)) {
              environment.document.createError(key, String.format("Typing issue: the structure '%s' has field '%s' but it is not orderable (type is %s)", element.getAdamaType(), key.name, fd.type.getAdamaType()));
            }
          } else {
            environment.document.createError(key, String.format("Field not found: the structure '%s' does not contain the field '%s'.", element.getAdamaType(), key.name));
          }
        }
      }
      return typeSql.makeCopyWithNewPosition(this, typeSql.behavior);
    }
    return null;
  }

  public static String getCompareLine(FieldDefinition fd, Environment environment, OrderPair key) {
    final var cmpLine = new StringBuilder();
    var compareType = fd.type;
    String nativeCompare = null;
    if (compareType instanceof TyNativeLong) {
      nativeCompare = "Long";
    } else if (compareType instanceof TyNativeInteger || compareType instanceof TyNativeEnum) {
      nativeCompare = "Integer";
    } else if (compareType instanceof TyNativeBoolean) {
      nativeCompare = "Boolean";
    } else if (compareType instanceof TyNativeDouble) {
      nativeCompare = "Double";
    }
    if (nativeCompare != null) {
      cmpLine.append(key.asc ? "" : "-").append(nativeCompare).append(".compare(__a.").append(key.name).append(", __b.").append(key.name).append(")");
      return cmpLine.toString();
    }
    var addLazyGet = false;
    if (compareType instanceof TyReactiveLazy) {
      compareType = environment.rules.ExtractEmbeddedType(compareType, false);
      addLazyGet = true;
    }
    compareType = RuleSetCommon.Resolve(environment, compareType, false);
    boolean supportInsensitive;
    String compareMethod = "compareTo";
    if (key.insensitive != null) {
      compareMethod = "compareToIgnoreCase";
    }
    cmpLine.append(key.asc ? "" : "-").append("__a.").append(key.name).append(addLazyGet ? ".get()" : "");
    if (compareType instanceof TyReactiveMaybe || compareType instanceof TyNativeMaybe) {
      TyType interiorType = RuleSetCommon.Resolve(environment, environment.rules.ExtractEmbeddedType(compareType, false), false);
      supportInsensitive = interiorType instanceof TyReactiveString || interiorType instanceof TyNativeString;
      cmpLine.append(".compareValues(__b.").append(key.name).append(addLazyGet ? ".get()" : "").append(", (__x, __y) -> __x.").append(compareMethod).append("(__y))");
    } else {
      supportInsensitive = compareType instanceof TyReactiveString || compareType instanceof TyNativeString;
      cmpLine.append(".").append(compareMethod).append("(__b.").append(key.name).append(addLazyGet ? ".get()" : "").append(")");
    }
    if (!supportInsensitive && key.insensitive != null) {
      environment.document.createError(fd, "The field '" + fd.name + "' can not be ordered insensitively");
    }
    return cmpLine.toString();
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    final var comparatorNameBuilder = new StringBuilder();
    comparatorNameBuilder.append("__ORDER_" + elementType.name());
    sql.writeJava(sb, environment);
    for (final OrderPair key : keys) {
      comparatorNameBuilder.append("_").append(key.name).append(key.asc ? "_a" : "_d");
      final var fd = elementType.storage().fields.get(key.name);
      if (fd != null) {
        compareLines.add(getCompareLine(fd, environment, key));
      }
    }
    comparatorName = comparatorNameBuilder.toString();
    sb.append(".orderBy(").append(intermediateExpression ? "false, " : "true, ").append(comparatorName).append(")");
    environment.document.add(comparatorName, this);
  }

  @Override
  public void writeLatentJava(final StringBuilderWithTabs sb) {
    if (elementType != null) {
      sb.append("private final static Comparator<RTx").append(elementType.name()).append("> ").append(comparatorName).append(" = new Comparator<RTx").append(elementType.name()).append(">() {").tabUp().writeNewline();
      sb.append("@Override").writeNewline();
      sb.append("public int compare(RTx").append(elementType.name()).append(" __a, RTx").append(elementType.name()).append(" __b) {").tabUp().writeNewline();
      var first = true;
      var n = keys.size();
      for (final String compareLine : compareLines) {
        n--;
        if (n == 0) {
          sb.append("return ").append(compareLine).append(";").tabDown().writeNewline();
        } else {
          if (first) {
            sb.append("int ");
            first = false;
          }
          sb.append("result = ").append(compareLine).append(";").writeNewline();
          sb.append("if (result != 0) return result;").writeNewline();
        }
      }
      sb.append("}").tabDown().writeNewline();
      sb.append("};").writeNewline();
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    sql.free(environment);
  }
}
