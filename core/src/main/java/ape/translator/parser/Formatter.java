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

import ape.translator.parser.token.Token;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Formatter {
  private static String[] TAB_CACHE = makeTabCache();

  private static String[] makeTabCache() {
    String current = "";
    String[] cache = new String[40];
    for (int k = 0; k < cache.length; k++) {
      cache[k] = current;
      current += "  ";
    }
    return cache;
  }

  private int tab;
  private String tabCache;

  private void updateTab() {
    if (0 <= tab && tab < TAB_CACHE.length) {
      tabCache = TAB_CACHE[tab];
    } else {
      tabCache = "";
      for (int k = 0; k < tab; k++) {
        tabCache += "  ";
      }
    }
  }

  public Formatter() {
    this.tab = 0;
    updateTab();
  }

  public void tabUp() {
    this.tab++;
    updateTab();
  }

  public void startLine(Token t) {
    if (t.nonSemanticTokensPrior == null) {
      t.nonSemanticTokensPrior = new ArrayList<>();
      t.nonSemanticTokensPrior.add(Token.WRAP(tabCache));
    }
  }

  public void endLine(Token t) {
    if (t.nonSemanticTokensAfter == null) {
      t.nonSemanticTokensAfter = new ArrayList<>();
    }
    t.nonSemanticTokensAfter.add(Token.WS("\n"));
  }

  public void tabDown() {
    this.tab--;
    updateTab();
  }

  public static class FirstAndLastToken implements Consumer<Token> {
    public Token first;
    public Token last;

    public FirstAndLastToken() {
      this.first = null;
      this.last = null;
    }

    @Override
    public void accept(Token token) {
      if (first == null) {
        first = token;
      }
      last = token;
    }
  }

}
