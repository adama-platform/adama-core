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
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.function.Consumer;

public class TyNativeReactiveRecordPtr extends TyType implements //
    AssignmentViaNative, //
    DetailTypeHasMethods, DetailContainsAnEmbeddedType //
{
  public final TyReactiveRecord source;

  public TyNativeReactiveRecordPtr(final TypeBehavior behavior, final TyReactiveRecord source) {
    super(behavior);
    this.source = source;
    ingest(source);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    source.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getAdamaType() {
    return source.getAdamaType();
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    return source.getJavaBoxType(environment);
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return source.getJavaConcreteType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeReactiveRecordPtr(newBehavior, source).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    source.typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_reactive_ptr");
    writeAnnotations(writer);
    writer.endObject();
  }

  @Override
  public TyType getEmbeddedType(final Environment environment) {
    return source;
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    return source.lookupMethod(name, environment);
  }
}
