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
import ape.translator.tree.definitions.DefineDispatcher;
import ape.translator.tree.definitions.FunctionArg;
import ape.translator.tree.types.shared.EnumStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** generates the code to support enumerations */
public class CodeGenEnums {
  public static void writeDispatchers(final StringBuilderWithTabs sb, final EnumStorage storage, final HashMap<String, ArrayList<DefineDispatcher>> multi, final String name, final Environment environment) {
    for (final Map.Entry<String, ArrayList<DefineDispatcher>> entry : multi.entrySet()) {
      writeDispatcher(sb, storage, entry.getValue(), storage.getId(name, entry.getKey()), environment);
    }
  }

  public static void writeDispatcher(final StringBuilderWithTabs sb, final EnumStorage storage, final ArrayList<DefineDispatcher> dispatchers, final int dispatcherIndex, final Environment environment) {
    // write the individual functions
    final var firstDispatcher = dispatchers.get(0);
    DefineDispatcher catchAll = null;
    for (final DefineDispatcher potential : storage.findFindingDispatchers(dispatchers, storage.getDefaultLabel(), true).values()) {
      final var takeIt = catchAll == null || potential.valueToken != null && storage.getDefaultLabel().equals(potential.valueToken.text) && potential.starToken == null;
      if (takeIt) {
        catchAll = potential;
      }
    }
    for (final DefineDispatcher dispatcher : dispatchers) {
      sb.append("private ");
      if (dispatcher.returnType == null) {
        sb.append("void");
      } else {
        sb.append(dispatcher.returnType.getJavaConcreteType(environment));
      }
      if (dispatcher.starToken != null && dispatcher.valueToken == null) {
        catchAll = dispatcher;
      }
      sb.append(" __IND_DISPATCH_").append(dispatcherIndex + "_").append(dispatcher.functionName.text).append("__" + dispatcher.positionIndex).append("(");
      sb.append("int self");
      for (final FunctionArg arg : dispatcher.args) {
        sb.append(", ");
        sb.append(arg.type.getJavaConcreteType(environment)).append(" ").append(arg.argName);
      }
      sb.append(") ");
      dispatcher.code.writeJava(sb, dispatcher.prepareEnvironment(environment));
      sb.writeNewline();
    }
    sb.append("private ");
    if (firstDispatcher.returnType == null) {
      sb.append("void");
    } else {
      sb.append(firstDispatcher.returnType.getJavaConcreteType(environment));
    }
    sb.append(" __DISPATCH_").append(dispatcherIndex + "_").append(firstDispatcher.functionName.text).append("(int __value");
    for (final FunctionArg arg : firstDispatcher.args) {
      sb.append(", ");
      sb.append(arg.type.getJavaConcreteType(environment)).append(" ").append(arg.argName);
    }
    sb.append(") {").tabUp().writeNewline();
    for (final Map.Entry<String, Integer> option : storage.options.entrySet()) {
      final var matches = storage.findFindingDispatchers(dispatchers, option.getKey(), firstDispatcher.returnType == null);
      sb.append("if (__value == ").append(option.getValue() + "").append(") {").tabUp().writeNewline();
      var atSpecifc = 0;
      for (final Map.Entry<String, DefineDispatcher> associatedDispatcher : matches.entrySet()) {
        atSpecifc++;
        if (associatedDispatcher.getValue().returnType != null) {
          sb.append("return ");
        }
        sb.append("__IND_DISPATCH_").append(dispatcherIndex + "_").append(firstDispatcher.functionName.text).append("__" + associatedDispatcher.getValue().positionIndex).append("(__value");
        for (final FunctionArg arg : associatedDispatcher.getValue().args) {
          sb.append(", ");
          sb.append(arg.argName);
        }
        sb.append(");");
        if (atSpecifc == matches.size()) {
          if (associatedDispatcher.getValue().returnType == null) {
            sb.writeNewline().append("return;");
          }
          sb.tabDown();
        }
        sb.writeNewline();
      }
      sb.append("}");
      sb.writeNewline();
    }
    if (catchAll.returnType != null) {
      sb.append("return ");
    }
    sb.append("__IND_DISPATCH_").append(dispatcherIndex + "_").append(firstDispatcher.functionName.text).append("__" + catchAll.positionIndex).append("(__value");
    for (final FunctionArg arg : catchAll.args) {
      sb.append(", ");
      sb.append(arg.argName);
    }
    sb.append(");").tabDown().writeNewline();
    sb.append("}").writeNewline();
  }

