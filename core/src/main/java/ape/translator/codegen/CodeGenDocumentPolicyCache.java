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

import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.types.structures.StructureStorage;

public class CodeGenDocumentPolicyCache {
  public static void writeRecordDeltaClass(final StructureStorage storage, final StringBuilderWithTabs sb) {
    sb.append("public class DeltaPrivacyCache {").tabUp().writeNewline();
    for (String policy : storage.policies.keySet()) {
      sb.append("public final boolean ").append(policy).append(";").writeNewline();
    }
    int countdown = storage.policies.size();
    if (countdown == 0) {
      sb.append("public DeltaPrivacyCache(NtPrincipal __who) {}").tabDown().writeNewline();
      sb.append("}").writeNewline();
    } else {
      sb.append("public DeltaPrivacyCache(NtPrincipal __who) {").tabUp().writeNewline();
      for (String policy : storage.policies.keySet()) {
        sb.append("this.").append(policy).append("=__POLICY_").append(policy).append("(__who);");
        countdown--;
        if (countdown == 0) {
          sb.tabDown();
        }
        sb.writeNewline();
      }
      sb.append("}").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }

  }
}
