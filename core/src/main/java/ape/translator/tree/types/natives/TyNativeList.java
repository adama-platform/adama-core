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
package ape.translator.tree.types.natives;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.constants.EmptyListConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetLists;
import ape.translator.tree.types.checking.ruleset.RuleSetMaybe;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.natives.functions.TyNativeAggregateFunctional;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.traits.assign.AssignmentViaNativeOnlyForSet;
import ape.translator.tree.types.traits.details.*;
import ape.translator.tree.types.traits.details.*;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyNativeList extends TyType implements //
    DetailContainsAnEmbeddedType, //
    DetailNativeDeclarationIsNotStandard, //
    AssignmentViaNativeOnlyForSet, //
    DetailHasDeltaType, //
    DetailIndexLookup, //
    DetailComputeRequiresGet, //
    DetailInventDefaultValueExpression, //
    DetailTypeHasMethods {
  public final TyType elementType;
  public final Token listToken;
  public final Token readonlyToken;
  public final TokenizedItem<TyType> tokenElementType;

  public TyNativeList(final TypeBehavior behavior, final Token readonlyToken, final Token listToken, final TokenizedItem<TyType> tokenElementType) {
    super(behavior);
    this.readonlyToken = readonlyToken;
    this.listToken = listToken;
    elementType = tokenElementType.item;
    this.tokenElementType = tokenElementType;
    ingest(listToken);
    ingest(elementType);
  }

  public static TyNativeList WRAP(final TyType type) {
    return new TyNativeList(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem<>(type));
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    if (readonlyToken != null) {
      yielder.accept(readonlyToken);
    }
    yielder.accept(listToken);
    tokenElementType.emitBefore(yielder);
    elementType.emit(yielder);
    tokenElementType.emitAfter(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    elementType.format(formatter);
  }

  @Override
  public String getAdamaType() {
    return String.format("list<%s>", elementType.getAdamaType());
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    final var resolved = getEmbeddedType(environment);
    return String.format("NtList<%s>", resolved.getJavaBoxType(environment));
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeList(newBehavior, readonlyToken, listToken, new TokenizedItem<>(elementType.makeCopyWithNewPosition(position, newBehavior))).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    elementType.typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_list");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    elementType.writeTypeReflectionJson(writer, source);
    writer.endObject();
  }

  @Override
  public TyType getEmbeddedType(final Environment environment) {
    return environment.rules.Resolve(elementType, false);
  }

  @Override
  public String getDeltaType(final Environment environment) {
    final var resolved = getEmbeddedType(environment);
    if (resolved instanceof TyReactiveRecord) {
      return "DRecordList<" + ((TyReactiveRecord) resolved).getDeltaType(environment) + ">";
    }
    if (resolved instanceof TyNativeMessage && ((TyNativeMessage) resolved).hasUniqueId()) {
      return "DRecordList<" + ((TyNativeMessage) resolved).getDeltaType(environment) + ">";
    }
    return "DList<" + ((DetailHasDeltaType) resolved).getDeltaType(environment) + ">";
  }

  @Override
  public IndexLookupStyle getLookupStyle(final Environment environment) {
    return IndexLookupStyle.ExpressionLookupMethod;
  }

  @Override
  public String getPatternWhenValueProvided(final Environment environment) {
    return "%s";
  }

  @Override
  public String getStringWhenValueNotProvided(final Environment environment) {
    final var resolved = getEmbeddedType(environment);
    return String.format("new EmptyNtList<%s>()", resolved.getJavaBoxType(environment));
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("size".equals(name)) {
      return new TyNativeFunctional("size", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("size", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, listToken).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("toArray".equals(name)) {
      final var foi = new FunctionOverloadInstance("toArray", new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, tokenElementType.item, null).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL);
      TyType elementType = environment.rules.Resolve(tokenElementType.item, true);
      if (elementType != null) {
        foi.hiddenSuffixArgs.add("(Integer __n) -> (Object) (new " + elementType.getJavaConcreteType(environment) + "[__n])");
      }
      return new TyNativeFunctional("toArray", FunctionOverloadInstance.WRAP(foi), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("flatten".equals(name)) {
      TyType listElementType = getEmbeddedType(environment);
      if (RuleSetLists.IsNativeList(environment, listElementType, true) && listElementType instanceof DetailContainsAnEmbeddedType) {
        TyType itemType = ((DetailContainsAnEmbeddedType) listElementType).getEmbeddedType(environment);
        TyType resultType = new TyNativeList(TypeBehavior.ReadOnlyNativeValue,  null, null, new TokenizedItem<>(itemType)).withPosition(this);
        final var foi = new FunctionOverloadInstance("LibLists.flatten", resultType, new ArrayList<>(), FunctionPaint.READONLY_NORMAL);
        return new TyNativeFunctional("LibLists.flatten", FunctionOverloadInstance.WRAP(foi), FunctionStyleJava.InjectNameThenExpressionAndArgs);
      }
    }
    if ("manifest".equals(name)) {
      TyType listElementType = getEmbeddedType(environment);
      if (RuleSetMaybe.IsMaybe(environment, listElementType, true) && listElementType instanceof DetailContainsAnEmbeddedType) {
        TyType typeInMaybe = ((DetailContainsAnEmbeddedType) listElementType).getEmbeddedType(environment);
        TyType resultType = new TyNativeList(TypeBehavior.ReadOnlyNativeValue,  null, null, new TokenizedItem<>(typeInMaybe)).withPosition(this);
        final var foi = new FunctionOverloadInstance("LibLists.manifest", resultType, new ArrayList<>(), FunctionPaint.READONLY_CAST);
        foi.setThisType(this);
        return new TyNativeFunctional("LibLists.manifest", FunctionOverloadInstance.WRAP(foi), FunctionStyleJava.InjectNameThenExpressionAndArgs);
      }
    }
    if ("reverse".equals(name)) {
      TyType listElementType = getEmbeddedType(environment);
      TyType resultType = new TyNativeList(TypeBehavior.ReadOnlyNativeValue,  null, null, new TokenizedItem<>(listElementType)).withPosition(this);
      final var foi = new FunctionOverloadInstance("LibLists.reverse", resultType, new ArrayList<>(), FunctionPaint.READONLY_NORMAL);
      return new TyNativeFunctional("LibLists.reverse", FunctionOverloadInstance.WRAP(foi), FunctionStyleJava.InjectNameThenExpressionAndArgs);
    }
    if ("skip".equals(name)) {
      TyType listElementType = getEmbeddedType(environment);
      TyType resultType = new TyNativeList(TypeBehavior.ReadOnlyNativeValue,  null, null, new TokenizedItem<>(listElementType)).withPosition(this);
      ArrayList<TyType> args = new ArrayList<>();
      args.add(new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null));
      final var foi = new FunctionOverloadInstance("LibLists.skip", resultType, args, FunctionPaint.READONLY_NORMAL);
      return new TyNativeFunctional("LibLists.skip", FunctionOverloadInstance.WRAP(foi), FunctionStyleJava.InjectNameThenExpressionAndArgs);
    }
    if ("drop".equals(name)) {
      TyType listElementType = getEmbeddedType(environment);
      TyType resultType = new TyNativeList(TypeBehavior.ReadOnlyNativeValue,  null, null, new TokenizedItem<>(listElementType)).withPosition(this);
      ArrayList<TyType> args = new ArrayList<>();
      args.add(new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null));
      final var foi = new FunctionOverloadInstance("LibLists.drop", resultType, args, FunctionPaint.READONLY_NORMAL);
      return new TyNativeFunctional("LibLists.drop", FunctionOverloadInstance.WRAP(foi), FunctionStyleJava.InjectNameThenExpressionAndArgs);
    }
    TyNativeFunctional extensionBeforeAggregate = environment.state.globals.findExtension(this, name);
    if (extensionBeforeAggregate != null) {
      return extensionBeforeAggregate;
    }
    final var embedType = getEmbeddedType(environment);
    if (embedType != null && embedType instanceof DetailTypeHasMethods) {
      final var childMethod = ((DetailTypeHasMethods) embedType).lookupMethod(name, environment);
      if (childMethod != null) {
        return new TyNativeAggregateFunctional(embedType, childMethod);
      }
    }
    return null;
  }

  @Override
  public TyType typeAfterGet(final Environment environment) {
    if (elementType instanceof DetailComputeRequiresGet) {
      return new TyNativeList(behavior, readonlyToken, listToken, new TokenizedItem<>(((DetailComputeRequiresGet) elementType).typeAfterGet(environment)));
    } else {
      return this;
    }
  }

  @Override
  public Expression inventDefaultValueExpression(DocumentPosition forWhatExpression) {
    return new EmptyListConstant(null, tokenElementType);
  }
}
