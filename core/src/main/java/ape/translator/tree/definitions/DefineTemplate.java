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
import ape.translator.tree.expressions.constants.TemplateConstant;

import java.util.function.Consumer;

/** defines a template with a document */
public class DefineTemplate extends Definition {
  public final Token templateToken;
  public final Token nameToken;
  public final Token colonToken;
  public final TemplateConstant value;

  public DefineTemplate(Token templateToken, Token nameToken, Token colonToken, TemplateConstant value) {
    this.templateToken = templateToken;
    this.nameToken = nameToken;
    this.colonToken = colonToken;
    this.value = value;
    ingest(templateToken);
    ingest(value);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(templateToken);
    yielder.accept(nameToken);
    yielder.accept(colonToken);
    value.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(templateToken);
    value.format(formatter);
  }
}
