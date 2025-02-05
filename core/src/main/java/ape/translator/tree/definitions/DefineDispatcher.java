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

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * a dispatcher is a function attached to enum such that the enum controls the destiny of which
 * function gets called
 */
public class DefineDispatcher extends Definition {
  public final ArrayList<FunctionArg> args;
  public final Token closeParen;
  public final Block code;
  public final Token dispatchToken;
  public final Token doubleColonToken;
  public final Token enumNameToken;
  public final Token functionName;
  public final Token introReturnType;
  public final Token openParen;
  public final Token starToken;
  public final Token valueToken;
  public int positionIndex;
  public TyType returnType;
  public int signatureId;

  public DefineDispatcher(final Token dispatchToken, final Token enumNameToken, final Token doubleColonToken, final Token valueToken, final Token starToken, final Token functionName, final Token openParen, final ArrayList<FunctionArg> args, final Token closeParen, final Token introReturnType, final TyType returnType, final Block code) {
    this.dispatchToken = dispatchToken;
    this.enumNameToken = enumNameToken;
    this.doubleColonToken = doubleColonToken;
    this.valueToken = valueToken;
    this.starToken = starToken;
    this.functionName = functionName;
    this.openParen = openParen;
    this.args = args;
    this.closeParen = closeParen;
    this.introReturnType = introReturnType;
    this.returnType = returnType;
    this.code = code;
    ingest(dispatchToken);
    ingest(code);
  }

  public FunctionOverloadInstance computeFunctionOverloadInstance() {
    final var types = new ArrayList<TyType>();
    for (final FunctionArg arg : args) {
      types.add(arg.type);
    }
    return new FunctionOverloadInstance(" __DISPATCH_" + signatureId + "_" + functionName.text, returnType, types, FunctionPaint.NORMAL);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(dispatchToken);
    yielder.accept(enumNameToken);
    yielder.accept(doubleColonToken);
    if (valueToken != null) {
      yielder.accept(valueToken);
    }
    if (starToken != null) {
      yielder.accept(starToken);
    }
    yielder.accept(functionName);
    yielder.accept(openParen);
    for (final FunctionArg arg : args) {
      arg.emit(yielder);
    }
    yielder.accept(closeParen);
    if (introReturnType != null) {
      yielder.accept(introReturnType);
      returnType.emit(yielder);
    }
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(dispatchToken);
    code.format(formatter);
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    for(FunctionArg arg : args) {
      fe.define(arg.argName);
    }
    code.free(fe);
    checker.register(fe.free, (environment) -> {
      returnType = environment.rules.Resolve(returnType, false);
      environment.rules.FindEnumType(enumNameToken.text, this, false);
      for (final FunctionArg arg : args) {
        arg.typing(environment);
      }
      final var flow = code.typing(prepareEnvironment(environment));
      if (returnType != null && flow == ControlFlow.Open) {
        environment.document.createError(this, String.format("Dispatch '%s' does not return in all cases", functionName.text));
      }
    });
  }

  public Environment prepareEnvironment(final Environment environment) {
    final var toUse = environment.scopeDefine();
    final var enumType = environment.document.types.get(enumNameToken.text);
    toUse.define("self", enumType, true, this);
    for (final FunctionArg arg : args) {
      boolean readonly = arg.evalReadonly(true, this, environment);
      toUse.define(arg.argName, arg.type, readonly, arg.type);
    }
    toUse.setReturnType(returnType);
    return toUse;
  }

  public String signature() {
    final var sb = new StringBuilder();
    for (final FunctionArg arg : args) {
      sb.append("+").append(arg.type.getAdamaType());
    }
    return sb.toString();
  }
}
