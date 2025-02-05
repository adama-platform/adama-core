/**
 * MIT License
 * 
 * Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
