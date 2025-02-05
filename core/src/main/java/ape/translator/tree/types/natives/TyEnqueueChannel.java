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

public class TyEnqueueChannel extends TyType implements //
    DetailTypeHasMethods, //
    DetailNeverPublic {
  public final TokenizedItem<TyType> tokenizedType;
  public final String channelName;

  public TyEnqueueChannel(final String channelName, final TokenizedItem<TyType> tokenizedType) {
    super(TypeBehavior.ReadOnlyNativeValue);
    this.channelName = channelName;
    this.tokenizedType = tokenizedType;
    ingest(tokenizedType.item);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    throw new UnsupportedOperationException("internal types can't be emitted");
  }

  @Override
  public void format(Formatter formatter) {
    throw new UnsupportedOperationException("internal types can't be emitted");
  }

  @Override
  public String getAdamaType() {
    return "enqueue.channel<" + tokenizedType.item.getAdamaType() + ">";
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    throw new UnsupportedOperationException("TyEnqueueChannel does support assignment");
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    throw new UnsupportedOperationException("TyEnqueueChannel does support assignment");
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyEnqueueChannel(channelName, tokenizedType).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    tokenizedType.item.typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("channelqueue");
    writer.writeObjectFieldIntro("channel");
    writer.writeString(channelName);
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    tokenizedType.item.writeTypeReflectionJson(writer, source);
    writer.endObject();
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("enqueue".equals(name)) {
      TyNativeVoid noResult = new TyNativeVoid();
      final var argTypes = new ArrayList<TyType>();
      argTypes.add(new TyNativePrincipal(TypeBehavior.ReadOnlyNativeValue, null, null));
      argTypes.add(environment.rules.Resolve(tokenizedType.item, false));
      return new TyNativeFunctional("__enqueue", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("__enqueue(\"" + channelName + "\", ", noResult, argTypes, FunctionPaint.NORMAL)), FunctionStyleJava.InjectNameThenArgsNoInitialParenthesis);
    }

    return null;
  }
}
