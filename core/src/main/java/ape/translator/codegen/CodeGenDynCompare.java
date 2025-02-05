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
import ape.translator.parser.token.Token;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.linq.OrderBy;
import ape.translator.tree.expressions.linq.OrderPair;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;
import ape.translator.tree.types.traits.IsOrderable;

import java.util.ArrayList;

public class CodeGenDynCompare {
  public static void writeDynCompare(final StructureStorage storage, final StringBuilderWithTabs sb, final Environment environment, final String className) {
    sb.append("class DynCmp_").append(className).append(" implements Comparator<").append(className).append("> {").tabUp().writeNewline();
    sb.append("private final CompareField[] parsed;").writeNewline();
    sb.append("DynCmp_").append(className).append("(String instructions) {").tabUp().writeNewline();
    sb.append("this.parsed = DynCompareParser.parse(instructions);").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("@Override").writeNewline();
    sb.append("public int compare(").append(className).append(" __a, ").append(className).append(" __b) {").tabUp().writeNewline();

    sb.append("for (CompareField field : parsed) {").tabUp().writeNewline();
    sb.append("int delta = 0;").writeNewline();
    sb.append("switch (field.name) {").tabUp().writeNewline();
    ArrayList<FieldDefinition> orderableFields = new ArrayList<>();
    for (FieldDefinition fd : storage.fieldsByOrder) {
      var fieldType = OrderBy.getOrderableType(fd, environment);
      if ((fieldType instanceof IsOrderable)) {
        orderableFields.add(fd);
      }
    }
    int count = orderableFields.size();
    for (FieldDefinition fd : orderableFields) {
      sb.append("case \"").append(fd.name).append("\":").tabUp().writeNewline();
      sb.append("delta = ").append(OrderBy.getCompareLine(fd, environment, new OrderPair(null, Token.WRAP(fd.name), null, null))).append(";").writeNewline();
      sb.append("break;").tabDown();
      count--;
      if (count == 0) {
        sb.tabDown();
      }
      sb.writeNewline();
    }
    sb.append("}").writeNewline();
    sb.append("if (delta != 0) {").tabUp().writeNewline();
    sb.append("return field.desc ? -delta : delta;").tabDown().writeNewline();
    sb.append("}").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("return 0;").tabDown().writeNewline();
    sb.append("}").tabDown().writeNewline();
    sb.append("}").writeNewline();
  }
}
