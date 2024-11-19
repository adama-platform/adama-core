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
package ape.translator.tree.statements;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;

import java.util.function.Consumer;

/** scope a statement */
public class ScopeWrap extends Statement {
  private final Statement wrapped;

  public ScopeWrap(Statement wrapped) {
    this.wrapped = wrapped;
    ingest(wrapped);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    wrapped.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    wrapped.format(formatter);
  }

  @Override
  public ControlFlow typing(Environment environment) {
    return wrapped.typing(environment.scope());
  }

  @Override
  public void free(FreeEnvironment environment) {
    wrapped.free(environment);
  }

  @Override
  public void writeJava(StringBuilderWithTabs sb, Environment environment) {
    wrapped.writeJava(sb, environment.scope());
  }
}
