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
import ape.translator.tree.expressions.constants.NoOneClientConstant;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.traits.CanBeMapDomain;
import ape.translator.tree.types.traits.DetailCanExtractForUnique;
import ape.translator.tree.types.traits.IsOrderable;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.function.Consumer;

public class TyNativeSecurePrincipal extends TySimpleNative implements //
    DetailHasDeltaType, //
    CanBeMapDomain, //
    IsOrderable, //
    DetailCanExtractForUnique, //
    DetailTypeHasMethods, //
    AssignmentViaNative //
{
  public final Token readonlyToken;
  public final Token secureToken;
  public final Token openToken;
  public final Token principalToken;
  public final Token closedToken;

  public TyNativeSecurePrincipal(final TypeBehavior behavior, final Token readonlyToken, final Token secureToken, final Token openToken, final Token principalToken, final Token closedToken) {
    super(behavior, "NtPrincipal", "NtPrincipal", -1);
    this.readonlyToken = readonlyToken;
    this.secureToken = secureToken;
    this.openToken = openToken;
    this.principalToken = principalToken;
    this.closedToken = closedToken;
    ingest(secureToken);
    ingest(closedToken);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    if (readonlyToken != null) {
      yielder.accept(readonlyToken);
    }
    yielder.accept(secureToken);
    yielder.accept(openToken);
    yielder.accept(principalToken);
    yielder.accept(closedToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getRxStringCodexName() {
    return "RxMap.PrincipalCodec";
  }

  @Override
  public String getAdamaType() {
    return "secure<principal>";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeSecurePrincipal(newBehavior, readonlyToken, secureToken, openToken, principalToken, closedToken).withPosition(position);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_value");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    writer.writeString("secure<principal>");
    writer.endObject();
  }

  @Override
  public String getDeltaType(final Environment environment) {
    return "DPrincipal";
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new NoOneClientConstant(Token.WRAP("@no_one")).withPosition(forWhatExpression);
  }

  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    TyNativeFunctional exclusive = environment.state.globals.findExtension(this, name);
    if (exclusive != null) {
      return exclusive;
    }
    return environment.state.globals.findExtension(new TyNativePrincipal(behavior, readonlyToken, principalToken), name);
  }
}
