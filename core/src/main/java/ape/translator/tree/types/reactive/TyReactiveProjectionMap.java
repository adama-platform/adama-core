/**
 * MIT License
 * 
 * Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.DetailNeedsSettle;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.function.Consumer;

public class TyReactiveProjectionMap extends TyType implements //
    DetailTypeHasMethods, //
    DetailNeedsSettle {
  private final Token projectionToken;
  private final Token open;
  private final Token tableVar;
  private final Token dot;
  private final Token field;
  private final Token close;
  private final boolean readonly;
  private final boolean policy;
  private TyType rangeType;

  public TyReactiveProjectionMap(Token projectionToken, Token open, Token tableVar, Token dot, Token field, Token close, boolean readonly, boolean policy) {
    super(TypeBehavior.ReadOnlyWithGet);
    this.projectionToken = projectionToken;
    this.open = open;
    this.tableVar = tableVar;
    this.dot = dot;
    this.field = field;
    this.close = close;
    this.readonly = readonly;
    this.policy = policy;
    ingest(projectionToken);
    ingest(close);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    yielder.accept(projectionToken);
    yielder.accept(open);
    yielder.accept(tableVar);
    yielder.accept(dot);
    yielder.accept(field);
    yielder.accept(close);
  }

  @Override
  public String getAdamaType() {
    return "projection<" + tableVar.text + "." + field.text + ">";
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return getJavaConcreteType(environment);
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    // return "RxProjectionMap<" + environment.document.pureFunctions.get(funcName.text).returnType.getJavaBoxType(environment) + ">";
    return null;
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyReactiveProjectionMap(projectionToken, open, tableVar, dot, field, close, readonly, policy).withPosition(position);
  }

  @Override
  public void typing(Environment environment) {
    if (readonly) {
      environment.document.createError(this, "projection maps are readonly by default and the annotation is not welcome.");
    }
    if (policy) {
      environment.document.createError(this, "projection maps must be private.");
    }
    TyType tableType = environment.rules.Resolve(environment.lookup(tableVar.text, true, this, false), false);
    if (!(tableType instanceof TyReactiveTable)) {
      environment.document.createError(this, tableVar.text + " must be a reactive table");
      return;
    }
    TyReactiveRecord recordType = (TyReactiveRecord) environment.rules.Resolve(((TyReactiveTable) tableType).getEmbeddedType(environment), false);
    FieldDefinition fd = recordType.storage.fields.get(field.text);
    if (fd == null) {
      environment.document.createError(this, "'" + field.text + "' was not a field within table '" + tableVar.text + "'");
      return;
    }
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_projection");
    writeAnnotations(writer);
    writer.endObject();
  }

  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    return null;
  }
}
