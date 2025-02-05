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
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.definitions.DefineFunction;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;

import java.util.ArrayList;
import java.util.Iterator;

/** responsible for writing all document level functions */
public class CodeGenFunctions {
  public static void writeArgsJava(final StringBuilder sb, final Environment environment, final boolean firstSeed, final ArrayList<TokenizedItem<Expression>> args, final FunctionOverloadInstance functionInstance) {
    var first = firstSeed;
    Iterator<TyType> castIt = functionInstance.types.iterator();
    for (final TokenizedItem<Expression> arg : args) {
      if (!first) {
        sb.append(", ");
      } else {
        first = false;
      }
      boolean cast = functionInstance.castArgs && castIt.hasNext() && arg.item.passedTypeChecking();
      if (cast) {
        String concreteType = castIt.next().getJavaConcreteType(environment);
        String givenType = arg.item.getCachedType().getJavaConcreteType(environment);
        cast = !concreteType.equals(givenType);
        if (cast) {
          sb.append("(");
          sb.append(concreteType);
          sb.append(")");
          sb.append("(");
        }
      }
      arg.item.writeJava(sb, environment);
      if (cast) {
        sb.append(")");
      }
    }
    for (final String hiddenSuffix : functionInstance.hiddenSuffixArgs) {
      if (!first) {
        sb.append(", ");
      } else {
        first = false;
      }
      sb.append(hiddenSuffix);
    }
  }

  public static void writeFunctionsJava(final StringBuilderWithTabs sb, final Environment environment) {
    for (final DefineFunction df : environment.document.functionDefinitions) {
      df.writeFunctionJava(sb, df.prepareEnvironment(environment));
    }
  }
}
