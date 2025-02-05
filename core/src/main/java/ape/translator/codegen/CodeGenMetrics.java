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

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineMetric;

public class CodeGenMetrics {
  public static void writeMetricsDump(final StringBuilderWithTabs sb, final Environment environment) {
    if (environment.document.metrics.size() == 0) {
      sb.append("@Override").writeNewline();
      sb.append("public String __metrics() { return \"{}\"; }").writeNewline();
    } else {
      sb.append("@Override").writeNewline();
      sb.append("public String __metrics() {").tabUp().writeNewline();
      sb.append("JsonStreamWriter __writer = new JsonStreamWriter();").writeNewline();
      sb.append("__writer.beginObject();").writeNewline();
      for (DefineMetric dm : environment.document.metrics.values()) {
        sb.append("__writer.writeObjectFieldIntro(\"" + dm.nameToken.text + "\");");
        if (environment.rules.IsLong(dm.metricType, true)) {
          sb.append("__writer.writeLong(");
        } else if (environment.rules.IsInteger(dm.metricType, true)) {
          sb.append("__writer.writeInteger(");
        } else {
          sb.append("__writer.writeDouble(");
        }
        dm.expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
        sb.append(");").writeNewline();
      }
      sb.append("__writer.endObject();").writeNewline();
      sb.append("return __writer.toString();").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }
  }
}