  /** write an enum array out, maybe it filtered by prefix */
  public static void writeEnumArray(final StringBuilderWithTabs sb, final String name, final String id, final String prefix, final EnumStorage storage) {
    sb.append("private static final int [] __").append(id).append("_").append(name).append(" = new int[] {");
    final var filtered = new ArrayList<Map.Entry<String, Integer>>();
    for (final Map.Entry<String, Integer> option : storage.options.entrySet()) {
      if (option.getKey().startsWith(prefix)) {
        filtered.add(option);
      }
    }
    var first = true;
    for (final Map.Entry<String, Integer> option : filtered) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append(Integer.toString(option.getValue())); // value
    }
    sb.append("};").writeNewline();
  }

  public static void writeEnumFixer(final StringBuilderWithTabs sb, final String name, final EnumStorage storage) {
    sb.append("private static final int __EnumFix_").append(name).append("(int value) {").tabUp().writeNewline();
    sb.append("switch (value) {").tabUp().writeNewline();
    int countDown = storage.options.size();
    for (Map.Entry<String, Integer> entry : storage.options.entrySet()) {
      countDown--;
      sb.append("case ").append("" + entry.getValue()).append(":");
      if (countDown == 0) {
        sb.tabUp();
      }
      sb.writeNewline();
    }
    sb.append("return value;").tabDown().writeNewline();
    sb.append("default:").tabUp().writeNewline();
    sb.append("return ").append("" + storage.getDefaultValue()).append(";").tabDown().tabDown().writeNewline();
    sb.append("}").tabDown().writeNewline();
    sb.append("}").writeNewline();
  }

  public static void writeEnumNextPrevString(final StringBuilderWithTabs sb, final String name, final EnumStorage storage) {
    if (storage.options.size() > 0) {
      HashMap<Integer, Integer> next = new HashMap<>();
      HashMap<Integer, Integer> prev = new HashMap<>();
      Integer prior = null;
      int start = 0;
      for (Map.Entry<String, Integer> entry : storage.options.entrySet()) {
        if (prior == null) {
          start = entry.getValue();
        } else {
          next.put(prior, entry.getValue());
          prev.put(entry.getValue(), prior);
        }
        prior = entry.getValue();
      }
      next.put(prior, start);
      prev.put(start, prior);

      // <enum>.to_string()
      sb.append("private static final String __EnumString_").append(name).append("(int value) {").tabUp().writeNewline();
      sb.append("switch (value) {").tabUp().writeNewline();
      for (Map.Entry<String, Integer> entry : storage.options.entrySet()) {
        sb.append("case ").append("" + entry.getValue()).append(":").tabUp().writeNewline();
        sb.append("return \"").append(entry.getKey()).append("\";").tabDown().writeNewline();
      }
      sb.append("default:").tabUp().writeNewline();

      sb.append("return \"").append(storage.getDefaultLabel()).append("\";").tabDown().tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
      sb.append("}").writeNewline();

      // <enum>.next()
      sb.append("private static final int __EnumCycleNext_").append(name).append("(int value) {").tabUp().writeNewline();
      sb.append("switch (value) {").tabUp().writeNewline();
      for (Map.Entry<String, Integer> entry : storage.options.entrySet()) {
        int cV = entry.getValue();
        int nV = next.get(entry.getValue());
        if (nV != cV + 1) {
          sb.append("case ").append("" + entry.getValue()).append(":").tabUp().writeNewline();
          sb.append("return ").append("" + next.get(entry.getValue())).append(";").tabDown().writeNewline();
        }
      }
      sb.append("default:").tabUp().writeNewline();
      sb.append("return value + 1;").tabDown().tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
      sb.append("}").writeNewline();

      // <enum>.prev()
      sb.append("private static final int __EnumCyclePrev_").append(name).append("(int value) {").tabUp().writeNewline();
      sb.append("switch (value) {").tabUp().writeNewline();
      for (Map.Entry<String, Integer> entry : storage.options.entrySet()) {
        int cV = entry.getValue();
        int nV = prev.get(entry.getValue());
        if (nV != cV - 1) {
          sb.append("case ").append("" + entry.getValue()).append(":").tabUp().writeNewline();
          sb.append("return ").append("" + prev.get(entry.getValue())).append(";").tabDown().writeNewline();
        }
      }
      sb.append("default:").tabUp().writeNewline();
      sb.append("return value - 1;").tabDown().tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }
  }
}
