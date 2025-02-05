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
import ape.runtime.natives.NtPrincipal;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class TyInternalReadonlyClass extends TyType {
  private final Class<?> clazz;

  public TyInternalReadonlyClass(Class<?> clazz) {
    super(TypeBehavior.ReadOnlyNativeValue);
    this.clazz = clazz;
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    throw new UnsupportedOperationException("internal types can't be emitted");
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getAdamaType() {
    return "internal<" + clazz.getSimpleName() + ">";
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return clazz.getName();
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    return clazz.getName();
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyInternalReadonlyClass(this.clazz).withPosition(position);
  }

  @Override
  public void typing(Environment environment) {
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    throw new UnsupportedOperationException("internal types can't be reflected");
  }

  public TyType getLookupType(Environment environment, String field) {
    try {
      Field fType = clazz.getField(field);
      if (fType.getType() == String.class) {
        return new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("string"));
      } else if (fType.getType() == NtPrincipal.class) {
        return new TyNativePrincipal(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("client"));
      } else {
        environment.document.createError(this, "Field '" + field + "' had a type we didn't recognize in internal type: " + clazz.getSimpleName());
        return null;
      }
    } catch (Exception ex) {
      environment.document.createError(this, "Field '" + field + "' was not found in internal type: " + clazz.getSimpleName());
      return null;
    }
  }
}
