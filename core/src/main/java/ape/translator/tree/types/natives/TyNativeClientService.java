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
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.definitions.DefineClientService;
import ape.translator.tree.definitions.DefineService;
import ape.translator.tree.definitions.client.Method;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.DetailNeverPublic;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyNativeClientService extends TyType implements //
    DetailNeverPublic, //
    DetailTypeHasMethods {
  public final DefineClientService service;

  public TyNativeClientService(final DefineClientService service) {
    super(TypeBehavior.ReadOnlyNativeValue);
    this.service = service;
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getAdamaType() {
    return "client";
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeClientService(service).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("client-service");
    writer.writeObjectFieldIntro("client-service");
    writer.writeString(service.name.text);
    writeAnnotations(writer);
    writer.endObject();
  }

  private TyType lookupType(String name, Environment environment) {
    if ("dynamic".equals(name)) {
      return new TyNativeDynamic(TypeBehavior.ReadOnlyNativeValue, null, null);
    }
    return environment.rules.FindMessageStructure(name, this, true).withPosition(service);
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    Method method = service.methods.get(name);
    if (method != null) {
      ArrayList<TyType> argTypes = new ArrayList<>();
      argTypes.add(new TyNativePrincipal(TypeBehavior.ReadWriteNative, null, null).withPosition(service));
      argTypes.add(lookupType(method.type_query.text, environment));
      if (method.type_body != null) {
        argTypes.add(lookupType(method.type_body.text, environment));
      }
      if (method.return_type != null) {
        TyType outputType = lookupType(method.return_type.text, environment);
        outputType = outputType.withPosition(service);
        outputType = new TyNativeResult(TypeBehavior.ReadOnlyNativeValue, null, method.name, new TokenizedItem<>(outputType)).withPosition(this);
        return new TyNativeFunctional(name, FunctionOverloadInstance.WRAP(new FunctionOverloadInstance(name, outputType, argTypes, FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.RemoteCall);
      } else {
        return new TyNativeFunctional(name, FunctionOverloadInstance.WRAP(new FunctionOverloadInstance(name, new TyNativeVoid(), argTypes, FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.RemoteCall);
      }
    }
    return null;
  }
}
