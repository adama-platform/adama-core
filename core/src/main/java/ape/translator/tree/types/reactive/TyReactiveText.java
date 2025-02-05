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
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.assign.AssignmentViaSetter;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyReactiveText extends TyType implements //
    DetailHasDeltaType, //
    AssignmentViaSetter, //
    DetailTypeHasMethods {
  public final Token textToken;
  public final boolean readonly;

  public TyReactiveText(final boolean readonly, final Token textToken) {
    super(readonly ? TypeBehavior.ReadOnlyWithGet : TypeBehavior.ReadWriteWithSetGet);
    this.readonly = readonly;
    this.textToken = textToken;
    ingest(textToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(textToken);
  }

  @Override
  public String getAdamaType() {
    return "text";
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    return "RxText";
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return "RxText";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveText(readonly, textToken).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_text");
    writeAnnotations(writer);
    writer.endObject();
  }

  @Override
  public String getDeltaType(Environment environment) {
    return "DText";
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    ArrayList<TyType> args = new ArrayList<>();
    if ("append".equals(name)) {
      args.add(new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, textToken).withPosition(this));
      args.add(new TyNativeDynamic(TypeBehavior.ReadOnlyNativeValue, null, textToken).withPosition(this));
      return new TyNativeFunctional("append", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("append", new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, textToken).withPosition(this), args, FunctionPaint.NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("get".equals(name)) {
      return new TyNativeFunctional("append", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("append", new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, textToken).withPosition(this), args, FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    return null;
  }
}
