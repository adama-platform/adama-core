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
package ape.translator.tree.types;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.natives.TyNativeTable;
import ape.translator.tree.types.reactive.TyReactiveTable;
import ape.translator.tree.types.traits.details.DetailRequiresResolveCall;

import java.util.function.Consumer;

/** for passing a table in via an argument */
public class TyTablePtr extends TyType implements DetailRequiresResolveCall {

  public final String nameTokenText;
  public final Token readonlyToken;
  public final Token tableToken;
  public final TokenizedItem<Token> nameToken;

  public TyTablePtr(final TypeBehavior behavior, final Token readonlyToken, final Token tableToken, final TokenizedItem<Token> nameToken) {
    super(behavior);
    this.tableToken = tableToken;
    this.readonlyToken = readonlyToken;
    this.nameToken = nameToken;
    nameTokenText = nameToken.item.text;
    ingest(tableToken);
    ingest(nameToken.item);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    if (readonlyToken != null) {
      yielder.accept(readonlyToken);
    }
    yielder.accept(tableToken);
    nameToken.emitBefore(yielder);
    yielder.accept(nameToken.item);
    nameToken.emitAfter(yielder);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getAdamaType() {
    return "table<" + nameTokenText + ">";
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return resolve(environment).getJavaBoxType(environment);
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    return getJavaBoxType(environment);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyTablePtr(newBehavior, readonlyToken, tableToken, nameToken).withPosition(position);
  }

  @Override
  public void typing(Environment environment) {
    resolve(environment).typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("table_ref");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("record_name");
    writer.writeString(nameTokenText);
    writer.endObject();
  }

  @Override
  public TyType resolve(Environment environment) {
    TyType subType = environment.document.types.get(nameTokenText);
    if (subType != null && subType instanceof TyNativeMessage) {
      return new TyNativeTable(behavior, readonlyToken, tableToken, nameToken);
    } else {
      return new TyReactiveTable(subType.behavior.isReadOnly, tableToken, nameToken);
    }
  }
}
