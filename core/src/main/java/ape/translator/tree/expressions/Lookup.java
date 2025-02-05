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
package ape.translator.tree.expressions;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.TyNativeGlobalObject;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;

import java.util.function.Consumer;

/** look up a variable in the current environment and then extract its value */
public class Lookup extends Expression {
  public final Token variableToken;
  private boolean addGet;
  private boolean hide;

  /** the variable to look up */
  public Lookup(final Token variableToken) {
    this.variableToken = variableToken;
    hide = false;
    addGet = false;
    ingest(variableToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(variableToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    var type = environment.lookup(variableToken.text, environment.state.isContextComputation(), this, false);

    if (type instanceof TyNativeGlobalObject) {
      hide = true;
      return type;
    }
    if (type == null) {
      environment.document.createError(this, String.format("The variable '%s' was not defined", variableToken.text));
    }
    if (type != null && environment.state.isContextComputation() && type instanceof DetailComputeRequiresGet) {
      addGet = true;
      type = ((DetailComputeRequiresGet) type).typeAfterGet(environment);
      if (type != null) {
        type = type.makeCopyWithNewPosition(this, type.behavior);
      }
    }
    return type;
  }

  /**
   * note: the context matters here. If we are assigning, then we must return a relevant mode of
   * assignment to the underlying variable.
   */
  @Override
  // move the context into the environment
  public void writeJava(final StringBuilder sb, final Environment environment) {
    environment.lookup(variableToken.text, environment.state.isContextComputation(), this, true);
    if (!hide) {
      sb.append(variableToken.text);
      if (addGet) {
        sb.append(".get()");
      }
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    environment.require(variableToken.text);
  }
}
