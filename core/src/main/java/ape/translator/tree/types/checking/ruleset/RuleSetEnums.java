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
package ape.translator.tree.types.checking.ruleset;

import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeEnum;
import ape.translator.tree.types.shared.EnumStorage;
import ape.translator.tree.types.traits.IsEnum;

public class RuleSetEnums {
  public static IsEnum FindEnumType(final Environment environment, final String name, final DocumentPosition position, final boolean silent) {
    final var type = environment.document.types.get(name);
    if (type != null) {
      if (type instanceof IsEnum) {
        return (IsEnum) type.makeCopyWithNewPosition(position, type.behavior);
      } else if (!silent) {
        environment.document.createError(position, String.format("Type incorrect: expecting '%s' to be an enumeration; instead, found a type of '%s'.", name, type.getAdamaType()));
      }
    } else if (!silent) {
      environment.document.createError(position, String.format("Type not found: an enumeration named '%s' was not found.", name));
    }
    return null;
  }

  public static boolean IsEnum(final Environment environment, final TyType tyTypeOriginal, final boolean silent) {
    final var tyType = RuleSetCommon.Resolve(environment, tyTypeOriginal, silent);
    if (tyType != null) {
      if (tyType instanceof IsEnum) {
        return true;
      }
      RuleSetCommon.SignalTypeFailure(environment, new TyNativeEnum(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("enum<?>"), null, new EnumStorage("?"), null), tyTypeOriginal, silent);
    }
    return false;
  }
}
