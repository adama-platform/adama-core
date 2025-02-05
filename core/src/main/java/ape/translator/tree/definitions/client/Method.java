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

/** a single method invocation against an HTTP uri */
public class Method extends Definition {
  public final Token method;
  public final Token uri_str;
  public final Token open_type;
  public final Token type_query;
  public final Token comma;
  public final Token type_body;
  public final Token close_type;
  public final Token name;
  public final Token arrow;
  public final Token return_type;
  public final Token semicolon;

  public Method(final Token method, Token uri_str, Token open_type, Token type_query, Token comma, Token type_body, Token close_type, Token name, Token arrow, Token return_type, Token semicolon) {
    this.method = method;
    this.uri_str = uri_str;
    this.open_type = open_type;
    this.type_query = type_query;
    this.comma = comma;
    this.type_body = type_body;
    this.close_type = close_type;
    this.name = name;
    this.semicolon = semicolon;
    this.arrow = arrow;
    this.return_type = return_type;
    ingest(method);
    ingest(uri_str);
    ingest(semicolon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(method);
    yielder.accept(uri_str);
    if (open_type != null) {
      yielder.accept(open_type);
      yielder.accept(type_query);
      if (comma != null) {
        yielder.accept(comma);
        yielder.accept(type_body);
      }
      yielder.accept(close_type);
    }
    yielder.accept(name);
    if (arrow != null) {
      yielder.accept(arrow);
      yielder.accept(return_type);
    }
    yielder.accept(semicolon);
  }

  @Override
  public void format(Formatter formatter) {
  }
}
