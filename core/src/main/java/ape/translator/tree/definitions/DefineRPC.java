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
package ape.translator.tree.definitions;

import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.privacy.PublicPolicy;
import ape.translator.tree.statements.Block;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.natives.TyNativePrincipal;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StorageSpecialization;
import ape.translator.tree.types.structures.StructureStorage;

import java.util.List;
import java.util.function.Consumer;

public class DefineRPC extends Definition {
  public final Token rpcToken;
  public final Token name;
  public final Token openParen;
  public final Token clientVar;
  public final List<FunctionArg> args;
  public final Token closeParen;
  public final Block code;
  private TyNativeMessage genMessageType;

  public DefineRPC(Token rpcToken, Token name, Token openParen, Token clientVar, List<FunctionArg> args, Token closeParen, Block code) {
    this.rpcToken = rpcToken;
    this.name = name;
    this.openParen = openParen;
    this.clientVar = clientVar;
    this.args = args;
    this.closeParen = closeParen;
    this.code = code;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(rpcToken);
    yielder.accept(name);
    yielder.accept(openParen);
    yielder.accept(clientVar);
    for (FunctionArg arg : args) {
      arg.emit(yielder);
    }
    yielder.accept(closeParen);
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(rpcToken);
    code.format(formatter);
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    for (FunctionArg arg : args) {
      fe.define(arg.argNameToken.text);
    }
    code.free(fe);
    checker.register(fe.free, (environment) -> {
      final var next = environment.scopeAsMessageHandler();
      next.define(clientVar.text, new TyNativePrincipal(TypeBehavior.ReadOnlyNativeValue, null, clientVar).withPosition(this), true, this);
      for (final FunctionArg arg : args) {
        next.define(arg.argName, arg.type, true, arg.type);
      }
      code.typing(next);
      genTyNativeMessage().typing(environment);
    });
  }

  public TyNativeMessage genTyNativeMessage() {
    if (genMessageType != null) {
      return genMessageType;
    }
    StructureStorage storage = new StructureStorage(name.cloneWithNewText("Gen" + name.text), StorageSpecialization.Message, false, false, openParen);
    PublicPolicy policy = new PublicPolicy(null);
    policy.ingest(rpcToken);
    for (FunctionArg arg : args) {
      storage.add(new FieldDefinition(policy, null, arg.type, arg.argNameToken, null, null, null, null, null, null, null, null));
    }
    genMessageType = new TyNativeMessage(TypeBehavior.ReadOnlyNativeValue, rpcToken, name.cloneWithNewText(genMessageTypeName()), storage);
    return genMessageType;
  }

  public String genMessageTypeName() {
    return "__Gen" + name.text.toUpperCase();
  }

}
