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
package ape.translator.tree.definitions;

import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

/** link a known service into the specification */
public class LinkService extends Definition {
  private final Token link;
  public final Token name;
  private final Token open;
  private final Token close;
  private final ArrayList<Consumer<Consumer<Token>>> emission;
  public final ArrayList<DefineService.ServiceAspect> aspects;

  public LinkService(Token link, Token name, Token open, ArrayList<Consumer<Consumer<Token>>> emission, ArrayList<DefineService.ServiceAspect> aspects, Token close) {
    this.link = link;
    this.name = name;
    this.open = open;
    this.emission = emission;
    this.aspects = aspects;
    this.close = close;
    ingest(link);
    ingest(close);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(link);
    yielder.accept(name);
    yielder.accept(open);
    for (Consumer<Consumer<Token>> emitter : emission) {
       emitter.accept(yielder);
    }
    yielder.accept(close);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(link);
    formatter.endLine(close);
  }

  public String toParams() {
    StringBuilder sb = new StringBuilder();
    for (Consumer<Consumer<Token>> emitter : emission) {
      emitter.accept((t) -> {
        if (t.nonSemanticTokensPrior != null) {
          for (Token prior : t.nonSemanticTokensPrior) {
            sb.append(prior.text);
          }
        }
        sb.append(t.text);
        if (t.nonSemanticTokensAfter != null) {
          for (Token next : t.nonSemanticTokensAfter) {
            sb.append(next.text);
          }
        }
      });
    }
    return sb.toString();
  }

  public HashSet<String> names() {
    HashSet<String> names = new HashSet<>();
    for (DefineService.ServiceAspect aspect : aspects) {
      names.add(aspect.name.text);
    }
    return names;
  }
}
