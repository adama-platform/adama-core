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
import ape.translator.tree.types.traits.DetailNeedsSettle;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;

import java.util.function.Consumer;

public class TyReactiveLazy extends TyType implements //
    DetailContainsAnEmbeddedType, //
    DetailNeedsSettle, //
    DetailComputeRequiresGet // to get the native value
{
  public final TyType computedType;
  public final boolean cached;

  public TyReactiveLazy(final TyType computedType, boolean cached) {
    super(TypeBehavior.ReadOnlyGetNativeValue);
    this.computedType = computedType;
    ingest(computedType);
    this.cached = cached;
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getAdamaType() {
    return "r<auto<" + computedType.getAdamaType() + ">>";
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    if (cached) {
      return String.format("RxCachedLazy<%s>", computedType.getJavaBoxType(environment));
    } else {
      return String.format("RxLazy<%s>", computedType.getJavaBoxType(environment));
    }
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveLazy(computedType, cached).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    computedType.typing(environment);
    var typedAs = environment.rules.Resolve(computedType, false);
    if (!(typedAs instanceof DetailHasDeltaType)) {
      environment.document.createError(this, String.format("Lazy type has inappropriate type `%s`", computedType.getAdamaType()));
      return;
    }
    while (typedAs instanceof DetailContainsAnEmbeddedType) {
      typedAs = environment.rules.Resolve(((DetailContainsAnEmbeddedType) typedAs).getEmbeddedType(environment), false);
      if (!(typedAs instanceof DetailHasDeltaType)) {
        environment.document.createError(this, String.format("Lazy type has inappropriate type `%s`", computedType.getAdamaType()));
        return;
      }
    }
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    computedType.writeTypeReflectionJson(writer, source);
  }

  @Override
  public TyType typeAfterGet(final Environment environment) {
    return getEmbeddedType(environment);
  }

  @Override
  public TyType getEmbeddedType(final Environment environment) {
    return environment.rules.Resolve(computedType, false);
  }
}
