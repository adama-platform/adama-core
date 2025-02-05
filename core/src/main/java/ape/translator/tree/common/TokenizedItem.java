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
package ape.translator.tree.common;

import ape.translator.parser.token.Token;

import java.util.ArrayList;
import java.util.function.Consumer;

/** this allows us to wrap anything with tokens either before the item or after. */
public class TokenizedItem<T> {
  public final ArrayList<Token> after;
  public final ArrayList<Token> before;
  public final T item;

  public TokenizedItem(final T item) {
    this.before = new ArrayList<>();
    this.item = item;
    this.after = new ArrayList<>();
  }

  public void after(final Token token) {
    this.after.add(token);
  }

  public void before(final Token token) {
    this.before.add(token);
  }

  public void emitAfter(final Consumer<Token> yielder) {
    for (final Token b : after) {
      yielder.accept(b);
    }
  }

  public void emitBefore(final Consumer<Token> yielder) {
    for (final Token b : before) {
      yielder.accept(b);
    }
  }
}
