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
import ape.translator.tree.expressions.constants.TemplateConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.traits.DetailNeverPublic;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.function.Consumer;

/** templates are functions that take dynamic objects */
public class TyNativeTemplate extends TySimpleNative implements //
    DetailTypeHasMethods, //
    DetailNeverPublic {
  private final Token templateToken;

  public TyNativeTemplate(Token templateToken) {
    super(TypeBehavior.ReadOnlyNativeValue, "NtTemplate", "NtTemplate", -1);
    this.templateToken = templateToken;
    ingest(templateToken);
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    yielder.accept(templateToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getAdamaType() {
    return "template";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyNativeString(newBehavior, null, templateToken).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("template");
    writer.endObject();
  }

  @Override
  public Expression inventDefaultValueExpression(DocumentPosition forWhatExpression) {
    return new TemplateConstant(templateToken.cloneWithNewText("`x``x`"));
  }

  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    return environment.state.globals.findExtension(this, name);
  }
}
