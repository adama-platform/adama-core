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
import ape.translator.tree.expressions.constants.StringConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.*;
import ape.translator.tree.types.traits.*;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

/** The type representing a utf-8 encoded string. This uses the native 'String' java type. */
public class TyNativeString extends TySimpleNative implements //
    IsNativeValue, //
    DetailHasDeltaType, //
    CanBeMapDomain, //
    DetailTypeHasMethods, //
    DetailCanExtractForUnique, //
    IsCSVCompatible, //
    IsOrderable, //
    AssignmentViaNative //
{
  public final Token readonlyToken;
  public final Token token;

  public TyNativeString(final TypeBehavior behavior, final Token readonlyToken, final Token token) {
    super(behavior, "String", "String", -1);
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
    return "string";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeString(newBehavior, readonlyToken, token).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("string");
    writer.endObject();
  }

  @Override
  public String getDeltaType(final Environment environment) {
    return "DString";
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new StringConstant(Token.WRAP("\"\"")).withPosition(forWhatExpression);
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("length".equals(name)) {
      return new TyNativeFunctional("length", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("size", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, token).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    return environment.state.globals.findExtension(this, name);
  }

  @Override
  public String getRxStringCodexName() {
    return "RxMap.StringCodec";
  }
}
