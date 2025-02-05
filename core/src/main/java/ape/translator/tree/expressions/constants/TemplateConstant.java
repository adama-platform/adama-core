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
package ape.translator.tree.expressions.constants;

import ape.common.Escaping;
import ape.common.template.Parser;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.TyNativeTemplate;

import java.util.function.Consumer;

/** constant templates */
public class TemplateConstant extends Expression {
  private final Token token;

  public TemplateConstant(Token token) {
    this.token = token;
    ingest(token);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(token);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.endLine(token);
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    try {
      Parser.parse(raw());
    } catch (Exception ex) {
      environment.document.createError(this, "the template doesn't parse");
    }
    return new TyNativeTemplate(token);
  }

  @Override
  public void free(FreeEnvironment environment) {
  }

  private String raw() {
    String text = token.text;
    int kSecond = text.indexOf('`', 1);
    return text.substring(kSecond + 1, text.length() - kSecond * 2 + 1);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    sb.append("new NtTemplate(\"").append(new Escaping(raw()).go()).append("\")");
  }
}
