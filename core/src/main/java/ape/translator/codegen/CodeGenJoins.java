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
import ape.translator.tree.definitions.DefineAssoc;
import ape.translator.tree.types.structures.JoinAssoc;
import ape.translator.tree.types.structures.StructureStorage;

import java.util.Map;
import java.util.TreeSet;

public class CodeGenJoins {
  public static void writeGraphs(final StringBuilderWithTabs sb, final Environment environment) {
    for (Map.Entry<String, DefineAssoc> assoc : environment.document.assocs.entrySet()) {
      DefineAssoc da = assoc.getValue();
      sb.append("RxAssocGraph<RTx").append(da.toTypeName.text).append("> ___assoc_").append(da.name.text).append(" = new RxAssocGraph<RTx").append(da.toTypeName.text).append(">();");
    }
    if (environment.document.assocs.size() == 0) {
      sb.append("@Override").writeNewline();
      sb.append("protected long __computeGraphs() { return 0; }").writeNewline();
    } else {
      sb.append("@Override").writeNewline();
      sb.append("protected long __computeGraphs() {").tabUp().writeNewline();
      sb.append("long __gmemory = 0L;").writeNewline();
      for (Map.Entry<String, DefineAssoc> assoc : environment.document.assocs.entrySet()) {
        DefineAssoc da = assoc.getValue();
        sb.append("___assoc_").append(da.name.text).append(".compute();").writeNewline();
        sb.append("__gmemory += 1024 + ___assoc_").append(da.name.text).append(".memory();").writeNewline();
      }
      sb.append("return __gmemory;").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }
  }

  public static void writeJoins(final StructureStorage storage, final StringBuilderWithTabs sb, final Environment environment) {
    for (JoinAssoc ja : storage.joins) {
      // make the edge maker
      String edgeMaker = "__EDMK_" + ja.tableName.text + "_" + environment.autoVariable();
      sb.append("EdgeMaker<RTx").append(ja.edgeRecordName).append("> ").append(edgeMaker).append(" = new EdgeMaker<>() {").tabUp().writeNewline();
      Environment next = ja.nextItemEnv(environment);
      sb.append("@Override").writeNewline();
      sb.append("public Integer from(RTx").append(ja.edgeRecordName).append(" ").append(ja.itemVar.text).append(") {").tabUp().writeNewline();
      sb.append("return ");
      if (ja.fromMaybe) {
        sb.append("LibGraph.int2nullable(");
        ja.fromExpr.writeJava(sb, next);
        sb.append(")");
      } else {
        ja.fromExpr.writeJava(sb, next);
      }
      sb.append(";").tabDown().writeNewline();
      sb.append("}").writeNewline();
      sb.append("@Override").writeNewline();
      sb.append("public Integer to(RTx").append(ja.edgeRecordName).append(" ").append(ja.itemVar.text).append(") {").tabUp().writeNewline();
      sb.append("return ");
      if (ja.toMaybe) {
        sb.append("LibGraph.int2nullable(");
        ja.toExpr.writeJava(sb, next);
        sb.append(")");
      } else {
        ja.toExpr.writeJava(sb, next);
      }
      sb.append(";").tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
      sb.append("};").writeNewline();
      String tracker = "__DET_" + environment.autoVariable();
      sb.append("DifferentialEdgeTracker<RTx").append(ja.edgeRecordName).append(",RTx").append(ja.foundAssoc.toTypeName.text).append("> ").append(tracker).append(" = new DifferentialEdgeTracker<>(").append(ja.tableName.text).append(",___assoc_").append("" + ja.foundAssoc.name.text).append(",").append(edgeMaker).append(");").writeNewline();
      TreeSet<String> variablesToWatch = new TreeSet<>();
      variablesToWatch.addAll(ja.watching.variables);
      variablesToWatch.addAll(ja.watching.pubsub);
      for(String depend : variablesToWatch) {
        sb.append(depend).append(".__subscribe(").append(tracker).append(");").writeNewline();
      }
      sb.append(ja.tableName.text).append(".pump(").append(tracker).append(");").writeNewline();
    }
  }
}
