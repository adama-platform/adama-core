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
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.constants.NothingAssetConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleReactive;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeAsset;
import ape.translator.tree.types.traits.IsOrderable;

public class TyReactiveAsset extends TySimpleReactive implements //
    IsOrderable {
  public TyReactiveAsset(boolean readonly, final Token token) {
    super(readonly, token, "RxAsset");
  }

  @Override
  public String getAdamaType() {
    return "r<asset>";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveAsset(readonly, token).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("asset");
    writer.endObject();
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new NothingAssetConstant(Token.WRAP("@nothing")).withPosition(forWhatExpression);
  }

  @Override
  public TyType typeAfterGet(final Environment environment) {
    return new TyNativeAsset(TypeBehavior.ReadOnlyNativeValue, null, token);
  }
}
