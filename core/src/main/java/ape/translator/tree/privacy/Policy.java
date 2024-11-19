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
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;

import java.util.function.Consumer;

/** defines a policy for field records */
public abstract class Policy extends DocumentPosition {
  public abstract void emit(Consumer<Token> yielder);

  public abstract void format(Formatter formatter);

  public abstract void typing(Environment environment, StructureStorage owningStructureStorage);

  public abstract boolean writePrivacyCheckGuard(StringBuilderWithTabs sb, FieldDefinition field, Environment environment);

  public abstract void free(FreeEnvironment environment);

  public abstract void writeTypeReflectionJson(JsonStreamWriter writer);
}
