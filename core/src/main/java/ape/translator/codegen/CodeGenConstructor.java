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
import ape.translator.tree.definitions.DefineConstructor;

/** generates code the constructors within adama's doc */
public class CodeGenConstructor {
  public static void writeConstructors(final StringBuilderWithTabs sb, final Environment environment) {
    var idx = 0;
    var messageTypeNameToUse = "NtMessageBase";
    for (final DefineConstructor dc : environment.document.constructors) {
      if (dc.unifiedMessageTypeNameToUse != null) {
        messageTypeNameToUse = "RTx" + dc.unifiedMessageTypeNameToUse;
      }
      sb.append("private void __construct").append("_" + idx).append("(CoreRequestContext __context, NtPrincipal __who");
      sb.append(", ").append(messageTypeNameToUse).append(" ");
      sb.append(dc.messageNameToken == null ? "__object" : dc.messageNameToken.text);
      sb.append(") {");
      if (dc.code.statements.size() == 0) {
        sb.append("}").writeNewline();
      } else {
        sb.tabUp().writeNewline();
        dc.code.specialWriteJava(sb, environment, false, true);
        sb.append("}").writeNewline();
      }
      idx++;
    }
    if (idx == 0) {
      sb.append("@Override").writeNewline();
      sb.append("protected void __construct_intern(CoreRequestContext _c, NtMessageBase _m) {}").writeNewline();
      sb.append("@Override").writeNewline();
      sb.append("protected NtMessageBase __parse_construct_arg(JsonStreamReader __reader) {").tabUp().writeNewline();
      sb.append("__reader.skipValue();").writeNewline();
      sb.append("return NtMessageBase.NULL;").tabDown().writeNewline();
      sb.append("}").writeNewline();
    } else {
      sb.append("@Override").writeNewline();
      sb.append("protected NtMessageBase __parse_construct_arg(JsonStreamReader __reader) {").tabUp().writeNewline();
      if (!messageTypeNameToUse.equals("NtMessageBase")) {
        sb.append("return new ").append(messageTypeNameToUse).append("(__reader);").tabDown().writeNewline();
      } else {
        sb.append("__reader.skipValue();").writeNewline();
        sb.append("return NtMessageBase.NULL;").tabDown().writeNewline();
      }
      sb.append("}").writeNewline();
      sb.append("@Override").writeNewline();
      if (!messageTypeNameToUse.equals("NtMessageBase")) {
        sb.append("protected void __construct_intern(CoreRequestContext __context, NtMessageBase __object_pre_cast) {").tabUp().writeNewline();
        sb.append(messageTypeNameToUse).append(" __object = (").append(messageTypeNameToUse).append(") __object_pre_cast;").writeNewline();
      } else {
        sb.append("protected void __construct_intern(CoreRequestContext __context, NtMessageBase __object) {").tabUp().writeNewline();
      }
      for (var k = 0; k < idx; k++) {
        sb.append("__construct").append("_" + k).append("(__context, __context.who, __object);");
        if (k + 1 >= idx) {
          sb.tabDown();
        }
        sb.writeNewline();
      }
      sb.append("}").writeNewline();
    }
  }
}
