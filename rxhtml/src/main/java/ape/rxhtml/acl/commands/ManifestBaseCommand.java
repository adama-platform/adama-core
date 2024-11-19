/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

/** Manifests are e part of a multi-tenant web app where there is a local storage config with add, use, and delete */
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
