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
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.CanBumpResult;
import ape.translator.tree.types.natives.TyNativeList;
import ape.translator.tree.types.traits.IsNativeValue;

public class RuleSetBump {
  public static CanBumpResult CanBumpBool(final Environment environment, final TyType typeOriginal, final boolean silent) {
    final var type = RuleSetCommon.Resolve(environment, typeOriginal, silent);
    if (type != null) {
      if (RuleSetCommon.IsBoolean(environment, type, true)) {
        return CanBumpResult.YesWithNative;
      }
      if (type instanceof TyNativeList) {
        final var elementType = RuleSetCommon.ExtractEmbeddedType(environment, type, silent);
        if (elementType != null) {
          if (RuleSetCommon.IsBoolean(environment, elementType, silent)) {
            return CanBumpResult.YesWithListTransformNative;
          }
        }
        return CanBumpResult.No;
      }
      if (!silent) {
        RuleSetCommon.IsBoolean(environment, type, silent);
      }
    }
    return CanBumpResult.No;
  }

  public static CanBumpResult CanBumpNumeric(final Environment environment, final TyType typeOriginal, final boolean silent) {
    final var type = typeOriginal; // RuleSetCommon.ResolvePastLazy(environment, typeOriginal, silent);
    if (type != null) {
      if (RuleSetCommon.IsInteger(environment, type, true) || RuleSetCommon.IsLong(environment, type, true) || RuleSetCommon.IsDouble(environment, type, true)) {
        if (type instanceof IsNativeValue) {
          return CanBumpResult.YesWithNative;
        } else {
          return CanBumpResult.YesWithSetter;
        }
      }
      if (type instanceof TyNativeList) {
        final var elementType = RuleSetCommon.ExtractEmbeddedType(environment, type, silent);
        if (elementType != null) {
          if (RuleSetCommon.IsInteger(environment, elementType, true) || RuleSetCommon.IsLong(environment, elementType, true) || RuleSetCommon.IsDouble(environment, elementType, true)) {
            if (elementType instanceof IsNativeValue) {
              return CanBumpResult.YesWithListTransformNative;
            } else {
              return CanBumpResult.YesWithListTransformSetter;
            }
          }
        }
      }
      if (!silent) {
        environment.document.createError(typeOriginal, String.format("Type check failure: Must have a type of 'int', 'long', 'double', 'list<int>', 'list<long>', 'list<double>'; instead got '%s'", typeOriginal.getAdamaType()));
      }
    }
    return CanBumpResult.No;
  }
}
