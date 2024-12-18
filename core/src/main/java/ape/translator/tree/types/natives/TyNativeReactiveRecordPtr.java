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
