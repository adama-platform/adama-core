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
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeMaybe;
import ape.translator.tree.types.traits.DetailNeedsSettle;
import ape.translator.tree.types.traits.IsKillable;
import ape.translator.tree.types.traits.assign.AssignmentViaSetter;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.function.Consumer;

public class TyReactiveMaybe extends TyType implements //
    DetailContainsAnEmbeddedType, //
    DetailNeedsSettle, //
    DetailComputeRequiresGet, //
    IsKillable, //
    AssignmentViaSetter //
{
  public final boolean readonly;
  public final Token maybeToken;
  public final TokenizedItem<TyType> tokenizedElementType;

  public TyReactiveMaybe(boolean readonly, final Token maybeToken, final TokenizedItem<TyType> elementType) {
    super(readonly ? TypeBehavior.ReadOnlyWithGet : TypeBehavior.ReadWriteWithSetGet);
    this.readonly = readonly;
    this.maybeToken = maybeToken;
    tokenizedElementType = elementType;
    ingest(maybeToken);
    ingest(elementType.item);
  }

  @Override
  public void format(Formatter formatter) {
    tokenizedElementType.item.format(formatter);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(maybeToken);
    tokenizedElementType.emitBefore(yielder);
    tokenizedElementType.item.emit(yielder);
    tokenizedElementType.emitAfter(yielder);
  }

  @Override
  public String getAdamaType() {
    return String.format("r<maybe<%s>>", tokenizedElementType.item.getAdamaType());
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    final var elementTypeFixed = environment.rules.Resolve(getEmbeddedType(environment), true);
    String primary = elementTypeFixed.getJavaBoxType(environment);
    String secondary = null;
    if (elementTypeFixed instanceof DetailComputeRequiresGet) {
      secondary = environment.rules.Resolve(((DetailComputeRequiresGet) elementTypeFixed).typeAfterGet(environment), false).getJavaBoxType(environment);
    } else {
      secondary = primary;
    }
    return String.format("RxMaybe<%s,%s>", primary, secondary);
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType getEmbeddedType(final Environment environment) {
    return environment.rules.Resolve(tokenizedElementType.item, false);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyReactiveMaybe(readonly, maybeToken, tokenizedElementType).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    environment.rules.Resolve(tokenizedElementType.item, false);
    tokenizedElementType.item.typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("reactive_maybe");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("type");
    tokenizedElementType.item.writeTypeReflectionJson(writer, source);
    writer.endObject();
  }

  @Override
  public TyType typeAfterGet(final Environment environment) {
    final var elementType = environment.rules.Resolve(tokenizedElementType.item, false);
    if (elementType instanceof TyReactiveRecord) {
      return new TyNativeMaybe(TypeBehavior.ReadWriteNative, null, maybeToken, new TokenizedItem<>(elementType));
    } else {
      return new TyNativeMaybe(TypeBehavior.ReadWriteNative, null, maybeToken, new TokenizedItem<>(((DetailComputeRequiresGet) elementType).typeAfterGet(environment)));
    }
  }
}
