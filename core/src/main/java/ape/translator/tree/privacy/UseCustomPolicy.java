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
package ape.translator.tree.privacy;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;

import java.util.HashSet;
import java.util.function.Consumer;

/** a policy that refers to code within the record */
public class UseCustomPolicy extends Policy {
  public final Token customToken;
  public final Guard guard;
  private final HashSet<String> globals;

  public UseCustomPolicy(final Token customToken, Guard guard) {
    this.customToken = customToken;
    this.guard = guard;
    ingest(customToken);
    ingest(guard);
    globals = new HashSet<>();
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(customToken);
    guard.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    guard.format(formatter);
  }

  @Override
  public void typing(final Environment environment, final StructureStorage owningStructureStorage) {
    for (final TokenizedItem<String> policyToCheck : guard.policies) {
      var dcp = owningStructureStorage.policies.get(policyToCheck.item);
      if (dcp == null) {
        globals.add(policyToCheck.item);
        dcp = environment.document.root.storage.policies.get(policyToCheck.item);
        if (dcp == null) {
          environment.document.createError(this, String.format("Policy '%s' was not found", policyToCheck.item));
        }
      } else {
        if (owningStructureStorage.root) {
          globals.add(policyToCheck.item);
        }
      }
    }
    for (TokenizedItem<String> filter : guard.filters) {
      if (!environment.document.viewerType.storage.viewFilters.containsKey(filter.item)) {
        environment.document.createError(this, String.format("Filter '%s' was not found", filter.item));
      }
    }
  }

  @Override
  public boolean writePrivacyCheckGuard(final StringBuilderWithTabs sb, final FieldDefinition field, final Environment environment) {
    sb.append("if (");
    var first = true;
    for (final TokenizedItem<String> policyToCheck : guard.policies) {
      if (first) {
        first = false;
      } else {
        sb.append(" && ");
      }
      if (globals.contains(policyToCheck.item)) {
        sb.append("__policy_cache.").append(policyToCheck.item);
      } else {
        sb.append("__item.__POLICY_").append(policyToCheck.item).append("(__writer.who)");
      }
    }
    for (final TokenizedItem<String> filterToRequire : guard.filters) {
      if (first) {
        first = false;
      } else {
        sb.append(" && ");
      }
      sb.append("__VIEWER.__vf_").append(filterToRequire.item);
    }
    sb.append(") {").tabUp().writeNewline();
    return true;
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer) {
    guard.writeReflect(writer);
  }

  @Override
  public void free(FreeEnvironment environment) {
  }
}
