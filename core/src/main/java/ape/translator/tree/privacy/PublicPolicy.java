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
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;

import java.util.function.Consumer;

/** a public policy that means it is visible for all time */
public class PublicPolicy extends Policy {
  public final Token publicToken;

  public PublicPolicy(final Token publicToken) {
    this.publicToken = publicToken;
    ingest(publicToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    if (publicToken != null) {
      yielder.accept(publicToken);
    }
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void typing(final Environment environment, final StructureStorage owningStructureStorage) {
  }

  @Override
  public boolean writePrivacyCheckGuard(final StringBuilderWithTabs sb, final FieldDefinition field, final Environment environment) {
    return false;
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer) {
    writer.writeString("public");
  }

  @Override
  public void free(FreeEnvironment environment) {
  }
}
