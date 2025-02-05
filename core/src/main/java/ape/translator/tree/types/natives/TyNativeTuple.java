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
package ape.translator.tree.types.natives;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.AnonymousTuple;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StorageSpecialization;
import ape.translator.tree.types.structures.StructureStorage;
import ape.translator.tree.types.traits.CanBeNativeArray;
import ape.translator.tree.types.traits.details.DetailRequiresResolveCall;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TyNativeTuple extends TyType implements //
    CanBeNativeArray, //
    DetailRequiresResolveCall {

  private final Token readonlyToken;
  private final Token tupleToken;
  private final ArrayList<PrefixedType> types;
  private final StructureStorage storage;
  private Token endToken;
  public TyNativeTuple(final TypeBehavior behavior, final Token readonlyToken, final Token tupleToken) {
    super(behavior);
    this.readonlyToken = readonlyToken;
    this.tupleToken = tupleToken;
    this.types = new ArrayList<>();
    this.endToken = null;
    this.storage = new StructureStorage(Token.WRAP("__Tuple"), StorageSpecialization.Message, true, false, tupleToken);
    ingest(readonlyToken);
    ingest(tupleToken);
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    if (readonlyToken != null) {
      yielder.accept(readonlyToken);
    }
    yielder.accept(tupleToken);
    for (PrefixedType pt : types) {
      yielder.accept(pt.token);
      pt.type.emit(yielder);
    }
    yielder.accept(endToken);
  }

  @Override
  public void format(Formatter formatter) {
    for (PrefixedType pt : types) {
      pt.type.format(formatter);
    }
  }

  @Override
  public String getAdamaType() {
    StringBuilder sb = new StringBuilder();
    sb.append("tuple");
    for (PrefixedType pt : types) {
      sb.append(pt.token.text);
      sb.append(pt.type.getAdamaType());
    }
    sb.append(">");
    return sb.toString();
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    throw new UnsupportedOperationException("the tuple must be resolved");
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    throw new UnsupportedOperationException("the tuple must be resolved");
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    TyNativeTuple tuple = new TyNativeTuple(this.behavior, readonlyToken, tupleToken);
    for (PrefixedType pt : types) {
      tuple.add(pt.token, pt.type);
    }
    tuple.finish(endToken);
    return tuple.withPosition(position);
  }

  public void add(Token token, TyType type) {
    ingest(token);
    FieldDefinition fd = FieldDefinition.invent(type, AnonymousTuple.nameOf(types.size()));
    fd.ingest(this);
    storage.add(fd);
    types.add(new PrefixedType(token, type));
  }

  public void finish(Token token) {
    this.endToken = token;
    ingest(endToken);
  }

  @Override
  public void typing(Environment environment) {
    // handled by environment.rules.Revolve
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_tuple");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("types");
    writer.beginArray();
    for (PrefixedType pt : types) {
      pt.type.writeTypeReflectionJson(writer, source);
    }
    writer.endArray();
    writer.endObject();
  }

  @Override
  public TyType resolve(Environment environment) {
    TyType typeEstimate = new TyNativeMessage(this.behavior, tupleToken, Token.WRAP("_TupleConvert_" + environment.autoVariable()), storage);
    return environment.rules.EnsureRegisteredAndDedupe(typeEstimate, false);
  }

  public static class PrefixedType {
    public final Token token;
    public final TyType type;

    public PrefixedType(Token token, TyType type) {
      this.token = token;
      this.type = type;
    }
  }
}
