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
package ape.translator.tree.types.natives.functions;

import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;

import java.util.function.Consumer;

/** painting of a function with various properties */
public class FunctionPaint {
  private static final Token[] EMPTY_TOKENS = new Token[0];
  private final Token[] tokens;
  public final boolean pure;
  public final boolean castArgs;
  public final boolean castReturn;
  public final boolean aborts;
  public final boolean viewer;

  public FunctionPaint(final boolean pure, final boolean castArgs, final boolean castReturn, final boolean aborts) {
    this.tokens = EMPTY_TOKENS;
    this.pure = pure;
    this.castArgs = castArgs;
    this.castReturn = castReturn;
    this.aborts = aborts;
    this.viewer = false;
  }

  public FunctionPaint(Token... tokens) {
    this.tokens = tokens;
    boolean _pure = false;
    boolean _aborts = false;
    boolean _viewer = false;
    for (Token token : tokens) {
      if (token.text.equals("readonly")) {
        _pure = true;
      }
      if (token.text.equals("aborts")) {
        _aborts = true;
      }
      if (token.text.equals("viewer")) {
        _viewer = true;
      }
    }
    this.pure = _pure;
    this.castArgs = false;
    this.castReturn = false;
    this.aborts = _aborts;
    this.viewer = _viewer;
  }

  public void emit(Consumer<Token> yielder) {
    for (Token token : tokens) {
      yielder.accept(token);
    }
  }

  public void format(Formatter formatter) {
  }

  public static final FunctionPaint READONLY_NORMAL = new FunctionPaint(true, false, false, false);
  public static final FunctionPaint CAST_NORMAL = new FunctionPaint(false, true, false, false);
  public static final FunctionPaint NORMAL = new FunctionPaint(false, false, false, false);
  public static final FunctionPaint READONLY_CAST = new FunctionPaint(true, true, false, false);
}
