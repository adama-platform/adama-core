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
