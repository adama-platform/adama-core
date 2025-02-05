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
import ape.translator.tree.definitions.DefineStateTransition;

import java.util.Map;

/** responsible for writing the state machine stepper */
public class CodeGenStateMachine {
  public static void writeStateMachine(final StringBuilderWithTabs sb, final Environment environment) {
    // write the code for each step function
    for (final Map.Entry<String, DefineStateTransition> entry : environment.document.transitions.entrySet()) {
      sb.append("private void __step_" + entry.getKey() + "() ");
      entry.getValue().code.writeJava(sb, environment.scopeAsStateMachineTransition());
      sb.writeNewline();
    }
    var n = environment.document.transitions.size();
    sb.append("@Override").writeNewline();
    if (n > 0) {
      sb.append("protected void __invoke_label(String __new_state) {").tabUp().writeNewline();
      sb.append("switch(__new_state) {").tabUp().writeNewline();
      for (final Map.Entry<String, DefineStateTransition> entry : environment.document.transitions.entrySet()) {
        sb.append(String.format("case \"%s\":", entry.getKey())).tabUp().writeNewline();
        sb.append(String.format("__step_%s();", entry.getKey())).writeNewline();
        sb.append("return;");
        n--;
        if (n == 0) {
          sb.tabDown();
        }
        sb.tabDown().writeNewline();
      }
      sb.append("}").tabDown().writeNewline();
      sb.append("}").writeNewline();
    } else {
      sb.append("protected void __invoke_label(String __new_state) {}").writeNewline();
    }
  }
}
