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
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.traits.DetailNeverPublic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TyNativeFunctional extends TyType implements DetailNeverPublic {
  public final String name;
  public final ArrayList<FunctionOverloadInstance> overloads;
  public final FunctionStyleJava style;

  public TyNativeFunctional(final String name, final ArrayList<FunctionOverloadInstance> overloads, final FunctionStyleJava style) {
    super(TypeBehavior.ReadOnlyNativeValue);
    this.name = name;
    this.overloads = overloads;
    this.style = style;
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
    return "$<" + name + ">";
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeFunctional(name, overloads, style).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
    for (final FunctionOverloadInstance fo : overloads) {
      fo.typing(environment);
    }
    for (var k = 0; k < overloads.size(); k++) {
      for (var j = k + 1; j < overloads.size(); j++) {
        overloads.get(k).testOverlap(overloads.get(j), environment);
      }
    }
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_functional");
    writeAnnotations(writer);
    writer.endObject();
  }

  /** to help typing anonymous messages to existing messages */
  public FunctionOverloadInstance guess(int count) {
    if (overloads.size() == 1) {
      if (overloads.get(0).types.size() == count) {
        return overloads.get(0);
      }
    }
    return null;
  }


  /** find the right instance basedd on the given types */
  public FunctionOverloadInstance find(final DocumentPosition position, final ArrayList<TyType> argTypes, final Environment environment) {
    var result = overloads.get(0);
    var score = (argTypes.size() + 1) * (argTypes.size() + 1);
    for (final FunctionOverloadInstance candidate : overloads) {
      final var testScore = candidate.score(environment, argTypes);
      if (testScore < score) {
        score = testScore;
        result = candidate;
      }
    }
    result.test(position, environment, argTypes);
    return result;
  }

  public Set<String> gatherDependencies() {
    HashSet<String> set = new HashSet<>();
    for (FunctionOverloadInstance foi : overloads) {
      set.addAll(foi.dependencies);
    }
    return set;
  }

  public Set<String> gatherAssocs() {
    HashSet<String> set = new HashSet<>();
    for (FunctionOverloadInstance foi : overloads) {
      set.addAll(foi.assocs);
    }
    return set;
  }
}
