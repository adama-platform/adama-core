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
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetTable;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.DetailNeedsSettle;
import ape.translator.tree.types.traits.IsKillable;
import ape.translator.tree.types.traits.IsFauxList;
import ape.translator.tree.types.traits.IsReactiveValue;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyReactiveList extends TyType implements //
    DetailTypeHasMethods, //
    DetailNeedsSettle, //
        IsFauxList, //
    IsKillable, //
    DetailHasDeltaType {
  public final boolean readonly;
  public final Token listToken;
  public final TokenizedItem<TyType> tokenizedRangeType;

  public TyReactiveList(boolean readonly, final Token listToken, final TokenizedItem<TyType> rangeType) {
    super(readonly ? TypeBehavior.ReadOnlyWithGet : TypeBehavior.ReadWriteWithSetGet);
    this.readonly = readonly;
    this.listToken = listToken;
    this.tokenizedRangeType = rangeType;
    ingest(listToken);
    ingest(rangeType.item);
  }

  @Override
  public void format(Formatter formatter) {
    tokenizedRangeType.item.format(formatter);
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    yielder.accept(listToken);
    tokenizedRangeType.emitBefore(yielder);
    tokenizedRangeType.item.emit(yielder);
    tokenizedRangeType.emitAfter(yielder);
  }

  @Override
  public String getAdamaType() {
    return "r<list<" + tokenizedRangeType.item.getAdamaType() + ">>";
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return "RxList<" + getRangeType(environment).getJavaBoxType(environment) + ">";
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveList(readonly, listToken, tokenizedRangeType).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    tokenizedRangeType.item.typing(environment);
    final var resolvedRangeType = environment.rules.Resolve(tokenizedRangeType.item, false);
    if (RuleSetTable.IsTable(environment, resolvedRangeType, true)) {
      environment.document.createError(this, String.format("The range type '%s' is not an appropriate for a list.", resolvedRangeType.getAdamaType()));
    }
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_list");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("range");
    tokenizedRangeType.item.writeTypeReflectionJson(writer, source);
    writer.endObject();
  }

  @Override
  public TyType getRangeType(final Environment environment) {
    return environment.rules.Resolve(tokenizedRangeType.item, false);
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("size".equals(name)) {
      return new TyNativeFunctional("size", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("size", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, listToken).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("prepend".equals(name) || "append".equals(name)) {
      ArrayList<TyType> args = new ArrayList<>();
      return new TyNativeFunctional(name, FunctionOverloadInstance.WRAP(new FunctionOverloadInstance(name, new TyNativeMaybe(TypeBehavior.ReadWriteWithSetGet, null, null, tokenizedRangeType), args, FunctionPaint.NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("remove".equals(name)) {
      ArrayList<TyType> args = new ArrayList<>();
      args.add(new TyNativeDouble(TypeBehavior.ReadOnlyNativeValue, null, listToken));
      return new TyNativeFunctional("remove", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("remove", new TyNativeVoid().withPosition(this), args, FunctionPaint.NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("clear".equals(name)) {
      return new TyNativeFunctional("clear", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("clear", new TyNativeVoid().withPosition(this), new ArrayList<>(), FunctionPaint.NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    return null;
  }

  @Override
  public String getDeltaType(Environment environment) {
    var range = getRangeType(environment);
    if (range instanceof DetailComputeRequiresGet) {
      range = ((DetailComputeRequiresGet) range).typeAfterGet(environment);
    }
    return "DMap<Double," + ((DetailHasDeltaType) range).getDeltaType(environment) + ">";
  }
}
