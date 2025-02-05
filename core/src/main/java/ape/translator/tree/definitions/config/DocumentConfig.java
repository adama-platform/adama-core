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
package ape.translator.tree.definitions.config;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeInteger;

import java.util.function.Consumer;

/** there are operational parameters which are embedded with the document */
public class DocumentConfig extends StaticPiece {
  public final Token name;
  public final Token equals;
  public final Expression value;
  public final Token semicolon;

  public DocumentConfig(Token name, Token equals, Expression value, Token semicolon) {
    this.name = name;
    this.equals = equals;
    this.value = value;
    this.semicolon = semicolon;
    ingest(name, equals, semicolon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(name);
    yielder.accept(equals);
    value.emit(yielder);
    yielder.accept(semicolon);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(name);
    value.format(formatter);
    formatter.endLine(semicolon);
  }

  @Override
  public void typing(Environment environment) {
    Environment next = environment.scopeWithComputeContext(ComputeContext.Computation);
    switch (name.text) {
      case "maximum_history":
      case "frequency":
      case "temporal_resolution_ms":
        next.rules.IsInteger(value.typing(next, new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null)), false);
        return;
      case "delete_on_close":
      case "readonly":
        next.rules.IsBoolean(value.typing(next, new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null)), false);
        return;
    }
  }
}
