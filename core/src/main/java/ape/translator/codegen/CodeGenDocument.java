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

/** responsible for parts of the document which are common */
public class CodeGenDocument {
  public static void writePrelude(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("import ape.runtime.async.*;").writeNewline();
    sb.append("import ape.runtime.contracts.*;").writeNewline();
    sb.append("import ape.runtime.delta.*;").writeNewline();
    sb.append("import ape.runtime.exceptions.*;").writeNewline();
    sb.append("import ape.runtime.graph.*;").writeNewline();
    sb.append("import ape.runtime.index.*;").writeNewline();
    sb.append("import ape.runtime.json.*;").writeNewline();
    sb.append("import ape.runtime.natives.*;").writeNewline();
    sb.append("import ape.runtime.natives.algo.*;").writeNewline();
    sb.append("import ape.runtime.natives.lists.*;").writeNewline();
    sb.append("import ape.runtime.ops.*;").writeNewline();
    sb.append("import ape.runtime.reactives.*;").writeNewline();
    sb.append("import ape.runtime.reactives.tables.*;").writeNewline();
    sb.append("import ape.runtime.remote.*;").writeNewline();
    sb.append("import ape.runtime.remote.replication.*;").writeNewline();
    sb.append("import ape.runtime.stdlib.*;").writeNewline();
    sb.append("import ape.runtime.sys.*;").writeNewline();
    sb.append("import ape.runtime.sys.cron.*;").writeNewline();
    sb.append("import ape.runtime.sys.web.*;").writeNewline();
    sb.append("import ape.runtime.text.*;").writeNewline();
    sb.append("import java.time.*;").writeNewline();
    sb.append("import java.util.function.Consumer;").writeNewline();
    sb.append("import java.util.function.Function;").writeNewline();
    sb.append("import java.util.ArrayList;").writeNewline();
    sb.append("import java.util.Comparator;").writeNewline();
    sb.append("import java.util.HashMap;").writeNewline();
    sb.append("import java.util.HashSet;").writeNewline();
    sb.append("import java.util.Map;").writeNewline();
    sb.append("import java.util.Set;").writeNewline();
    for (final String imp : environment.state.globals.imports()) {
      if (imp.startsWith("ape.runtime.stdlib")) {
        continue;
      }
      sb.append("import ").append(imp).append(";").writeNewline();
    }
  }
}
