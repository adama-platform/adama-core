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
import ape.translator.tree.definitions.DefineCronTask;
import ape.translator.tree.types.reactive.TyReactiveTable;
import ape.translator.tree.types.structures.FieldDefinition;

import java.util.TreeSet;

public class CodeGenDebug {
  public static void writeDebugInfo(final StringBuilderWithTabs sb, final Environment environment) {
    TreeSet<String> tables = new TreeSet<>();
    for(FieldDefinition fd : environment.document.root.storage.fieldsByOrder) {
      if (fd.type instanceof TyReactiveTable) {
        tables.add(fd.name);
      }
    }

    if (tables.size() == 0 && environment.document.cronTasks.size() == 0) {
      sb.append("@Override").writeNewline();
      sb.append("public void __debug(JsonStreamWriter __writer) {}").writeNewline();
    } else {
      sb.append("@Override").writeNewline();
      sb.append("public void __debug(JsonStreamWriter __writer) {").tabUp().writeNewline();
      sb.append("__writer.writeObjectFieldIntro(\"tables\");").writeNewline();
      sb.append("__writer.beginObject();").writeNewline();
      for (String tbl : tables) {
        sb.append("__writer.writeObjectFieldIntro(\"").append(tbl).append("\");").writeNewline();
        sb.append(tbl).append(".debug(__writer);").writeNewline();
      }
      sb.append("__writer.endObject();").writeNewline();
      sb.append("__writer.writeObjectFieldIntro(\"cron\");").writeNewline();
      sb.append("__writer.beginObject();").writeNewline();
      for (DefineCronTask dct : environment.document.cronTasks.values()) {
        sb.append("__writer.writeObjectFieldIntro(\"").append(dct.name.text).append("\");").writeNewline();
        sb.append("__writer.writeLong(__").append(dct.name.text).append(".get());").writeNewline();
      }
      sb.append("__writer.endObject();").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }
  }
}
