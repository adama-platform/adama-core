/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.translator.tree.types.natives;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.constants.TimeConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.natives.functions.TyNativeFunctionInternalFieldReplacement;
import ape.translator.tree.types.traits.DetailCanExtractForUnique;
import ape.translator.tree.types.traits.IsCSVCompatible;
import ape.translator.tree.types.traits.IsNativeValue;
import ape.translator.tree.types.traits.IsOrderable;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

/** Type for native time within a day at the precision of a minute */
public class TyNativeTime extends TySimpleNative implements //
    IsNativeValue, //
    IsOrderable, //
    DetailHasDeltaType, //
    DetailTypeHasMethods, //
    DetailCanExtractForUnique, //
    IsCSVCompatible, //
    AssignmentViaNative {
  public final Token readonlyToken;
  public final Token token;

  public TyNativeTime(final TypeBehavior behavior, final Token readonlyToken, final Token token) {
    super(behavior, "NtTime", "NtTime", 64);
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
    return "time";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyNativeTime(newBehavior, readonlyToken, token).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("time");
    writer.endObject();
  }

  @Override
  public String getDeltaType(Environment environment) {
    return "DTime";
  }

  @Override
  public Expression inventDefaultValueExpression(DocumentPosition forWhatExpression) {
    return new TimeConstant(0, 0, token);
  }


  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    if ("hour".equals(name)) {
      return new TyNativeFunctionInternalFieldReplacement("hour", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("hour", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, Token.WRAP("readonly"), null).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.None);
    } else if ("minute".equals(name)) {
      return new TyNativeFunctionInternalFieldReplacement("minute", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("minute", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, Token.WRAP("readonly"), null).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.None);
    }
    return environment.state.globals.findExtension(this, name);
  }
}
