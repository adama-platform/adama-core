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
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeFunctional;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.DetailNeedsSettle;
import ape.translator.tree.types.traits.IsKillable;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;
import ape.translator.tree.types.traits.details.DetailRequiresResolveCall;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyReactiveTable extends TyType implements //
    DetailContainsAnEmbeddedType, //
    DetailNeedsSettle, //
    IsKillable, //
    DetailTypeHasMethods {
  public final boolean readonly;
  public final String recordName;
  public final TokenizedItem<Token> recordNameToken;
  public final Token tableToken;
  private boolean hasPolicy;


  public TyReactiveTable(boolean readonly, final Token tableToken, final TokenizedItem<Token> recordNameToken) {
    super(readonly ? TypeBehavior.ReadOnlyWithGet : TypeBehavior.ReadWriteWithSetGet);
    this.readonly = readonly;
    this.tableToken = tableToken;
    this.recordNameToken = recordNameToken;
    recordName = recordNameToken.item.text;
    ingest(tableToken);
    ingest(recordNameToken.item);
    this.hasPolicy = false;
  }

  public void raiseHasPolicy() {
    this.hasPolicy = true;
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(tableToken);
    recordNameToken.emitBefore(yielder);
    yielder.accept(recordNameToken.item);
    recordNameToken.emitAfter(yielder);
  }

  @Override
  public String getAdamaType() {
    return String.format("table<%s>", recordName);
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    return String.format("RxTable<RTx%s>", recordName);
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveTable(readonly, tableToken, recordNameToken).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    environment.rules.Resolve(new TyReactiveRef(readonly, recordNameToken.item), false);
    if (hasPolicy) {
      environment.document.createError(this, "Tables are not allowed to have a privacy policy as they default to private.");
    }
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_table");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("record_name");
    writer.writeString(recordName);
    writer.endObject();
  }

  @Override
  public TyType getEmbeddedType(final Environment environment) {
    TyType subtype = new TyReactiveRef(readonly, recordNameToken.item);
    while (subtype instanceof DetailRequiresResolveCall) {
      subtype = ((DetailRequiresResolveCall) subtype).resolve(environment);
    }
    return subtype;
  }

  @Override
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if ("size".equals(name)) {
      return new TyNativeFunctional("size", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("size", new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, recordNameToken.item).withPosition(this), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    return null;
  }
}
