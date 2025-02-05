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
import ape.translator.tree.definitions.DefineTest;

/** responsible for writing tests */
public class CodeGenTests {
  public static void writeTests(final StringBuilderWithTabs sb, final Environment environment) {
    // generate test bodies
    if (!environment.state.options.removeTests) {
      for (final DefineTest test : environment.document.tests) {
        sb.append("public void __test_").append(test.name).append("(TestReportBuilder __report) throws AbortMessageException {").tabUp().writeNewline();
        sb.append("__report.begin(\"").append(test.name).append("\");").writeNewline();
        sb.append("try ");
        test.code.writeJava(sb, environment.scopeAsUnitTest());
        sb.append(" finally {").tabUp().writeNewline();
        sb.append("__report.end(getAndResetAssertions());").tabDown().writeNewline();
        sb.append("}").tabDown().writeNewline();
        sb.append("}").writeNewline();
      }
    }
    sb.append("@Override").writeNewline();
    sb.append("public String[] __getTests() {").tabUp().writeNewline();
    sb.append("return new String[] {");
    if (!environment.state.options.removeTests) {
      var first = true;
      for (final DefineTest test : environment.document.tests) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append("\"").append(test.name).append("\"");
      }
    }
    sb.append("};").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("@Override").writeNewline();
    if (environment.document.tests.size() > 0 && !environment.state.options.removeTests) {
      sb.append("public void __test(TestReportBuilder report, String testName) throws AbortMessageException {").tabUp().writeNewline();
      sb.append("switch(testName) {").writeNewline();
      for (final DefineTest test : environment.document.tests) {
        sb.tab().append("case \"").append(test.name).append("\":").writeNewline();
        sb.tab().tab().append("  __test_").append(test.name).append("(report);").writeNewline();
        sb.tab().tab().append("  return;").writeNewline();
      }
      sb.append("}").tabDown().writeNewline();
      sb.append("}").writeNewline();
    } else {
      sb.append("public void __test(TestReportBuilder report, String testName) throws AbortMessageException {}").writeNewline();
    }
  }
}
