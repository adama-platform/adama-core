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

import ape.common.SimpleCancel;
import ape.runtime.natives.NtMessageBase;
import ape.runtime.reactives.RxExport;
import ape.translator.env.Environment;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineExport;
import ape.translator.tree.watcher.WatchSet;

import java.util.Map;

/** generates the exports that can be subscribed too (via SSE/MQTT) */
public class CodeGenExports {
  public static void writeExports(final StringBuilderWithTabs sb, final Environment environment) {
    for (Map.Entry<String, DefineExport> entry : environment.document.exports.entrySet()) {
      sb.append("public SimpleCancel __export_").append(entry.getKey()).append("(CoreRequestContext __context, String __viewerState, Stream<String> __stream) {").tabUp().writeNewline();
      sb.append("RTx__ViewerType __viewer = new RTx__ViewerType();").writeNewline();
      sb.append("__viewer.__ingest(new JsonStreamReader(__viewerState));").writeNewline();
      WatchSet watching = entry.getValue().watching;
      for (String tableToWatch : watching.tables) {
        sb.append("final RxTableGuard ___").append(tableToWatch).append(" = new RxTableGuard(__current.__guard);").writeNewline();
      }
      for (String mapToWatch : watching.maps) {
        sb.append("final RxMapGuard ___").append(mapToWatch).append(" = new RxMapGuard(__current.__guard);").writeNewline();
      }
      for (String mapToWatch : watching.assocs) {
        sb.append("final RxMapGuard ___").append(mapToWatch).append(" = new RxMapGuard(__current.__guard);").writeNewline();
      }

      sb.append("RxExport<RTx__ViewerType> __current = new RxExport<RTx__ViewerType>(__context, __viewer, __stream) {").tabUp().writeNewline();
      sb.append("@Override").writeNewline();
      sb.append("public NtMessageBase compute() {").tabUp().writeNewline();
      sb.append("return ");
      entry.getValue().expression.writeJava(sb, environment);
      sb.append(";").tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
      sb.append("};").writeNewline();

      for (String watched : watching.variables) {
        if (!watching.pubsub.contains(watched)) {
          sb.append(watched).append(".__subscribe(__current.__guard);").writeNewline();
        }
      }
      for (final String watched : watching.pubsub) {
        sb.append(watched).append(".__subscribe(___").append(watched).append(");").writeNewline(); // SUPER AWESOME MODE
        sb.append("__current.__guard(").append(watched).append(",___").append(watched).append(");").writeNewline();
      }
      sb.append("return __register_export(__current);").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }

    sb.append("@Override").writeNewline();
    sb.append("public SimpleCancel __export(CoreRequestContext __context, String __name, String __viewerState, Stream<String> __stream) {").tabUp().writeNewline();
    int n = environment.document.exports.size();
    if (n > 0) {
      //;sb.append("if (__name.equals(\"").append(environment.document.exports.)++"\")) {").tabUp().writeNewline();
      sb.append("switch (__name) {").tabUp().writeNewline();
      for (Map.Entry<String, DefineExport> entry : environment.document.exports.entrySet()) {
        sb.append("case \"").append(entry.getKey()).append("\":").tabUp().writeNewline();
        sb.append("return __export_").append(entry.getKey()).append("(__context, __viewerState, __stream);");
        n--;
        if (n == 0) {
          sb.tabDown();
        }
        //
        sb.tabDown().writeNewline();
      }
      sb.append("}").writeNewline();
    }
    sb.append("__stream.failure(new ErrorCodeException(ErrorCodes.LIVING_DOCUMENT_NO_EXPORT_BY_NAME));").writeNewline();
    sb.append("return SimpleCancel.NOTHING_TO_CANCEL;").tabDown().writeNewline();
    sb.append("}").writeNewline();
  }
}
