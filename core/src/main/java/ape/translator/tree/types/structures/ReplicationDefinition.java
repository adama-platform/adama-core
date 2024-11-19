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
package ape.translator.tree.types.structures;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.definitions.DefineService;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetCommon;
import ape.translator.tree.types.checking.ruleset.RuleSetIngestion;
import ape.translator.tree.types.checking.ruleset.RuleSetMessages;
import ape.translator.tree.types.natives.TyNativeDynamic;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class ReplicationDefinition extends StructureComponent  {
  private final Token replication;
  private final Token open;
  public final Token service;
  private final Token split;
  public final Token method;
  private final Token close;
  public final Token name;
  private final Token equals;
  public final Expression expression;
  private final Token end;
  public final LinkedHashSet<String> servicesToWatch;
  public final LinkedHashSet<String> variablesToWatch;

  public ReplicationDefinition(Token replication, Token open, Token service, Token split, Token method, Token close, Token name, Token equals, Expression expression, Token end) {
    this.replication = replication;
    this.open = open;
    this.service = service;
    this.split = split;
    this.method = method;
    this.close = close;
    this.name = name;
    this.equals = equals;
    this.expression = expression;
    this.end = end;
    ingest(open);
    ingest(end);
    servicesToWatch = new LinkedHashSet<>();
    variablesToWatch = new LinkedHashSet<>();
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(replication);
    yielder.accept(open);
    yielder.accept(service);
    yielder.accept(split);
    yielder.accept(method);
    yielder.accept(close);
    yielder.accept(name);
    yielder.accept(equals);
    expression.emit(yielder);
    yielder.accept(end);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(replication);
    expression.format(formatter);
    formatter.endLine(end);
  }

  public void typing(Environment prior) {
    Environment env = prior.scopeWithComputeContext(ComputeContext.Computation);
    DefineService definition = env.document.services.get(service.text);
    if (definition == null) {
      env.document.createError(ReplicationDefinition.this, "The service '" + service.text + "' was not found for replication at '" + name.text + "'");
      return;
    }
    DefineService.ServiceReplication replication = definition.replicationsMap.get(method.text);
    if (replication == null) {
      env.document.createError(ReplicationDefinition.this, "The service '" + service.text + "' was had no replication for '" + method.text + "'");
      return;
    }
    if ("dynamic".equals(replication.inputTypeName.text)) {
      TyType type = expression.typing(env, new TyNativeDynamic(TypeBehavior.ReadOnlyNativeValue, null, null));
      RuleSetCommon.IsDynamic(env, type, false);
    } else {
      TyType expectedType = env.document.types.get(replication.inputTypeName.text);
      if (RuleSetMessages.IsNativeMessage(env, expectedType, false)) {
        TyType type = env.rules.Resolve(expression.typing(env, expectedType), false);
        if (RuleSetMessages.IsNativeMessage(env, type, false)) {
          RuleSetIngestion.CanAIngestB(env, expectedType, type, false);
        }
      }
    }
  }
}
