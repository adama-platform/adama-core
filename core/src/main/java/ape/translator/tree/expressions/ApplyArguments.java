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

import ape.translator.codegen.CodeGenFunctions;
import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.LatentCodeSnippet;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.natives.functions.TyNativeAggregateFunctional;
import ape.translator.tree.watcher.AggregateApplyWatcher;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * functional application. This applies arguments to the given expression, and the expectation is
 * that the expression is a function of some sorts
 */
public class ApplyArguments extends Expression implements LatentCodeSnippet {
  private final ArrayList<TokenizedItem<Expression>> args;
  private final Token closeParenToken;
  private final Expression expression;
  private final Token openParenToken;
  private TyType aggregateInputType;
  private TyType aggregateOutputType;
  private TreeMap<String, TyType> closureTyTypes;
  private FunctionOverloadInstance functionInstance;
  private FunctionStyleJava functionStyle;
  private boolean isAggregate;
  private ArrayList<String> latentLines;
  private TyType returnType;

  /** @param expression the expression that must be something that is or contains a function */
  public ApplyArguments(final Expression expression, final Token openParenToken, final ArrayList<TokenizedItem<Expression>> args, final Token closeParenToken) {
    this.expression = expression;
    this.openParenToken = openParenToken;
    this.args = args;
    this.closeParenToken = closeParenToken;
    this.ingest(expression);
    this.ingest(openParenToken);
    this.ingest(closeParenToken);
    functionInstance = null;
    closureTyTypes = null;
    latentLines = null;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    expression.emit(yielder);
    yielder.accept(openParenToken);
    for (final TokenizedItem<Expression> element : args) {
      element.emitBefore(yielder);
      element.item.emit(yielder);
      element.emitAfter(yielder);
    }
    yielder.accept(closeParenToken);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
    for (final TokenizedItem<Expression> element : args) {
      element.item.format(formatter);
    }
  }

