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
