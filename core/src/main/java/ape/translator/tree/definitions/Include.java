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

import java.util.function.Consumer;

/** include a file into the specification */
public class Include extends Definition {
  private final Token include;
  private final Token[] resources;
  private final Token semicolon;
  public final String import_name;

  public Include(Token include, Token[] resources, Token semicolon) {
    this.include = include;
    this.resources = resources;
    this.semicolon = semicolon;
    ingest(include);
    ingest(semicolon);
    StringBuilder name = new StringBuilder();
    for (Token token : resources) {
      name.append(token.text);
    }
    this.import_name = name.toString();
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(include);
    for (Token token : resources) {
      yielder.accept(token);
    }
    yielder.accept(semicolon);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(include);
    formatter.endLine(semicolon);
  }
}
