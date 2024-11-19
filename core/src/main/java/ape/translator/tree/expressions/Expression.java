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
package ape.translator.tree.expressions;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.types.TyType;

import java.util.function.Consumer;

public abstract class Expression extends DocumentPosition {
  protected TyType cachedType = null;

  public abstract void emit(Consumer<Token> yielder);

  public abstract void format(Formatter formatter);

  public TyType getCachedType() {
    return cachedType;
  }

  public boolean passedTypeChecking() {
    return cachedType != null;
  }

  public TyType typing(final Environment environment, final TyType suggestion) {
    if (cachedType == null) {
      cachedType = typingInternal(environment, suggestion);
    }
    return cachedType;
  }

  protected abstract TyType typingInternal(Environment environment, TyType suggestion);

  public Expression withPosition(final DocumentPosition position) {
    ingest(position);
    return this;
  }

  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    final var child = new StringBuilder();
    writeJava(child, environment);
    sb.append(child.toString());
  }

  public abstract void free(FreeEnvironment environment);

  public abstract void writeJava(StringBuilder sb, Environment environment);
}
