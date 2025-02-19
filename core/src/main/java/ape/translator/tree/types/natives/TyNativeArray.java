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
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.InjectExpression;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.natives.functions.TyNativeFunctionInternalFieldReplacement;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.*;
import ape.translator.tree.types.traits.details.*;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyNativeArray extends TyType implements //
    AssignmentViaNative, //
    DetailContainsAnEmbeddedType, //
    DetailHasDeltaType, //
    DetailIndexLookup, //
    DetailInventDefaultValueExpression, //
    DetailNativeDeclarationIsNotStandard, //
    DetailTypeHasMethods //
{
  public final Token arrayToken;
  public final TyType elementType;

  public TyNativeArray(final TypeBehavior behavior, final TyType elementType, final Token arrayToken) {
    super(behavior);
    this.elementType = elementType;
    this.arrayToken = arrayToken;
    ingest(elementType);
    ingest(arrayToken);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    elementType.emit(yielder);
    yielder.accept(arrayToken);
  }

  @Override
  public void format(Formatter formatter) {
    elementType.format(formatter);
  }

  @Override
  public String getAdamaType() {
    return String.format("%s[]", elementType.getAdamaType());
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    return String.format("%s[]", getEmbeddedType(environment).getJavaConcreteType(environment));
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeArray(newBehavior, elementType.makeCopyWithNewPosition(position, newBehavior), arrayToken).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    elementType.typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_array");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    elementType.writeTypeReflectionJson(writer, source);
    writer.endObject();
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
  public TyType getEmbeddedType(final Environment environment) {
    return environment.rules.Resolve(elementType, true);
  }

  @Override
  public IndexLookupStyle getLookupStyle(final Environment environment) {
    return IndexLookupStyle.UtilityFunction;
  }

  @Override
  public String getPatternWhenValueProvided(final Environment environment) {
    return "%s";
  }

  @Override
  public String getStringWhenValueNotProvided(final Environment environment) {
    return "new " + getJavaConcreteType(environment) + "{}";
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new InjectExpression(this) {
      @Override
      public void writeJava(final StringBuilder sb, final Environment environment) {
        sb.append(getStringWhenValueNotProvided(environment));
      }
    };
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("size".equals(name)) {
      return new TyNativeFunctionInternalFieldReplacement("length", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("length", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, arrayToken).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.None);
    }
    return environment.state.globals.findExtension(this, name);
  }
}
