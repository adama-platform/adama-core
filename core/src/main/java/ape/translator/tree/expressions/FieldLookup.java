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

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.functions.TyNativeAggregateFunctional;
import ape.translator.tree.types.natives.functions.TyNativeFunctionInternalFieldReplacement;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.traits.IsStructure;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.function.Consumer;

/** field/method look up ($e.field) */
public class FieldLookup extends Expression {
  public final Token dotToken;
  public final Expression expression;
  public final String fieldName;
  public final Token fieldNameToken;
  private boolean addGet;
  private TyType aggregateType;
  private boolean isGlobalObject;
  private boolean makeList;
  private boolean onlyExpression;
  private String overrideFieldName;
  private boolean requiresMaybeUnpack;
  private boolean doubleMaybeUnpack;
  private String maybeCastType;
  private TyType elementTypeIfList;
  private boolean jsonDeref;

  /**
   * @param expression the expression to evaluate
   * @param fieldNameToken the field to look up
   */
  public FieldLookup(final Expression expression, final Token dotToken, final Token fieldNameToken) {
    this.expression = expression;
    this.dotToken = dotToken;
    this.fieldNameToken = fieldNameToken;
    fieldName = fieldNameToken.text;
    this.ingest(expression);
    this.ingest(fieldNameToken);
    addGet = false;
    makeList = false;
    overrideFieldName = null;
    isGlobalObject = false;
    requiresMaybeUnpack = false;
    maybeCastType = null;
    elementTypeIfList = null;
    jsonDeref = false;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    expression.emit(yielder);
    yielder.accept(dotToken);
    yielder.accept(fieldNameToken);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }

