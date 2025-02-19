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
package ape.translator.tree.expressions;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.LatentCodeSnippet;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.TyNativeFunctional;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.watcher.LambdaWatcher;

import java.util.*;
import java.util.function.Consumer;

/** a very simple single variable lambda expression */
public class Lambda extends Expression implements LatentCodeSnippet {

  private final Token lambdaToken;
  private final Token variable;
  private final Token colon;
  private final Expression expr;
  private TyType variableType;
  private TyType exprType;
  private int generatedClassId;
  private final StringBuilder exprCode;
  private String evalReturnType;
  private String evalVariableType;

  private final ArrayList<String> trapBuilder;
  private final TreeMap<String, String> closureTypes;
  private final TreeMap<String, TyType> closureTyTypes;

  // @lambda x -> x + 1;
  public Lambda(final Token lambdaToken, final Token variable, final Token colon, final Expression expr) {
    this.lambdaToken = lambdaToken;
    this.variable = variable;
    this.colon = colon;
    this.expr = expr;
    ingest(lambdaToken);
    ingest(variable);
    ingest(colon);
    ingest(expr);
    variableType = null;
    exprType = null;
    generatedClassId = -1;
    exprCode = new StringBuilder();
    evalReturnType = null;
    evalVariableType = null;
    trapBuilder = new ArrayList<>();
    closureTypes = new TreeMap<>();
    closureTyTypes = new TreeMap<>();
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(lambdaToken);
    yielder.accept(variable);
    yielder.accept(colon);
    expr.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    expr.format(formatter);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    // how can we infer the type of variable
    if (suggestion != null && suggestion instanceof TyNativeFunctional) {
      ArrayList<FunctionOverloadInstance> instances = ((TyNativeFunctional) suggestion).overloads;
      if (instances.size() != 1) {
        environment.document.createError(this, "Not enough functional overload instances available");
        return null;
      }
      FunctionOverloadInstance instance = instances.get(0);
      if (instance.types.size() != 1) {
        environment.document.createError(this, "Lambda requires exactly one parameter on type inference");
        return null;
      }

      final var watch = environment.watch(new LambdaWatcher(environment, closureTyTypes, closureTypes)).captureSpecials();
      HashMap<String, TyType> specialsUsed = watch.specials();
      Environment next = watch.scopeDefine().scopeAsReadOnlyBoundary();
      variableType = environment.rules.Resolve(instance.types.get(0), false);
      next.define(variable.text, variableType, true, this);
      if (variableType == null) {
        environment.document.createError(this, "Failed to infer the variable type of the lambda");
      }
      exprType = environment.rules.Resolve(expr.typing(next, null), false);
      if (specialsUsed != null) {
        for (Map.Entry<String, TyType> entry : specialsUsed.entrySet()) {
          closureTyTypes.put(entry.getKey(), entry.getValue());
          closureTypes.put(entry.getKey(), entry.getValue().getJavaConcreteType(environment));
        }
      }
      if (exprType != null && variableType != null) {
        FunctionOverloadInstance created = new FunctionOverloadInstance("apply", exprType, instance.types, FunctionPaint.READONLY_NORMAL);
        return new TyNativeFunctional("apply", FunctionOverloadInstance.WRAP(created), FunctionStyleJava.ExpressionThenArgs);
      }
    }
    environment.document.createError(this, "Failed to infer the type arguments for the @lambda");
    return null;
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    environment.document.add(this);
    if (exprType != null && variableType != null) {
      evalReturnType = exprType.getJavaBoxType(environment);
      evalVariableType = variableType.getJavaBoxType(environment);
      generatedClassId = environment.document.inventClassId();
      expr.writeJava(exprCode, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append("new __CLOSURE_Lambda" + generatedClassId + "(");
      var notfirst = false;
      for (final Map.Entry<String, String> entry : closureTypes.entrySet()) {
        if (notfirst) {
          sb.append(", ");
        }
        notfirst = true;
        sb.append(entry.getKey());
      }
      // list the variables
      sb.append(")");
    }
  }

  @Override
  public void writeLatentJava(final StringBuilderWithTabs sb) {
    sb.append("private class __CLOSURE_Lambda").append("" + generatedClassId).append(" implements Function<").append(evalVariableType).append(", ").append(evalReturnType).append(">").append(" {").tabUp().writeNewline();
    for (final Map.Entry<String, String> entry : closureTypes.entrySet()) {
      sb.append("private ").append(entry.getValue()).append(" ").append(entry.getKey()).append(";").writeNewline();
    }
    if (closureTypes.size() > 0) {
      sb.append("private __CLOSURE_Lambda" + generatedClassId + "(");
      var notfirst = false;
      for (final Map.Entry<String, String> entry : closureTypes.entrySet()) {
        if (notfirst) {
          sb.append(", ");
        }
        notfirst = true;
        sb.append(entry.getValue()).append(" ").append(entry.getKey());
      }
      sb.append(") {").tabUp().writeNewline();
      var untilTabDown = closureTypes.size();
      for (final Map.Entry<String, String> entry : closureTypes.entrySet()) {
        sb.append("this." + entry.getKey() + " = " + entry.getKey() + ";");
        if (--untilTabDown <= 0) {
          sb.tabDown();
        }
        sb.writeNewline();
      }
      sb.append("}").writeNewline();
    }
    sb.append("@Override").writeNewline();
    sb.append("public ").append(evalReturnType).append(" apply(").append(evalVariableType).append(" ").append(variable.text).append(") {").tabUp().writeNewline();
    for (final String trapToWrite : trapBuilder) {
      sb.append(trapToWrite).writeNewline();
    }
    sb.append(String.format("__code_cost ++;")).writeNewline();
    sb.append("return " + exprCode.toString() + ";").tabDown().writeNewline();
    sb.append("}").tabDown().writeNewline();
    sb.append("}").writeNewline();
  }

  @Override
  public void free(FreeEnvironment environment) {
    FreeEnvironment next = environment.push();
    next.define(variable.text);
    expr.free(next);
  }
}
