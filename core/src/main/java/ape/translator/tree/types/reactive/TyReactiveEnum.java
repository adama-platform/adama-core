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
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.constants.EnumConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleReactive;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeEnum;
import ape.translator.tree.types.shared.EnumStorage;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsOrderable;

public class TyReactiveEnum extends TySimpleReactive implements //
    IsOrderable, //
    IsEnum //
{
  public final String name;
  public final EnumStorage storage;

  public TyReactiveEnum(boolean readonly, final Token nameToken, final EnumStorage storage) {
    super(readonly, nameToken, "RxEnumInt32");
    name = nameToken.text;
    this.storage = storage;
  }

  @Override
  public String getAdamaType() {
    return "r<" + name + ">";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveEnum(readonly, token, storage).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("enum");
    writer.writeObjectFieldIntro("enum");
    writer.writeString(name);
    writer.writeObjectFieldIntro("options");
    storage.writeTypeReflectionJson(writer);
    writer.endObject();
  }

  @Override
  public void format(Formatter formatter) {
    super.format(formatter);
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new EnumConstant(Token.WRAP(name), Token.WRAP("::"), Token.WRAP(storage.getDefaultLabel())).withPosition(forWhatExpression);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public EnumStorage storage() {
    return storage;
  }

  @Override
  public TyType typeAfterGet(final Environment environment) {
    return new TyNativeEnum(TypeBehavior.ReadOnlyNativeValue, token, token, token, storage, token).withPosition(this);
  }
}
