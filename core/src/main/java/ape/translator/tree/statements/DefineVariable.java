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

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.properties.CanAssignResult;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.checking.ruleset.RuleSetCommon;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.natives.TyNativeReactiveRecordPtr;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.traits.details.DetailInventDefaultValueExpression;
import ape.translator.tree.types.traits.details.DetailNativeDeclarationIsNotStandard;

import java.util.function.Consumer;

/** type var = expr; | let var = expr; */
public class DefineVariable extends Statement {
  public final String name;
  public final Token nameToken;
  public final Token preludeToken;
  private final Token endToken;
  private final Token equalToken;
  private TyType type;
  private Expression value;

  public DefineVariable(final Token preludeToken, final Token nameToken, final TyType type, final Token equalToken, final Expression value, final Token endToken) {
    this.preludeToken = preludeToken;
    if (preludeToken != null) {
      ingest(preludeToken);
    }
    this.nameToken = nameToken;
    ingest(nameToken);
    name = nameToken.text;
    this.type = type;
    if (type != null) {
      ingest(type);
    }
    this.equalToken = equalToken;
    this.value = value;
    if (equalToken != null) {
      ingest(value);
    }
    this.endToken = endToken;
    if (endToken != null) {
      ingest(endToken);
    }
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    if (preludeToken != null) {
      yielder.accept(preludeToken);
    }
    if (type != null) {
      type.emit(yielder);
    }
    yielder.accept(nameToken);
    if (equalToken != null) {
      yielder.accept(equalToken);
      value.emit(yielder);
    }
    if (endToken != null) {
      yielder.accept(endToken);
    }
  }

  @Override
  public void format(Formatter formatter) {
    if (type != null) {
      type.format(formatter);
    }
    if (equalToken != null) {
      value.format(formatter);
    }
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    // type the value
    TyType valueType = null;
    if (value != null) {
      valueType = environment.rules.Resolve(value.typing(environment.scopeWithComputeContext(ComputeContext.Computation), type), false);
      // infer the value type if auto
      if (type == null) {
        type = environment.rules.Resolve(valueType, false);
      }
    }
    // resolve it
    type = RuleSetCommon.Resolve(environment, type, false);
    // invent a value if we can
    if (value == null && type != null && type instanceof DetailInventDefaultValueExpression && !(type instanceof DetailNativeDeclarationIsNotStandard)) {
      value = ((DetailInventDefaultValueExpression) type).inventDefaultValueExpression(this);
      valueType = value.typing(environment.scopeWithComputeContext(ComputeContext.Computation), type);
    }
    if (type != null && type instanceof TyReactiveRecord) {
      type = new TyNativeReactiveRecordPtr(TypeBehavior.ReadWriteWithSetGet, (TyReactiveRecord) type);
      if (value == null) {
        environment.document.createError(type, String.format("Reactive pointers must be initialized"));
      }
    }
    // is capable of getting an assignment
    if (type != null && valueType != null) {
      final var result = environment.rules.CanAssignWithSet(type, valueType, false);
      final var canStore = environment.rules.CanTypeAStoreTypeB(type, valueType, StorageTweak.None, false);
      if (!canStore || result == CanAssignResult.No) {
        type = null;
      }
      if (type instanceof TyNativeMessage && type != null && valueType != null && valueType.behavior == TypeBehavior.ReadOnlyNativeValue) {
        type = type.makeCopyWithNewPosition(type, valueType.behavior);
      }
    }
    if (type != null) {
      type.typing(environment);
      if (environment.defined(name)) {
        environment.document.createError(this, String.format("Variable '%s' was already defined", name));
      } else {
        environment.define(name, type.makeCopyWithNewPosition(this, type.behavior), type.behavior == TypeBehavior.ReadOnlyNativeValue, type);
      }
    }
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    if (type != null) {
      boolean isReadOnly = type.behavior == TypeBehavior.ReadOnlyNativeValue;
      if (isReadOnly) {
        sb.append("final ");
      }
      sb.append(type.getJavaConcreteType(environment)).append(" " + name);
      if (value != null) {
        sb.append(" = ");
        if (type instanceof DetailNativeDeclarationIsNotStandard) {
          final var child = new StringBuilder();
          value.writeJava(child, environment.scopeWithComputeContext(ComputeContext.Computation));
          sb.append(String.format(((DetailNativeDeclarationIsNotStandard) type).getPatternWhenValueProvided(environment), child));
        } else {
          value.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
        }
        sb.append(";");
      } else {
        if (type instanceof DetailNativeDeclarationIsNotStandard) {
          sb.append(" = ").append(((DetailNativeDeclarationIsNotStandard) type).getStringWhenValueNotProvided(environment));
        }
        sb.append(";");
      }
      environment.define(name, type, isReadOnly, type);
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    environment.define(name);
    if (value != null) {
      value.free(environment);
    }
  }
}
