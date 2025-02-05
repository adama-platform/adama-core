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
package ape.translator.tree.types;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.common.Typable;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.traits.details.DetailInventDefaultValueExpression;
import ape.translator.tree.types.traits.details.DetailNativeDeclarationIsNotStandard;

import java.util.function.Consumer;

public abstract class TyType extends DocumentPosition implements Typable {
  public final TypeBehavior behavior;
  private TypeAnnotation annotation;

  public TyType(final TypeBehavior behavior) {
    this.behavior = behavior;
    this.annotation = null;
  }

  public abstract void format(Formatter formatter);

  public abstract void emitInternal(Consumer<Token> yielder);

  public void emit(Consumer<Token> yielder) {
    emitInternal(yielder);
    if (annotation != null) {
      annotation.emit(yielder);
    }
  }

  public abstract String getAdamaType();

  public abstract String getJavaBoxType(Environment environment);

  public abstract String getJavaConcreteType(Environment environment);

  public abstract TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior);

  public String getJavaDefaultValue(Environment environment, DocumentPosition position) {
    TyType self = environment.rules.Resolve(this, true);
    if (self instanceof DetailInventDefaultValueExpression) {
      Expression created = ((DetailInventDefaultValueExpression) self).inventDefaultValueExpression(position);
      created.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
      StringBuilder sb = new StringBuilder();
      created.writeJava(sb, environment);
      return sb.toString();
    }
    if (self instanceof DetailNativeDeclarationIsNotStandard) {
      return ((DetailNativeDeclarationIsNotStandard) self).getStringWhenValueNotProvided(environment);
    }
    throw new UnsupportedOperationException("failed to compute the default java value for:" + self.getAdamaType());
  }

  public TyType makeCopyWithNewPosition(DocumentPosition position, TypeBehavior newBehavior) {
    TyType copy = makeCopyWithNewPositionInternal(position, newBehavior);
    if (annotation != null) {
      copy.annotation = annotation;
    }
    return copy;
  }

  public abstract void typing(Environment environment);

  public void writeAnnotations(JsonStreamWriter writer) {
    if (annotation != null) {
      writer.writeObjectFieldIntro("annotations");
      writer.beginArray();
      for (TokenizedItem<TypeAnnotation.Annotation> token : annotation.annotations) {
        if (token.item.equals != null) {
          writer.beginObject();
          writer.writeObjectFieldIntro(token.item.name.text);
          writer.writeToken(token.item.value);
          writer.endObject();
        } else {
          writer.writeString(token.item.name.text);
        }
      }
      writer.endArray();
    }
  }

  public abstract void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source);

  public TyType withPosition(final DocumentPosition position) {
    reset();
    ingest(position);
    return this;
  }

  public void annotate(TypeAnnotation annotation) {
    this.annotation = annotation;
  }

  @Override
  public TyType getType() {
    return this;
  }
}
