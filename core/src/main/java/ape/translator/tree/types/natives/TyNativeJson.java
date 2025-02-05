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
import ape.translator.tree.expressions.constants.DynamicNullConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyNativeJson extends TySimpleNative implements //
    DetailHasDeltaType, //
    DetailTypeHasMethods, //
    AssignmentViaNative //
{
  public final Token readonlyToken;
  public final Token token;

  public TyNativeJson(final TypeBehavior behavior, final Token readonlyToken, final Token token) {
    super(behavior, "NtJson", "NtJson", -1);
    this.readonlyToken = readonlyToken;
    this.token = token;
    ingest(token);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    if (readonlyToken != null) {
      yielder.accept(readonlyToken);
    }
    yielder.accept(token);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getAdamaType() {
    return "json";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeJson(newBehavior, readonlyToken, token).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("json");
    writer.endObject();
  }

  @Override
  public String getDeltaType(final Environment environment) {
    return "DDynamic";
  }

  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    if ("to_dynamic".equals(name)) {
      return new TyNativeFunctional("to_dynamic", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("to_dynamic", new TyNativeDynamic(behavior, readonlyToken, token), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("deref".equals(name)) {
      ArrayList<FunctionOverloadInstance> versions = new ArrayList<>();
      {
        ArrayList<TyType> args = new ArrayList<>();
        args.add(new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, token));
        versions.add(new FunctionOverloadInstance("deref", new TyNativeJson(behavior, readonlyToken, token), args, FunctionPaint.READONLY_NORMAL));
      }
      {
        ArrayList<TyType> args = new ArrayList<>();
        args.add(new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, token));
        versions.add(new FunctionOverloadInstance("deref", new TyNativeJson(behavior, readonlyToken, token), args, FunctionPaint.READONLY_NORMAL));
      }
      return new TyNativeFunctional("next", versions, FunctionStyleJava.ExpressionThenArgs);
    }
    if ("to_b".equals(name)) {
      TyType retType = new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, token, new TokenizedItem<>(new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, null)));
      return new TyNativeFunctional("to_b", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("to_b", retType, new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("to_i".equals(name)) {
      TyType retType = new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, token, new TokenizedItem<>(new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null)));
      return new TyNativeFunctional("to_i", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("to_i", retType, new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("to_s".equals(name)) {
      TyType retType = new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, token, new TokenizedItem<>(new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, null)));
      return new TyNativeFunctional("to_i", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("to_i", retType, new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    return environment.state.globals.findExtension(this, name);
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new DynamicNullConstant(Token.WRAP("null")).withPosition(forWhatExpression);
  }
}
