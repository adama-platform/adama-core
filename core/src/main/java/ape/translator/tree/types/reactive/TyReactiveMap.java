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
package ape.translator.tree.types.reactive;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetTable;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.CanBeMapDomain;
import ape.translator.tree.types.traits.DetailNeedsSettle;
import ape.translator.tree.types.traits.IsKillable;
import ape.translator.tree.types.traits.IsMap;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyReactiveMap extends TyType implements //
    DetailTypeHasMethods, //
    DetailNeedsSettle, //
    IsMap, //
    IsKillable, //
    DetailContainsAnEmbeddedType, //
    DetailHasDeltaType {
  public final boolean readonly;
  public final Token closeThing;
  public final Token commaToken;
  public final TyType domainType;
  public final Token mapToken;
  public final Token openThing;
  public final TyType rangeType;

  public TyReactiveMap(boolean readonly, final Token mapToken, final Token openThing, final TyType domainType, final Token commaToken, final TyType rangeType, final Token closeThing) {
    super(readonly ? TypeBehavior.ReadOnlyWithGet : TypeBehavior.ReadWriteWithSetGet);
    this.readonly = readonly;
    this.mapToken = mapToken;
    this.openThing = openThing;
    this.domainType = domainType;
    this.commaToken = commaToken;
    this.rangeType = rangeType;
    this.closeThing = closeThing;
    ingest(mapToken);
    ingest(closeThing);
  }

  @Override
  public void format(Formatter formatter) {
    domainType.format(formatter);
    rangeType.format(formatter);
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    yielder.accept(mapToken);
    yielder.accept(openThing);
    domainType.emit(yielder);
    yielder.accept(commaToken);
    rangeType.emit(yielder);
    yielder.accept(closeThing);
  }

  @Override
  public String getAdamaType() {
    return "r<map<" + domainType.getAdamaType() + "," + rangeType.getAdamaType() + ">>";
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return "RxMap<" + getDomainType(environment).getJavaBoxType(environment) + "," + getRangeType(environment).getJavaBoxType(environment) + ">";
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveMap(readonly, mapToken, openThing, domainType, commaToken, rangeType, closeThing).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    domainType.typing(environment);
    rangeType.typing(environment);
    final var resolvedDomainType = environment.rules.Resolve(domainType, false);
    if (resolvedDomainType != null && !(resolvedDomainType instanceof CanBeMapDomain)) {
      environment.document.createError(this, String.format("The domain type '%s' is not an appropriate.", resolvedDomainType.getAdamaType()));
    }
    final var resolvedRangeType = environment.rules.Resolve(rangeType, false);
    if (RuleSetTable.IsTable(environment, resolvedRangeType, true)) {
      environment.document.createError(this, String.format("The range type '%s' is not an appropriate for a map.", resolvedRangeType.getAdamaType()));
    }
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_map");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("domain");
    domainType.writeTypeReflectionJson(writer, source);
    writer.writeObjectFieldIntro("range");
    rangeType.writeTypeReflectionJson(writer, source);
    writer.endObject();
  }

  @Override
  public TyType getDomainType(final Environment environment) {
    return environment.rules.Resolve(domainType, false);
  }

  @Override
  public TyType getRangeType(final Environment environment) {
    return environment.rules.Resolve(rangeType, false);
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("size".equals(name)) {
      return new TyNativeFunctional("size", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("size", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, mapToken).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("remove".equals(name)) {
      ArrayList<TyType> args = new ArrayList<>();
      args.add(domainType);
      return new TyNativeFunctional("remove", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("remove", new TyNativeVoid().withPosition(this), args, FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("has".equals(name)) {
      ArrayList<TyType> args = new ArrayList<>();
      args.add(domainType);
      return new TyNativeFunctional("has", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("has", new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, mapToken).withPosition(this), args, FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("clear".equals(name)) {
      return new TyNativeFunctional("clear", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("clear", new TyNativeVoid().withPosition(this), new ArrayList<>(), FunctionPaint.NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    return null;
  }

  @Override
  public String getDeltaType(Environment environment) {
    String domainBox = getDomainType(environment).getJavaBoxType(environment);
    var range = getRangeType(environment);
    if (range instanceof DetailComputeRequiresGet) {
      range = ((DetailComputeRequiresGet) range).typeAfterGet(environment);
    }
    return "DMap<" + domainBox + "," + ((DetailHasDeltaType) range).getDeltaType(environment) + ">";
  }

  @Override
  public TyType getEmbeddedType(Environment environment) {
    return new TyNativePair(TypeBehavior.ReadOnlyNativeValue, null, null, null, environment.rules.Resolve(domainType, false), null, environment.rules.Resolve(rangeType, false), null);
  }
}
