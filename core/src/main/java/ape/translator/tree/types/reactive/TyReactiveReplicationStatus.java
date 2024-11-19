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
import ape.translator.tree.types.natives.TyNativeFunctional;
import ape.translator.tree.types.structures.ReplicationDefinition;
import ape.translator.tree.types.traits.IsReactiveValue;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.function.Consumer;

public class TyReactiveReplicationStatus  extends TyType implements //
    IsReactiveValue, //
    DetailTypeHasMethods, //
    DetailHasDeltaType {

  public final ReplicationDefinition definition;

  public TyReactiveReplicationStatus(ReplicationDefinition definition) {
    super(TypeBehavior.ReadOnlyWithGet);
    this.definition = definition;
    ingest(definition);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
  }

  @Override
  public String getAdamaType() {
    return "replication";
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return "RxReplicationStatus";
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    return "RxReplicationStatus";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyReactiveReplicationStatus(definition).withPosition(position);
  }

  @Override
  public void typing(Environment environment) {
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("replication_status");
    writer.endObject();
  }

  @Override
  public String getDeltaType(Environment environment) {
    return "DReplicationStatus";
  }

  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    return null;
  }
}