  private void enforceSpecialIDReadonly(Environment environment) {
    if ("id".equals(fieldName) && environment.state.isContextAssignment()) {
      environment.document.createError(this, "'id' is a special readonly field");
    }
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    var eType = expression.typing(environment, null);
    eType = environment.rules.ResolvePtr(eType, false);

    if (eType != null) {
      // global object, find the function
      if (eType instanceof TyNativeGlobalObject) {
        isGlobalObject = true;
        final var func = ((TyNativeGlobalObject) eType).lookupMethod(fieldName, environment);
        if (func == null) {
          environment.document.createError(this, String.format("Global '%s' lacks '%s'", ((TyNativeGlobalObject) eType).globalName, fieldName));
          return null;
        }
      }
      if (eType instanceof TyNativePair) {
        TyNativePair ePair = (TyNativePair) eType;
        TyType resultType;
        if ("key".equals(fieldName)) {
          resultType = ePair.domainType;
        } else if ("value".equals(fieldName)) {
          resultType = ePair.rangeType;
        } else {
          environment.document.createError(this, String.format("Pair '%s' does not have '%s' field, and only supports 'key' and 'value'", eType.getAdamaType(), fieldName));
          return null;
        }
        resultType = environment.rules.Resolve(resultType, false);
        if (resultType instanceof DetailComputeRequiresGet && environment.state.isContextComputation()) {
          resultType = ((DetailComputeRequiresGet) resultType).typeAfterGet(environment);
          addGet = true;
        }
        return resultType;
      }
      if (eType instanceof TyInternalReadonlyClass) {
        return ((TyInternalReadonlyClass) eType).getLookupType(environment, fieldName);
      }
      if (eType instanceof TyNativeList) {
        elementTypeIfList = environment.rules.ResolvePtr(((TyNativeList) eType).getEmbeddedType(environment), false);
        if (elementTypeIfList != null && elementTypeIfList instanceof IsStructure) {
          final var fd = ((IsStructure) elementTypeIfList).storage().fields.get(fieldName);
          if (fd != null) {
            enforceSpecialIDReadonly(environment);
            aggregateType = fd.type;
            makeList = true;
            if (aggregateType instanceof DetailComputeRequiresGet && environment.state.isContextComputation()) {
              addGet = true;
              aggregateType = ((DetailComputeRequiresGet) aggregateType).typeAfterGet(environment);
            }
            if (aggregateType != null) {
              return new TyNativeList(TypeBehavior.ReadWriteWithSetGet, null, null, new TokenizedItem<>(aggregateType.makeCopyWithNewPosition(this, aggregateType.behavior))).withPosition(this);
            }
          }
        }
      }
      if (eType instanceof DetailTypeHasMethods) {
        final var functional = ((DetailTypeHasMethods) eType).lookupMethod(fieldName, environment);
        if (functional != null) {
          onlyExpression = functional instanceof TyNativeAggregateFunctional || functional.style.useOnlyExpressionInLookup;
          if (functional instanceof TyNativeFunctionInternalFieldReplacement) {
            overrideFieldName = functional.name;
          }
          return functional;
        }
      }
      TyType originalType = eType;
      if (environment.rules.IsMaybe(eType, true)) {
        requiresMaybeUnpack = true;
        eType = ((DetailContainsAnEmbeddedType) eType).getEmbeddedType(environment);
      }
      if (eType instanceof TyNativeJson) {
        maybeCastType = "NtJson";
        jsonDeref = true;
        return originalType;
      }
      if (eType instanceof IsStructure) {
        if (!environment.state.isContextComputation() && eType.behavior == TypeBehavior.ReadOnlyNativeValue) {
          environment.document.createError(this, String.format("The field '%s' is on a readonly message", fieldName));
        }
        if (eType instanceof TyReactiveRecord) {
          enforceSpecialIDReadonly(environment);
        }
        final var hrs = (IsStructure) eType;
        final var fd = hrs.storage().fields.get(fieldName);
        if (fd != null) {
          if ("__ViewerType".equals(((IsStructure) eType).storage().name.text)) {
            environment.registerViewerField(fieldName);
          }
          TyType actualType = environment.rules.Resolve(fd.type, false);
          if (environment.rules.IsMaybe(actualType, true) && requiresMaybeUnpack) {
            doubleMaybeUnpack = true;
            actualType = ((DetailContainsAnEmbeddedType) actualType).getEmbeddedType(environment);
          }
          if (actualType != null) {
            TyType typeToReturn;
            if (actualType instanceof DetailComputeRequiresGet && environment.state.isContextComputation()) {
              addGet = true;
              typeToReturn = ((DetailComputeRequiresGet) actualType).typeAfterGet(environment);
            } else {
              typeToReturn = actualType.makeCopyWithNewPosition(this, actualType.behavior);
            }
            if (requiresMaybeUnpack) {
              maybeCastType = eType.getJavaBoxType(environment);
              return new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("MAYBE"), new TokenizedItem<>(typeToReturn));
            } else {
              return typeToReturn;
            }
          }
        }
      }
      environment.document.createError(this, String.format("Type '%s' lacks field '%s'", eType.getAdamaType(), fieldName));
    }
    return null;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    if (passedTypeChecking() && !isGlobalObject) {
      expression.writeJava(sb, environment);
      if (onlyExpression) {
        return;
      }
      sb.append(".");
      var fieldNameToUse = fieldName;
      if (overrideFieldName != null) {
        fieldNameToUse = overrideFieldName;
      }
      String finalNameToUse = fieldNameToUse;
      Runnable injectName = () -> {
        if (jsonDeref) {
          sb.append("deref(\"");
        }
        sb.append(finalNameToUse);
        if (jsonDeref) {
          sb.append("\")");
        }
      };
      if (requiresMaybeUnpack) {
        sb.append("unpack").append(doubleMaybeUnpack ? "Transfer" : "").append("((__item) -> ((").append(maybeCastType).append(")").append(" __item).");
        injectName.run();
        if (addGet) {
          sb.append(".get()");
        }
        sb.append(")");
      } else if (makeList && aggregateType != null) {
        sb.append("transform((").append(elementTypeIfList.getJavaBoxType(environment)).append(" __item) -> (").append(aggregateType.getJavaBoxType(environment)).append(") (__item.");
        injectName.run();
        if (addGet) {
          sb.append(".get()");
        }
        sb.append("))");
      } else {
        injectName.run();
        if (addGet) {
          sb.append(".get()");
        }
      }
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    environment.require("::" + fieldName);
    expression.free(environment);
  }
}
