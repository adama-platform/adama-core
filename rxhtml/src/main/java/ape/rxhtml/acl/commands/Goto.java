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
 * ACL command that navigates to another page within the RxHTML forest.
 * Supports both static paths and dynamic paths with variable interpolation.
 * Syntax: goto:/path or goto:/path/{variable}
 */
public class Goto implements Command {
  public final String raw;
  public final Tree value;

  public Goto(String value) throws ParseException {
    this.raw = value;
    this.value = Parser.parse(value);
  }

  @Override
  public void writeTypes(ViewScope vs) {
  }

  @Override
  public void write(Environment env, String type, String eVar) {
    Map<String, String> vars = value.variables();
    if (value.hasAuto()) {
      env.feedback.warn(env.element, "goto's can't use auto variables");
    }
    if (vars.size() == 0) {
      env.writer.tab().append("$.onGO(").append(eVar).append(",'").append(type).append("',").append(env.stateVar).append(",").append(Escapes.constantOf(raw)).append(");").newline();
    } else {
      var oVar = env.pool.ask();
      env.writer.tab().append("var ").append(oVar).append("={};").newline();
      for (Map.Entry<String, String> ve : vars.entrySet()) {
        StatePath pathVar = StatePath.resolve(ve.getValue(), env.stateVar);
        env.writer.tab().append("$.YS(").append(pathVar.command).append(",").append(oVar).append(",'").append(pathVar.name).append("');").newline();
      }
      env.writer.tab().append("$.onGO(").append(eVar).append(",'").append(type).append("',").append(env.stateVar).append(",function(){ return ").append(value.js(env.contextOf("event:" + type), oVar)).append(";});").newline();
    }
  }
}
