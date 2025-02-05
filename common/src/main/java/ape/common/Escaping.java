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
package ape.common;

import java.util.PrimitiveIterator;

/** escaping strings per various rules via flags */
public class Escaping {
  private final String str;
  private boolean escapeDoubleQuote = true;
  private boolean escapeSingleQuote = false;
  private boolean removeReturns = true;
  private boolean escapeReturns = true;
  private boolean keepSlashes = false;
  private boolean removeNewLines = false;

  public Escaping(String str) {
    this.str = str;
  }

  public Escaping switchQuotes() {
    escapeDoubleQuote = !escapeDoubleQuote;
    escapeSingleQuote = !escapeSingleQuote;
    return this;
  }

  public Escaping keepReturns() {
    removeReturns = false;
    return this;
  }

  public Escaping dontEscapeReturns() {
    escapeReturns = false;
    return this;
  }

  public Escaping keepSlashes() {
    this.keepSlashes = true;
    return this;
  }

  public Escaping removeNewLines() {
    this.removeNewLines = true;
    return this;
  }

  @Override
  public String toString() {
    return go();
  }

  public String go() {
    StringBuilder result = new StringBuilder();
    PrimitiveIterator.OfInt it = str.codePoints().iterator();
    while (it.hasNext()) {
      int cp = it.nextInt();
      switch (cp) {
        case '\n':
          if (!removeNewLines) {
            result.append("\\n");
          }
          break;
        case '\\':
          if (keepSlashes) {
            result.append("\\");
          } else {
            result.append("\\\\");
          }
          break;
        case '"':
          if (escapeDoubleQuote) {
            result.append("\\\"");
          } else {
            result.append("\"");
          }
          break;
        case '\'':
          if (escapeSingleQuote) {
            result.append("\\'");
          } else {
            result.append("'");
          }
          break;
        case '\r':
          if (!removeReturns) {
            if (escapeReturns) {
              result.append("\\r");
            } else {
              result.append("\r");
            }
          }
          break;
        default:
          result.append(Character.toChars(cp));
      }
    }
    return result.toString();
  }
}
