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
package ape.translator.tree.types.natives;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.traits.assign.AssignmentViaSetter;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailNativeDeclarationIsNotStandard;

import java.util.function.Consumer;

/** the type of an Adama pair which is tightly coupled with map types */
public class TyNativePair extends TyType implements //
    AssignmentViaSetter, //
    DetailHasDeltaType, //
    DetailNativeDeclarationIsNotStandard {
  public final TyType domainType;
  public final TyType rangeType;
  private final Token readonlyToken;
  private final Token pairToken;
  private final Token beginToken;
  private final Token commaToken;
  private final Token endToken;

  public TyNativePair(final TypeBehavior behavior, final Token readonlyToken, final Token pairToken, final Token beginToken, final TyType domainType, final Token commaToken, final TyType rangeType, final Token endToken) {
    super(behavior);
    this.readonlyToken = readonlyToken;
    this.pairToken = pairToken;
    this.beginToken = beginToken;
    this.domainType = domainType;
    this.commaToken = commaToken;
    this.rangeType = rangeType;
    this.endToken = endToken;
    ingest(pairToken);
    ingest(beginToken);
    ingest(endToken);
    ingest(domainType);
    ingest(rangeType);
  }

  @Override
  public void emitInternal(Consumer<Token> yielder) {
    if (readonlyToken != null) {
      yielder.accept(readonlyToken);
    }
    yielder.accept(pairToken);
    yielder.accept(beginToken);
    domainType.emitInternal(yielder);
    yielder.accept(commaToken);
    rangeType.emitInternal(yielder);
    yielder.accept(endToken);
  }

  @Override
  public void format(Formatter formatter) {
    domainType.format(formatter);
    rangeType.format(formatter);
  }

  @Override
  public String getAdamaType() {
    return "pair<" + domainType.getAdamaType() + "," + rangeType.getAdamaType() + ">";
  }

  @Override
  public String getJavaBoxType(Environment environment) {
    return getJavaConcreteType(environment);
  }

  @Override
  public String getJavaConcreteType(Environment environment) {
    TyType resolvedDomain = environment.rules.Resolve(domainType, false);
    TyType resolvedRange = environment.rules.Resolve(rangeType, false);
    return "NtPair<" + resolvedDomain.getJavaBoxType(environment) + "," + resolvedRange.getJavaBoxType(environment) + ">";
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
    return new TyNativePair(newBehavior, readonlyToken, pairToken, beginToken, domainType, commaToken, rangeType, endToken).withPosition(position);
  }

  @Override
  public void typing(Environment environment) {
    domainType.typing(environment);
    rangeType.typing(environment);
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    writer.beginObject();
    writer.writeObjectFieldIntro("nature");
    writer.writeString("native_pair");
    writeAnnotations(writer);
    writer.writeObjectFieldIntro("domain");
    domainType.writeTypeReflectionJson(writer, source);
    writer.writeObjectFieldIntro("range");
    rangeType.writeTypeReflectionJson(writer, source);
    writer.endObject();
  }

  public TyType getDomainType(final Environment environment) {
    return environment.rules.Resolve(domainType, false);
  }

  public TyType getRangeType(final Environment environment) {
    return environment.rules.Resolve(rangeType, false);
  }

  @Override
  public String getDeltaType(Environment environment) {
    return "DPair<" + ((DetailHasDeltaType) domainType).getDeltaType(environment) + "," + ((DetailHasDeltaType) rangeType).getDeltaType(environment) + ">";
  }

  @Override
  public String getPatternWhenValueProvided(Environment environment) {
    return "new NtPair(%s)";
  }

  @Override
  public String getStringWhenValueNotProvided(Environment environment) {
    return "new NtPair(" + domainType.getJavaDefaultValue(environment, this) + "," + rangeType.getJavaDefaultValue(environment, this) + ")";
  }
}
