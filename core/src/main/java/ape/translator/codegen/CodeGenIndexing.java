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
package ape.translator.codegen;

import ape.translator.env.Environment;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.reactive.*;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.reactive.*;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;

import java.util.Map;

public class CodeGenIndexing {
  public static class IndexClassification {
    public final boolean requiresToInt;
    public final boolean isIntegral;
    public final boolean isPrincipal;
    public final boolean isString;
    public final boolean useHashCode;
    public final boolean good;
    public final String indexValueMethod;
    public final boolean isBoolean;

    public IndexClassification(TyType fieldType) {
      isBoolean = fieldType instanceof TyReactiveBoolean || fieldType instanceof TyNativeBoolean;
      boolean isReactiveIntegral = fieldType instanceof TyReactiveInteger || fieldType instanceof TyReactiveEnum;
      boolean isReactiveRequiresToInt = fieldType instanceof TyReactiveDate || fieldType instanceof TyReactiveTime || fieldType instanceof TyReactiveDateTime;
      boolean isReactiveString = fieldType instanceof TyReactiveString;
      boolean isReactive = isReactiveIntegral || isReactiveRequiresToInt || isReactiveString || fieldType instanceof TyReactiveBoolean || fieldType instanceof TyReactivePrincipal;
      requiresToInt = isReactiveRequiresToInt || fieldType instanceof TyNativeDate || fieldType instanceof TyNativeTime || fieldType instanceof TyNativeDateTime;
      isIntegral = isReactiveIntegral || fieldType instanceof TyNativeInteger || fieldType instanceof TyNativeEnum || requiresToInt;
      isPrincipal = fieldType instanceof TyReactivePrincipal || fieldType instanceof TyNativePrincipal;
      isString = isReactiveString || fieldType instanceof TyNativeString;
      useHashCode = isString || isPrincipal;
      good = isIntegral || isPrincipal || isString || isBoolean;
      if (isReactive) {
        indexValueMethod = "%s.getIndexValue()";
      } else if (isBoolean) {
        indexValueMethod = "((%s) ? 1 : 0)";
      } else if (requiresToInt) {
        indexValueMethod = "%s.toInt()";
      } else if (useHashCode) {
        indexValueMethod = "%s.hashCode()";
      } else {
        indexValueMethod = "%s";
      }
    }
  }

  public static void writeIndices(final String name, final StructureStorage storage, final StringBuilderWithTabs sb, final Environment environment) {
    boolean first;
    sb.append("@Override").writeNewline();
    sb.append("public String[] __getIndexColumns() {").tabUp().writeNewline();
    sb.append("return __INDEX_COLUMNS_").append(name).append(";").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("@Override").writeNewline();
    sb.append("public int[] __getIndexValues() {").tabUp().writeNewline();
    int indexId = 0;
    sb.append("return new int[] {");
    first = true;
    for (final Map.Entry<String, FieldDefinition> entry : storage.fields.entrySet()) {
      if (!storage.indexSet.contains(entry.getKey())) {
        continue;
      }
      final var fieldType = environment.rules.Resolve(entry.getValue().type, false);
      IndexClassification classification = new IndexClassification(fieldType);
      if (classification.good) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append("").append(String.format(classification.indexValueMethod, entry.getKey()));
      }
    }
    sb.append("};").tabDown().writeNewline();
    sb.append("}").writeNewline();
  }
  public static void writeIndexConstant(final String name, final StructureStorage storage, final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("private static String[] __INDEX_COLUMNS_").append(name).append(" = new String[] {");
    boolean first = true;
    for (final Map.Entry<String, FieldDefinition> entry : storage.fields.entrySet()) {
      if (!storage.indexSet.contains(entry.getKey())) {
        continue;
      }
      final var fieldType = environment.rules.Resolve(entry.getValue().type, false);
      IndexClassification classification = new IndexClassification(fieldType);
      if (classification.good) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append("\"").append(entry.getKey()).append("\"");
      }
    }
    sb.append("};").writeNewline();
  }

}
