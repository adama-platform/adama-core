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
import ape.translator.tree.expressions.constants.DateTimeConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.traits.DetailCanExtractForUnique;
import ape.translator.tree.types.traits.IsCSVCompatible;
import ape.translator.tree.types.traits.IsNativeValue;
import ape.translator.tree.types.traits.IsOrderable;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

/** Type for a native date and a time with the time zone in the typical gregorian calendar */
public class TyNativeDateTime extends TySimpleNative implements //
    IsNativeValue, //
    IsOrderable, //
    IsCSVCompatible, //
    DetailCanExtractForUnique, //
    DetailHasDeltaType, //
    DetailTypeHasMethods, //
    AssignmentViaNative {
  public final Token readonlyToken;
  public final Token token;

  public TyNativeDateTime(final TypeBehavior behavior, final Token readonlyToken, final Token token) {
    super(behavior, "NtDateTime", "NtDateTime", 160);
    this.readonlyToken = readonlyToken;
    this.token = token;
    ingest(token);
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
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
    return "datetime";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyNativeDateTime(newBehavior, readonlyToken, token).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("datetime");
    writer.endObject();
  }

  @Override
  public String getDeltaType(Environment environment) {
    return "DDateTime";
  }

  @Override
  public Expression inventDefaultValueExpression(DocumentPosition forWhatExpression) {
    return new DateTimeConstant(ZonedDateTime.parse("1900-01-01T00:00:00-00:00"), token);
  }


  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    return environment.state.globals.findExtension(this, name);
  }
}
