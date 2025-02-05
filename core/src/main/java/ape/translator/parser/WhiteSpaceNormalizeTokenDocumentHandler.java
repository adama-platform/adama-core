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
package ape.translator.parser;

import ape.translator.parser.token.MajorTokenType;
import ape.translator.parser.token.Token;

import java.util.ArrayList;

public class WhiteSpaceNormalizeTokenDocumentHandler extends TokenDocumentHandler {
  public final StringBuilder builder = new StringBuilder();

  private static ArrayList<Token> normalize(ArrayList<Token> list) {
    if (list == null) {
      return null;
    }
    ArrayList<Token> next = new ArrayList<>();
    for(Token token : list) {
      if (token.majorType == MajorTokenType.Whitespace) {
        next.add(token.cloneWithNewText(" "));
      } else {
        next.add(token);
      }
    }
    return next;
  }

  private static ArrayList<Token> remove(ArrayList<Token> list) {
    if (list == null) {
      return null;
    }
    ArrayList<Token> next = new ArrayList<>();
    for(Token token : list) {
      if (token.majorType != MajorTokenType.Whitespace) {
        next.add(token);
      }
    }
    return next;
  }

  public static void normalize(Token token) {
    token.nonSemanticTokensAfter = normalize(token.nonSemanticTokensAfter);
    token.nonSemanticTokensPrior = normalize(token.nonSemanticTokensPrior);
  }

  public static void remove(Token token) {
    token.nonSemanticTokensAfter = remove(token.nonSemanticTokensAfter);
    token.nonSemanticTokensPrior = remove(token.nonSemanticTokensPrior);
  }

  @Override
  public void accept(final Token token) {
    if (token.isSymbolWithTextEq(";")) {
      remove(token);
    } else {
      normalize(token);
    }
  }
}
