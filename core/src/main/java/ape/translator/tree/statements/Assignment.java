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
package ape.translator.tree.statements;

import ape.translator.codegen.CodeGenIngestion;
import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.operands.AssignmentOp;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.LocalTypeAssignmentResult;
import ape.translator.tree.types.checking.properties.CanAssignResult;
import ape.translator.tree.types.natives.TyNativeArray;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.natives.TyNativeList;

import java.util.function.Consumer;

/** left {=,<-} right */
public class Assignment extends Statement {
  public final Expression expression;
  public final boolean inForLoop;
  public final AssignmentOp op;
  public final Token opToken;
  public final Expression ref;
  public final Token trailingToken;
  public final Token asToken;
  public final Token ingestionDefine;
  private LocalTypeAssignmentResult result;

  public Assignment(final Expression ref, final Token opToken, final Expression expression, Token asToken, Token ingestionDefine, final Token trailingToken, final boolean inForLoop) {
    this.ref = ref;
    this.expression = expression;
    this.opToken = opToken;
    op = AssignmentOp.fromText(opToken.text);
    this.inForLoop = inForLoop;
    this.trailingToken = trailingToken;
    ingest(ref);
    ingest(expression);
    if (trailingToken != null) {
      ingest(trailingToken);
    }
    this.asToken = asToken;
    this.ingestionDefine = ingestionDefine;
    if (this.ingestionDefine != null) {
      ingest(ingestionDefine);
    }
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    ref.emit(yielder);
    yielder.accept(opToken);
    expression.emit(yielder);
    if (asToken != null) {
      yielder.accept(asToken);
      yielder.accept(ingestionDefine);
    }
    if (trailingToken != null) {
      yielder.accept(trailingToken);
    }
  }

  @Override
  public void format(Formatter formatter) {
    ref.format(formatter);
    expression.format(formatter);
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    result = new LocalTypeAssignmentResult(environment, ref, expression);
    switch (op) {
      case IngestFrom:
        result.ingest();
        if (ingestionDefine != null) {
          final var refType = ref.getCachedType();
          final var exprType = expression.getCachedType();
          boolean isArray = environment.rules.IngestionRightSideRequiresIteration(exprType);
          environment.rules.IsTable(refType, false);
          if (refType != null && exprType != null) {
            if (refType.behavior.isReadOnly) {
              environment.document.createError(DocumentPosition.sum(refType, exprType), String.format("'%s' is unable to accept an ingestion of '%s'.", refType.getAdamaType(), exprType.getAdamaType()));
            }
            if (isArray) {
              environment.define(ingestionDefine.text, new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, ingestionDefine, ingestionDefine), ingestionDefine), true, this);
            } else {
              environment.define(ingestionDefine.text, new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, ingestionDefine, ingestionDefine), true, this);
            }
          }
        }
        break;
      case Set:
        result.set();
        break;
    }
    return ControlFlow.Open;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    if (result == null || result.bad()) {
      return;
    }
    if (result.assignResult == CanAssignResult.YesWithNativeOp) {
      ref.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Assignment));
      sb.append(" ").append(op.js).append(" ");
      expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      if (!inForLoop) {
        sb.append(";");
      }
    } else if (result.assignResult == CanAssignResult.YesWithSetter) {
      ref.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Assignment));
      sb.append(op.notNative).append("(");
      expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(");");
    } else if (result.assignResult == CanAssignResult.YesWithMakeThenSetter) {
      ref.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Assignment));
      sb.append(".make()");
      sb.append(op.notNative).append("(");
      expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(");");
    } else if ((result.assignResult == CanAssignResult.YesWithTransformSetter || result.assignResult == CanAssignResult.YesWithTransformThenMakeSetter) && result.ltype != null) {
      final var varToCache = "_auto_" + environment.autoVariable();
      final var varToIterate = "_auto_" + environment.autoVariable();
      final var embeddedType = ((TyNativeList) result.ltype).getEmbeddedType(environment);
      if (embeddedType != null) {
        sb.append(result.ltype.getJavaConcreteType(environment)).append(" ").append(varToCache).append(" = ");
        ref.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Assignment));
        sb.append(";").writeNewline();
        sb.append("for (").append(embeddedType.getJavaConcreteType(environment)).append(" ").append(varToIterate).append(" : ").append(varToCache).append(") {").tabUp().writeNewline();
        sb.append(varToIterate);
        if (result.assignResult == CanAssignResult.YesWithTransformThenMakeSetter) {
          sb.append(".make()");
        }
        sb.append(op.notNative).append("(");
        expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
        sb.append(");").tabDown().writeNewline();
        sb.append("}").writeNewline();
      }
    } else if (result.assignResult == CanAssignResult.YesWithIngestionCodeGen) {
      CodeGenIngestion.writeJava(sb, environment, this, ingestionDefine);
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    ref.free(environment);
    expression.free(environment);
  }
}
