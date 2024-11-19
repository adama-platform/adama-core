/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
