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
package ape.translator.parser.exceptions;

import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;

/** an issue happened when building the tree */
public class ParseException extends AdamaLangException {
  public final String rawMessage;
  public final Token token;

  public ParseException(final String message, final Token token) {
    super(messageOf(message, token));
    rawMessage = message;
    this.token = token;
  }

  /** helpful to convert tokens into a string for the parent exception */
  private static String messageOf(final String message, final Token token) {
    final var sb = new StringBuilder();
    sb.append(message);
    if (token != null) {
      sb.append(token.toExceptionMessageTrailer());
    }
    return sb.toString();
  }

  /** this is the position within the tree */
  public DocumentPosition toDocumentPosition() {
    final var dp = new DocumentPosition();
    if (token != null) {
      dp.ingest(token);
    }
    return dp;
  }
}
