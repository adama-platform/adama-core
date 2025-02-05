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

import ape.translator.codegen.CodeGenEnums;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.LatentCodeSnippet;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeArray;
import ape.translator.tree.types.shared.EnumStorage;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class EnumValuesArray extends Expression implements LatentCodeSnippet {
  public final Token colonsToken;
  public final String enumTypeName;
  public final Token enumTypeNameToken;
  public final Token prefixToken;
  public final Token starToken;
  private int prefixCachedID;
  private EnumStorage storage;

  /**
   * The enumeration value
   * @param enumTypeNameToken the token for the type
   */
  public EnumValuesArray(final Token enumTypeNameToken, final Token colonsToken, final Token prefixToken, final Token starToken) {
    this.enumTypeNameToken = enumTypeNameToken;
    this.colonsToken = colonsToken;
    this.prefixToken = prefixToken;
    this.starToken = starToken;
    enumTypeName = enumTypeNameToken.text;
    ingest(enumTypeNameToken);
    ingest(starToken);
    prefixCachedID = 0;
    storage = null;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(enumTypeNameToken);
    yielder.accept(colonsToken);
    if (prefixToken != null) {
      yielder.accept(prefixToken);
    }
    yielder.accept(starToken);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    environment.mustBeComputeContext(this);
    final var isEnum = environment.rules.FindEnumType(enumTypeName, this, false);
    if (isEnum != null) {
      if (prefixToken != null) {
        prefixCachedID = environment.autoVariable();
        storage = isEnum.storage();
        environment.document.add(this);
      }
      return new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, (TyType) isEnum, null).withPosition(this);
    }
    return null;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    if (prefixToken == null) {
      sb.append("__ALL_VALUES_").append(enumTypeName);
    } else {
      sb.append("__").append(prefixToken.text).append(prefixCachedID).append("_").append(enumTypeName);
    }
  }

  public ArrayList<Integer> values(Environment environment) {
    final var isEnum = environment.rules.FindEnumType(enumTypeName, this, false);
    if (isEnum != null) {
      ArrayList<Integer> values = new ArrayList<>();
      for (Map.Entry<String, Integer> entry : isEnum.storage().options.entrySet()) {
        if (prefixToken == null) {
          values.add(entry.getValue());
        } else if (entry.getKey().startsWith(prefixToken.text)) {
          values.add(entry.getValue());
        }
      }
      return values;
    }
    return null;
  }

  @Override
  public void writeLatentJava(final StringBuilderWithTabs sb) {
    CodeGenEnums.writeEnumArray(sb, enumTypeName, prefixToken.text + prefixCachedID, prefixToken.text, storage);
  }

  @Override
  public void free(FreeEnvironment environment) {
  }
}
