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
import ape.translator.tree.types.traits.details.DetailRequiresResolveCall;
import ape.translator.tree.types.traits.details.DetailSpecialReactiveRefResolve;

import java.util.function.Consumer;

public class TyReactiveRef extends TyType implements //
    DetailRequiresResolveCall {
  public final String ref;
  public final boolean readonly;
  public final Token refToken;

  public TyReactiveRef(final boolean readonly, final Token refToken) {
    super(readonly ? TypeBehavior.ReadOnlyWithGet : TypeBehavior.ReadWriteWithSetGet);
    this.readonly = readonly;
    this.refToken = refToken;
    ref = refToken.text;
    ingest(refToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(refToken);
  }

  @Override
  public String getAdamaType() {
    return ref;
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    throw new UnsupportedOperationException("the reference must be resolved");
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    throw new UnsupportedOperationException("the reference must be resolved");
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveRef(readonly, refToken).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    // handled by environment.rules.Revolve
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_ref");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("ref");
    writer.writeString(ref);
    writer.endObject();
  }

  @Override
  public TyType resolve(final Environment environment) {
    final var type = environment.document.types.get(ref);
    if (type instanceof DetailSpecialReactiveRefResolve) {
      return ((DetailSpecialReactiveRefResolve) type).typeAfterReactiveRefResolve(environment);
    }
    return type;
  }
}
