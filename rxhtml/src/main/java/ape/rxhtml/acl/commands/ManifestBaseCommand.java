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
 * ACL command for managing web app manifests in multi-tenant applications.
 * Handles adding, using, and deleting manifest configurations stored in local storage.
 * Syntax: manifest-add:url, manifest-use:id, manifest-del:id
 */
public class ManifestBaseCommand implements Command {
  public final String command;
  public final String value;
  public final Tree tree;

  public ManifestBaseCommand(String command, String value) throws ParseException {
    this.command = command;
    this.value = value;
    this.tree = Parser.parse(value);
  }

  @Override
  public void write(Environment env, String type, String eVar) {
    Map<String, String> vars = tree.variables();
    if (vars.size() == 0) {
      env.writer.tab().append("$.MD_").append(command).append("(").append(eVar).append(",'").append(type).append("',").append(Escapes.constantOf(value)).append(");").newline();
    } else {
      var oVar = env.pool.ask();
      env.writer.tab().append("var ").append(oVar).append("={};").newline();
      for (Map.Entry<String, String> ve : vars.entrySet()) {
        StatePath pathVar = StatePath.resolve(ve.getValue(), env.stateVar);
        env.writer.tab().append("$.YS(").append(pathVar.command).append(",").append(oVar).append(",'").append(pathVar.name).append("');").newline();
      }
      env.writer.tab().append("$.MD_").append(command).append("(").append(eVar).append(",'").append(type).append("',function(){ return ").append(tree.js(env.contextOf("event:manifest-domain"), oVar)).append(";});").newline();
    }
  }

  @Override
  public void writeTypes(ViewScope vs) {
    // nothing
  }
}
