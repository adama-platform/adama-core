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
package ape.translator.codegen;

import ape.translator.env.Environment;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineTrafficHint;

public class CodeGenTraffic {
  public static void writeTrafficHint(final StringBuilderWithTabs sb, final Environment environment) {
    DefineTrafficHint hint = environment.document.trafficHint;
    if (hint == null) {
      sb.append("@Override").writeNewline();
      sb.append("public String __traffic(CoreRequestContext __context) { return \"\"; }").writeNewline();
    } else {
      sb.append("@Override").writeNewline();
      sb.append("public String __traffic(CoreRequestContext __context) {").tabUp().writeNewline();
      sb.append("NtPrincipal __who = __context.who;").writeNewline();
      sb.append("return ");
      hint.expression.writeJava(sb, hint.next(environment));
      sb.append(";").tabDown().writeNewline();
      sb.append("}").writeNewline();
    }
  }
}
