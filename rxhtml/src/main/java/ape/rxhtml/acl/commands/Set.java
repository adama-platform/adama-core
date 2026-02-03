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
package ape.rxhtml.acl.commands;

import ape.rxhtml.atl.ParseException;
import ape.rxhtml.atl.Parser;
import ape.rxhtml.atl.tree.Tree;
import ape.rxhtml.template.Environment;
import ape.rxhtml.template.Escapes;
import ape.rxhtml.template.StatePath;
import ape.rxhtml.typing.ViewScope;

import java.util.Map;

/**
 * ACL command that sets a value at a state path to a specified value.
 * Supports both constant values and dynamic expressions with variable interpolation.
 * Implements BulkCommand for atomic batch operations. Syntax: set:path=value
 */
public class Set implements Command, BulkCommand {
  public final String path;
  public final String value;
  public final Tree tree;
  public final boolean constant;
  public final String name;

  public Set(String path, String value) throws ParseException {
    if (path.startsWith("view:") | path.startsWith("data:")) {
      this.path = path;
    } else {
      this.path = "view:" + path;
    }
    this.value = value;
    this.tree = Parser.parse(value);
    StatePath test = (StatePath.resolve(this.path, "$X"));
    this.constant = tree.variables().size() == 0 && test.isRootLevelViewConstant();
    this.name = test.name;
  }

  @Override
  public void write(Environment env, String type, String eVar) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeTypes(ViewScope vs) {
    if (tree.variables().size() == 0) {
      vs.write(this.path, Escapes.typeOf(value), false);
    } else {
      vs.write(this.path, "string-formula", false);
    }
  }

  @Override
  public void writeBulk(Environment env, String eVar, String appendTo) {
    StatePath pathSet = StatePath.resolve(this.path, env.stateVar);
    if (tree.hasAuto()) {
      env.feedback.warn(env.element, "set's can't use auto variables");
    }
    Map<String, String> vars = tree.variables();
    if (vars.size() == 0) {
      env.writer.tab().append(appendTo).append(".push(").append("$.bS(").append(eVar).append(",").append(pathSet.command).append(",'").append(pathSet.name).append("',").append(Escapes.constantOf(value)).append("));").newline();
    } else {
      var oVar = env.pool.ask();
      env.writer.tab().append("var ").append(oVar).append("={};").newline();
      for (Map.Entry<String, String> ve : vars.entrySet()) {
        StatePath pathVar = StatePath.resolve(ve.getValue(), env.stateVar);
        env.writer.tab().append("$.YS(").append(pathVar.command).append(",").append(oVar).append(",'").append(pathVar.name).append("');").newline();
      }
      env.writer.tab().append(appendTo).append(".push(").append("$.bS(").append(eVar).append(",").append(pathSet.command).append(",'").append(pathSet.name).append("',function(){ return ").append(tree.js(env.contextOf("event:bulk"), oVar)).append(";}));").newline();
    }
  }
}
