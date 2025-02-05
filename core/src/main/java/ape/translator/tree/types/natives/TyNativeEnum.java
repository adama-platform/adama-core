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
import ape.translator.codegen.CodeGenEnums;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineDispatcher;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.constants.EnumConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.reactive.TyReactiveEnum;
import ape.translator.tree.types.shared.EnumStorage;
import ape.translator.tree.types.traits.*;
import ape.translator.tree.types.traits.*;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailSpecialReactiveRefResolve;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;
import ape.translator.tree.types.traits.details.DetailTypeProducesRootLevelCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TyNativeEnum extends TySimpleNative implements //
    IsNativeValue, //
    IsOrderable, //
    DetailHasDeltaType, //
    CanBeMapDomain, //
    DetailSpecialReactiveRefResolve, //
    DetailTypeProducesRootLevelCode, //
    DetailTypeHasMethods, //
    DetailCanExtractForUnique, //
    IsEnum, //
    AssignmentViaNative //
{
  public final Token endBrace;
  public final Token enumToken;
  public final String name;
  public final Token nameToken;
  public final Token openBrace;
  public final EnumStorage storage;

  public TyNativeEnum(final TypeBehavior behavior, final Token enumToken, final Token nameToken, final Token openBrace, final EnumStorage storage, final Token endBrace) {
    super(behavior, "int", "Integer", 4);
    this.enumToken = enumToken;
    this.nameToken = nameToken;
    name = nameToken.text;
    this.openBrace = openBrace;
    this.storage = storage;
    this.endBrace = endBrace;
    ingest(enumToken);
    ingest(nameToken);
    ingest(endBrace);
    storage.ingest(this);
  }

  @Override
  public void compile(final StringBuilderWithTabs sb, final Environment environment) {
    CodeGenEnums.writeEnumArray(sb, name, "ALL_VALUES", "", storage);
    CodeGenEnums.writeEnumNextPrevString(sb, name, storage);
    CodeGenEnums.writeEnumFixer(sb, name, storage);
    for (final Map.Entry<String, HashMap<String, ArrayList<DefineDispatcher>>> dispatchers : storage.dispatchersByNameThenSignature.entrySet()) {
      CodeGenEnums.writeDispatchers(sb, storage, dispatchers.getValue(), dispatchers.getKey(), environment);
    }
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(enumToken);
    yielder.accept(nameToken);
    yielder.accept(openBrace);
    storage.emit(yielder);
    yielder.accept(endBrace);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(enumToken);
    formatter.endLine(openBrace);
    formatter.tabUp();
    storage.format(formatter);
    formatter.tabDown();
    formatter.endLine(endBrace);
  }

  @Override
  public String getAdamaType() {
    return name;
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeEnum(newBehavior, enumToken, nameToken, openBrace, storage, endBrace).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("enum");
    writer.writeObjectFieldIntro("enum");
    writer.writeString(name);
    writer.writeObjectFieldIntro("options");
    storage.writeTypeReflectionJson(writer);
    writer.endObject();
  }

  @Override
  public String getDeltaType(final Environment environment) {
    return "DInt32";
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new EnumConstant(Token.WRAP(name), Token.WRAP("::"), Token.WRAP(storage.getDefaultLabel())).withPosition(forWhatExpression);
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("to_int".equals(name)) {
      return new TyNativeFunctional("to_int", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("Utility.identity", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, enumToken).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.InjectNameThenExpressionAndArgs);
    }
    if ("next".equals(name)) {
      return new TyNativeFunctional("next", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("__EnumCycleNext_" + this.name, this, new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.InjectNameThenExpressionAndArgs);
    }
    if ("prev".equals(name)) {
      return new TyNativeFunctional("prev", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("__EnumCyclePrev_" + this.name, this, new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.InjectNameThenExpressionAndArgs);
    }
    if ("to_string".equals(name)) {
      return new TyNativeFunctional("to_string", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("__EnumString_" + this.name, new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, enumToken), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.InjectNameThenExpressionAndArgs);
    }
    return storage.computeDispatcherType(name);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public EnumStorage storage() {
    return storage;
  }

  @Override
  public TyType typeAfterReactiveRefResolve(final Environment environment) {
    return new TyReactiveEnum(false, nameToken, storage).withPosition(this);
  }

  @Override
  public String getRxStringCodexName() {
    return "RxMap.IntegerCodec";
  }
}
