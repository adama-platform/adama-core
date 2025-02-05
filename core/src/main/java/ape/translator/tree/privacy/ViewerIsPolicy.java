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
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.types.checking.ruleset.RuleSetAsync;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;

import java.util.function.Consumer;

/** a policy that enables the field to be visible if another field is the viewer */
public class ViewerIsPolicy extends Policy {
  public final Token closeToken;
  public final Token fieldToken;
  public final Token openToken;
  public final Token viewerIsToken;

  public ViewerIsPolicy(final Token viewerIsToken, final Token openToken, final Token fieldToken, final Token closeToken) {
    this.viewerIsToken = viewerIsToken;
    this.openToken = openToken;
    this.fieldToken = fieldToken;
    this.closeToken = closeToken;
    ingest(viewerIsToken);
    ingest(closeToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(viewerIsToken);
    yielder.accept(openToken);
    yielder.accept(fieldToken);
    yielder.accept(closeToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public void typing(final Environment environment, final StructureStorage owningStructureStorage) {
    final var fd = owningStructureStorage.fields.get(fieldToken.text);
    if (fd == null) {
      environment.document.createError(this, String.format("Field '%s' was not defined within the record", fieldToken.text));
      return;
    }
    RuleSetAsync.IsPrincipal(environment, fd.type, false);
  }

  @Override
  public boolean writePrivacyCheckGuard(final StringBuilderWithTabs sb, final FieldDefinition field, final Environment environment) {
    sb.append("if (__writer.who.equals(__item.").append(fieldToken.text).append(".get())) {").tabUp().writeNewline();
    return true;
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer) {
    writer.writeString("viewer_is");
  }

  @Override
  public void free(FreeEnvironment environment) {
    environment.require(fieldToken.text);
  }
}
