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
package ape.translator.tree.privacy;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;

import java.util.ArrayList;
import java.util.function.Consumer;

/** a way to indicate that a bubble is protected by data */
public class Guard extends DocumentPosition {
  public final Token open;
  public final ArrayList<TokenizedItem<String>> policies;
  public final Token switchToFilter;
  public final ArrayList<TokenizedItem<String>> filters;
  public final Token close;

  public Guard(Token open, ArrayList<TokenizedItem<String>> policies, Token switchToFilter, ArrayList<TokenizedItem<String>> filters, Token close) {
    this.open = open;
    this.policies = policies;
    this.switchToFilter = switchToFilter;
    this.filters = filters;
    this.close = close;
    ingest(open);
    ingest(close);
  }

  public void emit(final Consumer<Token> yielder) {
    yielder.accept(open);
    for (TokenizedItem<String> policy : policies) {
      policy.emitBefore(yielder);
      policy.emitAfter(yielder);
    }
    if (switchToFilter != null) {
      yielder.accept(switchToFilter);
      for (TokenizedItem<String> policy : filters) {
        policy.emitBefore(yielder);
        policy.emitAfter(yielder);
      }
    }
    yielder.accept(close);
  }

  public void format(Formatter formatter) {
  }

  public void writeReflect(JsonStreamWriter writer) {
    writer.beginArray();
    for (TokenizedItem<String> policy : policies) {
      writer.writeString(policy.item);
    }
    writer.endArray();
  }
}
