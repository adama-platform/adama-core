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
package ape.translator.tree.statements.testing;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.natives.TyNativePrincipal;

import java.util.function.Consumer;

public class Send extends Statement {
  private final Token send;
  private final Token channel;
  private final Token open;
  private final Expression who;
  private final Token comma;
  private final Expression message;
  private final Token close;

  public Send(Token send, Token channel, Token open, Expression who, Token comma, Expression message, Token close) {
    this.send = send;
    this.channel = channel;
    this.open = open;
    this.who = who;
    this.comma = comma;
    this.message = message;
    this.close = close;
    ingest(send);
    ingest(channel);
    ingest(close);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(send);
    yielder.accept(channel);
    yielder.accept(open);
    who.emit(yielder);
    yielder.accept(comma);
    message.emit(yielder);
    yielder.accept(close);
  }

  @Override
  public void format(Formatter formatter) {
    who.format(formatter);
    message.format(formatter);
  }

  @Override
  public ControlFlow typing(Environment environment) {
    final var next = environment.scopeWithComputeContext(ComputeContext.Computation);
    if (!next.state.isTesting()) {
      environment.document.createError(this, String.format("@send is for testing purposes only"));
    }
    final var whoType = who.typing(next, new TyNativePrincipal(TypeBehavior.ReadOnlyNativeValue, null, null));
    environment.rules.IsPrincipal(whoType, false);
    final var exprType = message.typing(next, null /* ug */);
    environment.rules.IsNativeMessage(exprType, false);
    if (exprType != null) {
      final var messageNameType = next.document.channelToMessageType.get(channel.text);
      if (messageNameType == null) {
        environment.document.createError(this, String.format("Channel '%s' does not exist", channel.text));
        return ControlFlow.Open;
      }
      TyType messageType = environment.rules.FindMessageStructure(messageNameType, this, false);
      if (messageType != null) {
        environment.rules.CanTypeAStoreTypeB(messageType, exprType, StorageTweak.None, false);
      }
    }
    return ControlFlow.Open;
  }

  @Override
  public void free(FreeEnvironment environment) {
    who.free(environment);
    message.free(environment);
  }

  @Override
  public void writeJava(StringBuilderWithTabs sb, Environment environment) {
    String whoVar = "__who_" + environment.autoVariable();
    sb.append("NtPrincipal ").append(whoVar).append(" = ");
    who.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(";").writeNewline();
    sb.append("__test_send(\"").append(channel.text).append("\",").append(whoVar).append(",");
    message.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
    sb.append(");").writeNewline();
    sb.append("__test_progress();");
  }
}
