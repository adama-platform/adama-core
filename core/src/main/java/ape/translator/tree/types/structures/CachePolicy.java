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
package ape.translator.tree.types.structures;

import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;

import java.util.function.Consumer;

/** a policy that controls how long an item is cached for (once/every-X-time-unit/etc...) */
public class CachePolicy extends DocumentPosition {
  public Token open;
  public Token type;

  // if type == every
  public Token count;
  public Token unit;

  public Token close;

  public CachePolicy(Token open, Token type, Token count, Token unit, Token close) {
    this.open = open;
    this.type = type;
    this.count = count;
    this.unit = unit;
    this.close = close;
    ingest(open);
  }

  public void emit(final Consumer<Token> yielder) {
    yielder.accept(open);
    yielder.accept(type);
    if (count != null) {
      yielder.accept(count);
      yielder.accept(unit);
    }
    yielder.accept(close);
  }

  public void format(Formatter formatter) {

  }

  public long toSeconds() {
    if (count == null) {
      return -1;
    } else {
      int v = Integer.parseInt(count.text);
      switch (unit.text) {
        case "hr":
        case "hour":
        case "hours":
          return v * 60 * 60;
        case "min":
        case "minute":
        case "minutes":
          return v * 60;
        default:
          return v; // seconds
      }
    }
  }
}
