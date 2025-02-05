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
package ape.translator.codegen;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.FieldLookup;
import ape.translator.tree.expressions.Lookup;
import ape.translator.tree.statements.Assignment;
import ape.translator.tree.statements.Block;
import ape.translator.tree.statements.control.MegaIf;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeArray;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.IsMap;
import ape.translator.tree.types.traits.IsStructure;

import java.util.Map;

/**
 * responsible for the recursive "ingestion operator" (<-) which is how data gets into the tables or
 * objects
 */
public class CodeGenIngestion {
  public static void writeJava(final StringBuilderWithTabs sb, final Environment original, final Assignment assignment, Token exportIdsAs) {
    final var refType = assignment.ref.getCachedType();
    final var exprType = assignment.expression.getCachedType();
    boolean isArray = original.rules.IngestionRightSideRequiresIteration(exprType);
    boolean isMap = original.rules.IsMap(exprType);
    boolean hasId = original.rules.IngestionLeftSideRequiresBridgeCreate(refType);

    if (exportIdsAs != null) {
      if (isArray) {
        sb.append("int[] ").append(exportIdsAs.text).append(";").writeNewline();
        original.define(exportIdsAs.text, new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, exportIdsAs, exportIdsAs), exportIdsAs), true, assignment);
      } else {
        sb.append("int ").append(exportIdsAs.text).append(";").writeNewline();
        sb.append("// EXPORT:" + exportIdsAs.text).writeNewline();
        original.define(exportIdsAs.text, new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, exportIdsAs, exportIdsAs), true, assignment);
      }
    }

    final var environment = original.scope();
    sb.append("{").tabUp().writeNewline();

    String generatedIntArrayVar = null;
    if (exportIdsAs != null && isArray) {
      generatedIntArrayVar = "_AutoVarArrId" + environment.autoVariable();
      sb.append("ArrayList<Integer> ").append(generatedIntArrayVar).append(" = new ArrayList<Integer>();").writeNewline();
    }

    final var generatedRefVariable = "_AutoRef" + environment.autoVariable();
    sb.append(refType.getJavaConcreteType(environment)).append(" ").append(generatedRefVariable).append(" = ");
    assignment.ref.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Assignment));
    sb.append(";").writeNewline();
    final var autoVar = environment.autoVariable();
    var generateAssignVar = generatedRefVariable;
    var generateAssignType = refType;
    if (isMap) {
      TyType inputDomainType = environment.rules.Resolve(((IsMap) exprType).getDomainType(environment), true);
      TyType inputRangeType = environment.rules.Resolve(((IsMap) exprType).getRangeType(environment), true);
      TyType outputRangeType = environment.rules.Resolve(((IsMap) refType).getRangeType(environment), true);
      final var generatedExprVariableEntry = "_AutoEntry" + autoVar;
      sb.append("for (NtPair<").append(inputDomainType.getJavaBoxType(environment)).append(",").append(inputRangeType.getJavaBoxType(environment)).append("> ").append(generatedExprVariableEntry);
      sb.append(" : ");
      assignment.expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(") {").tabUp().writeNewline();
      generateAssignType = outputRangeType.makeCopyWithNewPosition(refType, TypeBehavior.ReadWriteNative);
      final var generatedExprVariable = "_AutoExpr" + autoVar;
      generateAssignVar = "_AutoRefElement" + autoVar;
      sb.append(inputRangeType.getJavaConcreteType(environment)).append(" ").append(generatedExprVariable).append(" = ").append(generatedExprVariableEntry).append(".value;").writeNewline();
      sb.append(generateAssignType.getJavaConcreteType(environment)).append(" ").append(generateAssignVar).append(" = ");
      sb.append(generatedRefVariable).append(".getOrCreate(").append(generatedExprVariableEntry).append(".key);").writeNewline();
      finish(environment, sb, generateAssignType, generateAssignVar, inputRangeType, generatedExprVariable);
      sb.append("}").tabDown().writeNewline();
    } else if (isArray) {
      final var iterateElementType = environment.rules.ExtractEmbeddedType(exprType, true);
      final var generatedExprVariable = "_AutoElement" + autoVar;
      sb.append("for (");
      sb.append(iterateElementType.getJavaConcreteType(environment)).append(" ").append(generatedExprVariable).append(" : ");
      assignment.expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(") {").tabUp().writeNewline();
      if (hasId) {
        generateAssignType = environment.rules.Resolve(environment.rules.ExtractEmbeddedType(refType, true), true).makeCopyWithNewPosition(refType, TypeBehavior.ReadWriteNative);
        generateAssignVar = "_CreateRef" + autoVar;
        sb.append(generateAssignType.getJavaConcreteType(environment)).append(" ").append(generateAssignVar).append(" = ").append(generatedRefVariable).append(".make();").writeNewline();
        if (exportIdsAs != null && isArray) {
          sb.append(generatedIntArrayVar).append(".add(").append(generateAssignVar).append(".id.get());").writeNewline();
        }
      }
      finish(environment, sb, generateAssignType, generateAssignVar, iterateElementType, generatedExprVariable);
      sb.append("}");
      if (exportIdsAs != null) {
        sb.writeNewline();
        sb.append(exportIdsAs.text).append(" = Utility.convertIntegerArrayList(").append(generatedIntArrayVar).append(");");
      }
      sb.tabDown().writeNewline();
    } else {
      final var generatedExprVariable = "_AutoExpr" + environment.autoVariable();
      sb.append(exprType.getJavaConcreteType(environment)).append(" ").append(generatedExprVariable).append(" = ");
      assignment.expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(";").writeNewline();
      if (hasId) {
        generateAssignType = environment.rules.Resolve(environment.rules.ExtractEmbeddedType(refType, true), true).makeCopyWithNewPosition(refType, TypeBehavior.ReadWriteNative);
        generateAssignVar = "_CreateRef" + autoVar;
        sb.append(generateAssignType.getJavaConcreteType(environment)).append(" ").append(generateAssignVar).append(" = ").append(generatedRefVariable).append(".make();").writeNewline();
        if (exportIdsAs != null) {
          sb.append(exportIdsAs.text).append(" = ").append(generateAssignVar).append(".id.get();").writeNewline();
        }
      }
      finish(environment, sb, generateAssignType, generateAssignVar, exprType, generatedExprVariable);
    }
    sb.append("}");
  }

  private static void finish(final Environment environment, final StringBuilderWithTabs sb, final TyType assignTypeRaw, final String assignVar, final TyType elementTypeRaw, final String elementVar) {
    if (assignVar != null && elementTypeRaw != null) {
      TyType assignType = environment.rules.ResolvePtr(assignTypeRaw, false);
      TyType elementType = environment.rules.ResolvePtr(elementTypeRaw, false);
      if (environment.rules.IsStructure(assignType, true) && environment.rules.IsStructure(elementType, true)) {
        var countDownUntilTab = ((IsStructure) elementType).storage().fields.size();
        environment.define(assignVar, assignType, false, assignType);
        environment.define(elementVar, elementType, false, elementType);
        if (countDownUntilTab > 0) {
          if (!environment.state.hasNoCost()) {
            sb.append("__code_cost += ").append(Integer.toString(((IsStructure) elementType).storage().fields.size() + 1)).append(";").writeNewline();
          }
          for (final Map.Entry<String, FieldDefinition> entryType : ((IsStructure) elementType).storage().fields.entrySet()) {
            final var fd = ((IsStructure) assignType).storage().fields.get(entryType.getKey());
            if (fd != null) {
              boolean isLeftMessageType = environment.rules.IsNativeMessage(assignType, true);
              if ("id".equals(fd.name) && !isLeftMessageType) {
                sb.append("/* id field skipped */");
              } else {
                final var leftAssignType = ((IsStructure) assignType).storage().fields.get(entryType.getKey()).type;
                TyType rightType = entryType.getValue().type;
                Expression rightExpr = new FieldLookup(new Lookup(Token.WRAP(elementVar)), null, entryType.getValue().nameToken);
                final var op = environment.rules.IngestionLeftElementRequiresRecursion(leftAssignType) ? "<-" : "=";
                if (environment.rules.IsMaybe(rightType, true)) {
                  // the right side is a maybe, so let's unwrap it
                  Token unwrapMaybe = Token.WRAP("__unwrap_" + fd.name + "_" + environment.autoVariable());
                  Block assBlock = new Block(null);
                  assBlock.add(new Assignment( //
                      new FieldLookup(new Lookup(Token.WRAP(assignVar)), null, entryType.getValue().nameToken), Token.WRAP(op), //
                      new Lookup(unwrapMaybe), null, null, null, false));
                  MegaIf _if = new MegaIf(null, new MegaIf.Condition(null, rightExpr, null, unwrapMaybe, null), assBlock);
                  _if.typing(environment);
                  _if.writeJava(sb, environment);
                } else {
                  final var ass = new Assignment( //
                      new FieldLookup(new Lookup(Token.WRAP(assignVar)), null, entryType.getValue().nameToken), Token.WRAP(op), //
                      rightExpr, null, null, null, false);
                  ass.typing(environment);
                  ass.writeJava(sb, environment);
                }
              }
            } else {
              sb.append("// N/A ").append(entryType.getKey());
            }
            if (--countDownUntilTab == 0) {
              if (((IsStructure) assignType).storage().hasPostIngestion()) {
                sb.writeNewline();
                sb.append(assignVar).append(".__postIngest();");
              }
              sb.tabDown();
            }
            sb.writeNewline();
          }
        } else {
          if (!environment.state.hasNoCost()) {
            if (((IsStructure) assignType).storage().hasPostIngestion()) {
              sb.append(assignVar).append(".__postIngest();").writeNewline();
            }
            sb.append("__code_cost += 1;");
          } else {
            if (((IsStructure) assignType).storage().hasPostIngestion()) {
              sb.append(assignVar).append(".__postIngest();");
            } else {
              sb.append("// NOTHING TO INGEST");
            }
          }
          sb.tabDown().writeNewline();
        }
      } else {
        final var ass = new Assignment( //
            new Lookup(Token.WRAP(assignVar)), Token.WRAP("="), //
            new Lookup(Token.WRAP(elementVar)), null, null, null, false);

        environment.define(assignVar, environment.rules.Resolve(assignType, true), false, assignType);
        environment.define(elementVar, environment.rules.Resolve(elementType, true), false, elementType);
        ass.typing(environment);
        ass.writeJava(sb, environment);
        sb.tabDown().writeNewline();
      }
    }
  }
}
