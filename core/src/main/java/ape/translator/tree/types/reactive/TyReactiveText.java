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
