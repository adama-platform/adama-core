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
package ape.translator.tree.definitions.client;

import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.definitions.Definition;

import java.util.function.Consumer;

/** defines an endpoint for reaching an HTTP server */
public class Endpoint extends Definition {
  public final Token token;
  public final Token begin;
  public final Token version;
  public final Token end;
  public final Token value;
  public final Token semicolon;

  public Endpoint(Token token, Token begin, Token version, Token end, Token value, Token semicolon) {
    this.token = token;
    this.begin = begin;
    this.version = version;
    this.end = end;
    this.value = value;
    this.semicolon = semicolon;
    ingest(token);
    ingest(semicolon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(token);
    if (begin != null) {
      yielder.accept(begin);
      yielder.accept(version);
      yielder.accept(end);
    }
    yielder.accept(value);
    yielder.accept(semicolon);
  }

  @Override
  public void format(Formatter formatter) {

  }

  public String version() {
    if (version != null) {
      return version.text;
    }
    return "prod";
  }
}
