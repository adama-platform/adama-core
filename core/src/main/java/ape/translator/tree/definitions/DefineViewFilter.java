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
package ape.translator.tree.definitions;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeBoolean;

import java.util.function.Consumer;

/** used within a record to define a custom policy */
public class DefineViewFilter extends DocumentPosition {
  public final Block code;
  public final Token defineFilter;
  public final Token name;
  public final TyNativeBoolean policyType;

  public DefineViewFilter(final Token definePolicy, final Token name, final Block code) {
    this.defineFilter = definePolicy;
    this.name = name;
    this.code = code;
    policyType = new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, name);
    ingest(definePolicy);
    ingest(code);
    policyType.ingest(name);
  }

  public void emit(final Consumer<Token> yielder) {
    yielder.accept(defineFilter);
    yielder.accept(name);
    code.emit(yielder);
  }

  public void format(Formatter formatter) {
    formatter.startLine(defineFilter);
    code.format(formatter);
  }

  public Environment scope(final Environment environment, DocumentPosition position) {
    Environment env = environment.scopeAsFilter().scopeWithComputeContext(ComputeContext.Computation);
    TyType returnType = policyType;
    if (position != null) {
      returnType = policyType.makeCopyWithNewPosition(position, policyType.behavior);
    }
    env.setReturnType(returnType);
    return env;
  }

  public void typing(Environment environment) {
    final var flow = code.typing(scope(environment, null));
    if (flow == ControlFlow.Open) {
      environment.document.createError(this, String.format("Filter '%s' does not return in all cases", name.text));
    }
  }
}