  @Override
  protected TyType typingInternal(final Environment environmentX, final TyType suggestion) {
    final var exprType = environmentX.rules.Resolve(expression.typing(environmentX, null), false);
    environmentX.rules.IsFunction(exprType, false);
    if (exprType != null && exprType instanceof TyNativeFunctional) {
      isAggregate = exprType instanceof TyNativeAggregateFunctional;
      var environmentToUse = environmentX;
      if (isAggregate) {
        closureTyTypes = new TreeMap<>();
        environmentToUse = environmentToUse.watch(new AggregateApplyWatcher(environmentToUse, closureTyTypes));
        environmentToUse.document.add(this);
      }
      final var argTypes = new ArrayList<TyType>();
      FunctionOverloadInstance guess = ((TyNativeFunctional) exprType).guess(args.size());
      int at = 0;
      for (final TokenizedItem<Expression> arg : args) {
        argTypes.add(arg.item.typing(environmentToUse.scopeWithComputeContext(ComputeContext.Computation), guess != null ? guess.types.get(at) : null));
        at++;
      }
      exprType.typing(environmentToUse);
      functionStyle = ((TyNativeFunctional) exprType).style;
      boolean exceptIfWebGet = environmentToUse.state.isWeb() && environmentToUse.state.getWebMethod().equals("get");
      if (functionStyle == FunctionStyleJava.RemoteCall) {
        boolean mustBeReadonly = environmentToUse.state.isReadonlyEnvironment() || environmentToUse.state.isPure();
        if (mustBeReadonly && !exceptIfWebGet) {
          environmentToUse.document.createError(expression, String.format("Services can not be invoked in read-only or pure functional context"));
        } else if (environmentToUse.state.getCacheObject() == null) {
          environmentToUse.document.createError(expression, String.format("Remote invocation not available in this scope"));
        }
      }
      functionInstance = ((TyNativeFunctional) exprType).find(expression, argTypes, environmentToUse);
      for (String depend : functionInstance.dependencies) {
        environmentToUse.lookup(depend, true, this, true);
      }
      for (String assoc : functionInstance.assocs) {
        environmentToUse.lookup_assoc(assoc);
      }
      for (String vf : functionInstance.viewerFields) {
        environmentToUse.registerViewerField(vf);
      }
      if (environmentToUse.state.shouldDumpRecordMethodSubscriptions(functionInstance.withinRecord.get())) {
        for (String dependR : functionInstance.recordDependencies) {
          environmentToUse.lookup(dependR, true, this, true);
        }
      }
      if (environmentToUse.state.isPure() && !functionInstance.pure) {
        environmentToUse.document.createError(expression, String.format("Pure functions can only call other pure functions"));
      }
      if (functionInstance.aborts && !(environmentToUse.state.isAbortable() || environmentToUse.state.isMessageHandler())) {
        environmentToUse.document.createError(expression, String.format("Abortable functions can only be called from another abortable function or within a message handler"));
      }
      if (functionInstance.viewer) {
        environmentToUse.useSpecial(environmentToUse.document.viewerType, "__viewer");
      }
      boolean hasViewer = environmentToUse.state.isBubble() || environmentToUse.state.hasViewer();
      if (!hasViewer && functionInstance.viewer) {
        environmentToUse.document.createError(expression, String.format("Functions with viewer can only be used from viewer states"));
      }
      if (environmentToUse.state.isReadonlyEnvironment() && !functionInstance.pure) {
        boolean serviceCallInGet = functionStyle == FunctionStyleJava.RemoteCall && exceptIfWebGet;
        if (!serviceCallInGet) {
          environmentToUse.document.createError(expression, String.format("Read only methods can only call other read-only methods or pure functions"));
        }
      }
      if (environmentToUse.state.isReactiveExpression() && !functionInstance.pure) {
        environmentToUse.document.createError(expression, String.format("Reactive expressions can only invoke pure functions"));
      }
      returnType = functionInstance.returnType;
      if (isAggregate) {
        aggregateInputType = ((TyNativeAggregateFunctional) exprType).typeBase;
        aggregateOutputType = returnType;
      }
      if (returnType == null) {
        returnType = new TyNativeVoid();
      }
      returnType = environmentToUse.rules.Resolve(returnType, false);
      if (returnType != null) {
        if (isAggregate) {
          return new TyNativeList(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem<>(returnType)).withPosition(this);
        } else {
          return returnType.makeCopyWithNewPosition(this, returnType.behavior);
        }
      }
    }
    environmentX.document.createError(expression, String.format("Expression is not a function"));
    return null;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    if (functionInstance != null) {
      final var childEnv = environment.scopeWithComputeContext(ComputeContext.Computation);
      if (functionInstance.castReturn) {
        sb.append("(").append(returnType.getJavaBoxType(environment)).append(") (");
      }
      switch (functionStyle) {
        case RemoteCall: {
          expression.writeJava(sb, environment);
          sb.append(".invoke(__self, \"");
          sb.append(functionInstance.javaFunction).append("\", ").append(environment.state.getCacheObject());
          final var temp = new StringBuilder();
          CodeGenFunctions.writeArgsJava(temp, childEnv, false, args, functionInstance);
          sb.append(temp);
          TyNativeResult resultType = (TyNativeResult) (functionInstance.returnType);
          if (resultType.tokenElementType.item instanceof TyNativeArray) {
            // array not allowed for dynamic
            TyNativeMessage msgType = (TyNativeMessage) (((TyNativeArray) resultType.tokenElementType.item).elementType);
            sb.append(", (__json) -> Utility.readArray(new JsonStreamReader(__json), (__reader) -> new RTx")
                .append(msgType.name).append("(__reader), (__n) -> new RTx")
                .append(msgType.name).append("[__n]))");
          } else {
            if (resultType.tokenElementType.item instanceof TyNativeDynamic) {
              sb.append(", (__json) -> new NtDynamic(__json))");
            } else {
              TyNativeMessage msgType = (TyNativeMessage) (resultType.tokenElementType.item);
              sb.append(", (__json) -> new RTx").append(msgType.name).append("(new JsonStreamReader(__json)))");
            }
          }
        } break;
        case ExpressionThenNameWithArgs:
        case ExpressionThenArgs:
          expression.writeJava(sb, environment);
          if (isAggregate) {
            final var method = functionInstance.returnType != null ? ".transform" : ".map";
            if (closureTyTypes.size() > 0) {
              final var id = buildLatentApplyClassInstance(childEnv);
              sb.append(method).append("(new __CLOSURE_Apply").append("_" + id).append("(");
              var first = true;
              for (final Map.Entry<String, TyType> entry : closureTyTypes.entrySet()) {
                if (first) {
                  first = false;
                } else {
                  sb.append(", ");
                }
                sb.append(entry.getKey());
              }
              sb.append(")");
            } else if (functionInstance.returnType != null) {
              sb.append(method).append("((__item) -> __item.").append(functionInstance.javaFunction);
              sb.append("(");
              final var temp = new StringBuilder();
              CodeGenFunctions.writeArgsJava(temp, childEnv, true, args, functionInstance);
              sb.append(temp);
              sb.append(")");
            } else {
              sb.append(method).append("((__item) -> { __item.").append(functionInstance.javaFunction);
              sb.append("(");
              final var temp = new StringBuilder();
              CodeGenFunctions.writeArgsJava(temp, childEnv, true, args, functionInstance);
              sb.append(temp);
              sb.append("); }");
            }
            sb.append(")");
          } else {
            if (functionStyle == FunctionStyleJava.ExpressionThenNameWithArgs) {
              sb.append(".").append(functionInstance.javaFunction);
            }
            sb.append("(");
            CodeGenFunctions.writeArgsJava(sb, childEnv, true, args, functionInstance);
            sb.append(")");
          }
          break;
        case InjectName:
          sb.append(functionInstance.javaFunction);
          break;
        case InjectNameThenArgs:
          sb.append(functionInstance.javaFunction);
          sb.append("(");
          CodeGenFunctions.writeArgsJava(sb, childEnv, true, args, functionInstance);
          sb.append(")");
          break;
        case InjectNameThenArgsNoInitialParenthesis:
          sb.append(functionInstance.javaFunction);
          CodeGenFunctions.writeArgsJava(sb, childEnv, true, args, functionInstance);
          sb.append(")");
          break;
        case InjectNameThenExpressionAndArgs:
          sb.append(functionInstance.javaFunction);
          sb.append("(");
          TyType thisType = functionInstance.getThisType();
          if (thisType != null && functionInstance.castArgs) {
            sb.append("(").append(thisType.getJavaBoxType(environment)).append(") ");
          }
          expression.writeJava(sb, childEnv);
          CodeGenFunctions.writeArgsJava(sb, childEnv, false, args, functionInstance);
          sb.append(")");
          break;
        case None:
        default:
          expression.writeJava(sb, childEnv);
          break;
      }
      if (functionInstance.castReturn) {
        sb.append(")");
      }
    }
  }

