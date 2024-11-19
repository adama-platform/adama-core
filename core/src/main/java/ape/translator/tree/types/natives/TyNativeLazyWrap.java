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
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;

import java.util.function.Consumer;

/** wrap lazy wrap an existing type to require a .get() */
public class TyNativeLazyWrap extends TyType implements DetailComputeRequiresGet {

  public final TyType wrapped;

  public TyNativeLazyWrap(TyType wrapped) {
    super(wrapped.behavior);
    this.wrapped = wrapped;
    ingest(wrapped);
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    wrapped.emitInternal(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    wrapped.format(formatter);
  }

  @Override
  public String getAdamaType() {
    return wrapped.getAdamaType();
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return wrapped.getJavaBoxType(environment);
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    return wrapped.getJavaConcreteType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyNativeLazyWrap(wrapped.makeCopyWithNewPositionInternal(position, newBehavior));
  }

  @Override
  public void typing(Environment environment) {
    wrapped.typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    wrapped.writeTypeReflectionJson(writer, source);
  }

  @Override
  public TyType typeAfterGet(Environment environment) {
    return wrapped;
  }
}
