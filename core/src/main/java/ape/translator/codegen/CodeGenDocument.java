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

/** responsible for parts of the document which are common */
public class CodeGenDocument {
  public static void writePrelude(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("import ape.common.Pair;").writeNewline();
    sb.append("import ape.runtime.async.*;").writeNewline();
    sb.append("import ape.runtime.contracts.*;").writeNewline();
    sb.append("import ape.runtime.delta.*;").writeNewline();
    sb.append("import ape.runtime.exceptions.*;").writeNewline();
    sb.append("import ape.runtime.graph.*;").writeNewline();
    sb.append("import ape.runtime.index.*;").writeNewline();
    sb.append("import ape.runtime.json.*;").writeNewline();
    sb.append("import ape.runtime.natives.*;").writeNewline();
    sb.append("import ape.runtime.natives.algo.*;").writeNewline();
    sb.append("import ape.runtime.natives.lists.*;").writeNewline();
    sb.append("import ape.runtime.ops.*;").writeNewline();
    sb.append("import ape.runtime.reactives.*;").writeNewline();
    sb.append("import ape.runtime.reactives.tables.*;").writeNewline();
    sb.append("import ape.runtime.remote.*;").writeNewline();
    sb.append("import ape.runtime.remote.client.*;").writeNewline();
    sb.append("import ape.runtime.remote.replication.*;").writeNewline();
    sb.append("import ape.runtime.stdlib.*;").writeNewline();
    sb.append("import ape.runtime.sys.*;").writeNewline();
    sb.append("import ape.runtime.sys.cron.*;").writeNewline();
    sb.append("import ape.runtime.sys.web.*;").writeNewline();
    sb.append("import ape.runtime.text.*;").writeNewline();
    sb.append("import java.time.*;").writeNewline();
    sb.append("import java.util.function.Consumer;").writeNewline();
    sb.append("import java.util.function.Function;").writeNewline();
    sb.append("import java.util.ArrayList;").writeNewline();
    sb.append("import java.util.Comparator;").writeNewline();
    sb.append("import java.util.HashMap;").writeNewline();
    sb.append("import java.util.HashSet;").writeNewline();
    sb.append("import java.util.Map;").writeNewline();
    sb.append("import java.util.Set;").writeNewline();
    for (final String imp : environment.state.globals.imports()) {
      if (imp.startsWith("ape.runtime.stdlib")) {
        continue;
      }
      sb.append("import ").append(imp).append(";").writeNewline();
    }
  }
}