  private int buildLatentApplyClassInstance(final Environment environment) {
    final var id = environment.autoVariable();
    final var sb = new StringBuilderWithTabs();
    if (aggregateOutputType != null) {
      sb.append("private class __CLOSURE_Apply").append("_" + id).append(" implements Function<").append(aggregateInputType.getJavaBoxType(environment)).append(",").append(aggregateOutputType.getJavaBoxType(environment)).append("> {").tabUp().writeNewline();
    } else {
      sb.append("private class __CLOSURE_Apply").append("_" + id).append(" implements Consumer<").append(aggregateInputType.getJavaBoxType(environment)).append("> {").tabUp().writeNewline();
    }
    for (final Map.Entry<String, TyType> entry : closureTyTypes.entrySet()) {
      sb.append("private ").append(entry.getValue().getJavaConcreteType(environment)).append(" ").append(entry.getKey()).append(";").writeNewline();
    }
    sb.append("private __CLOSURE_Apply").append("_" + id + "(");
    var first = true;
    for (final Map.Entry<String, TyType> entry : closureTyTypes.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append(entry.getValue().getJavaConcreteType(environment)).append(" ").append(entry.getKey());
    }
    sb.append(") {").writeNewline();
    for (final Map.Entry<String, TyType> entry : closureTyTypes.entrySet()) {
      sb.append("this.").append(entry.getKey()).append(" = ").append(entry.getKey()).append(";").writeNewline();
    }
    sb.append("}").writeNewline();
    sb.append("@Override").writeNewline();
    if (aggregateOutputType != null) {
      sb.append("public ").append(aggregateOutputType.getJavaBoxType(environment)).append(" apply(").append(aggregateInputType.getJavaBoxType(environment)).append(" __item) {").tabUp().writeNewline();
      sb.append("return __item.").append(functionInstance.javaFunction);
      sb.append("(");
      final var temp = new StringBuilder();
      CodeGenFunctions.writeArgsJava(temp, environment, true, args, functionInstance);
      sb.append(temp.toString());
      sb.append(");").tabDown().writeNewline();
    } else {
      sb.append("public void accept(").append(aggregateInputType.getJavaBoxType(environment)).append(" __item) {").tabUp().writeNewline();
      sb.append("__item.").append(functionInstance.javaFunction);
      sb.append("(");
      final var temp = new StringBuilder();
      CodeGenFunctions.writeArgsJava(temp, environment, true, args, functionInstance);
      sb.append(temp.toString());
      sb.append(");").tabDown().writeNewline();
    }
    sb.append("}").tabDown().writeNewline();
    sb.append("}").writeNewline();
    latentLines = sb.toLines();
    return id;
  }

  @Override
  public void writeLatentJava(final StringBuilderWithTabs sb) {
    if (latentLines != null) {
      for (final String line : latentLines) {
        sb.append(line).writeNewline();
      }
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
    for (var arg : args) {
      arg.item.free(environment);
    }
  }
}
