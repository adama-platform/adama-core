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
import ape.translator.tree.types.structures.BubbleDefinition;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;

/** when instrumented, generate a report of the state of the reactive connections */
public class CodeGenReport {
  public static void writeRxReport(final StructureStorage storage, final StringBuilderWithTabs sb, final Environment environment, String... others) {
    if (!environment.state.options.instrumentPerf) {
      sb.append("@Override").writeNewline();
      sb.append("public void __writeRxReport(JsonStreamWriter __writer) { }").writeNewline();
    } else {
      sb.append("@Override").writeNewline();
      sb.append("public void __writeRxReport(JsonStreamWriter __writer) {").tabUp().writeNewline();
      sb.append("__writer.beginObject();").writeNewline();
      for (FieldDefinition fd : storage.fields.values()) {
        sb.append(fd.name).append(".__reportRx(\"").append(fd.name).append("\", __writer);").writeNewline();
      }
      for (String other : others) {
        sb.append(other).append(".__reportRx(\"").append(other).append("\", __writer);").writeNewline();
      }
      for (BubbleDefinition bd : storage.bubbles.values()) {
        sb.append("___").append(bd.nameToken.text).append(".__reportRx(\"").append(bd.nameToken.text).append("\", __writer);").writeNewline();
      }
      sb.append("__writer.endObject();").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }
  }
}
