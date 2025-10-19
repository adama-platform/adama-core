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
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.CanTestEqualityResult;
import ape.translator.tree.types.traits.IsEnum;

public class RuleSetEquality {
  public static CanTestEqualityResult CanTestEquality(final Environment environment, final TyType typeA, final TyType typeB, final boolean silent) {
    if (typeA != null && typeB != null) {
      final var aInteger = RuleSetCommon.IsInteger(environment, typeA, true);
      final var bInteger = RuleSetCommon.IsInteger(environment, typeB, true);
      if (aInteger && bInteger) {
        return CanTestEqualityResult.Yes;
      }
      final var aComplex = RuleSetCommon.IsComplex(environment, typeA, true);
      final var bComplex = RuleSetCommon.IsComplex(environment, typeB, true);
      if (aComplex && bComplex) {
        return CanTestEqualityResult.YesButViaNear;
      }
      final var aDate = RuleSetCommon.IsDate(environment, typeA, true);
      final var bDate = RuleSetCommon.IsDate(environment, typeB, true);
      if (aDate && bDate) {
        return CanTestEqualityResult.Yes;
      }
      final var aDateTime = RuleSetCommon.IsDateTime(environment, typeA, true);
      final var bDateTime = RuleSetCommon.IsDateTime(environment, typeB, true);
      if (aDateTime && bDateTime) {
        return CanTestEqualityResult.Yes;
      }
      final var aTime = RuleSetCommon.IsTime(environment, typeA, true);
      final var bTime = RuleSetCommon.IsTime(environment, typeB, true);
      if (aTime && bTime) {
        return CanTestEqualityResult.Yes;
      }
      final var aTimeSpan = RuleSetCommon.IsTimeSpan(environment, typeA, true);
      final var bTimeSpan = RuleSetCommon.IsTimeSpan(environment, typeB, true);
      if (aTimeSpan && bTimeSpan) {
        return CanTestEqualityResult.Yes;
      }
      final var aLong = RuleSetCommon.IsLong(environment, typeA, true);
      final var bLong = RuleSetCommon.IsLong(environment, typeB, true);
      if ((aInteger || aLong) && (bInteger || bLong)) {
        return CanTestEqualityResult.Yes;
      }
      final var aNumber = RuleSetCommon.IsNumeric(environment, typeA, true) || aLong || aComplex;
      final var bNumber = RuleSetCommon.IsNumeric(environment, typeB, true) || bLong || bComplex;
      if (aNumber && bNumber) {
        // a mix of int/double
        return CanTestEqualityResult.YesButViaNear;
      }
      {
        final var aVec2 = RuleSetVector.IsVec2(environment, typeA);
        final var bVec2 = RuleSetVector.IsVec2(environment, typeB);
        if (aVec2 && bVec2) {
          return CanTestEqualityResult.YesButViaNear;
        }
        final var aVec3 = RuleSetVector.IsVec3(environment, typeA);
        final var bVec3 = RuleSetVector.IsVec3(environment, typeB);
        if (aVec3 && bVec3) {
          return CanTestEqualityResult.YesButViaNear;
        }
        final var aVec4 = RuleSetVector.IsVec4(environment, typeA);
        final var bVec4 = RuleSetVector.IsVec4(environment, typeB);
        if (aVec4 && bVec4) {
          return CanTestEqualityResult.YesButViaNear;
        }
        final var aMat2 = RuleSetMatrix.IsMatrix2(environment, typeA);
        final var bMat2 = RuleSetMatrix.IsMatrix2(environment, typeB);
        if (aMat2 && bMat2) {
          return CanTestEqualityResult.YesButViaNear;
        }
        final var aMat3 = RuleSetMatrix.IsMatrix3(environment, typeA);
        final var bMat3 = RuleSetMatrix.IsMatrix3(environment, typeB);
        if (aMat3 && bMat3) {
          return CanTestEqualityResult.YesButViaNear;
        }
        final var aMat4 = RuleSetMatrix.IsMatrix4(environment, typeA);
        final var bMat4 = RuleSetMatrix.IsMatrix4(environment, typeB);
        if (aMat4 && bMat4) {
          return CanTestEqualityResult.YesButViaNear;
        }
        final var aMatH4 = RuleSetMatrix.IsMatrixH4(environment, typeA);
        final var bMatH4 = RuleSetMatrix.IsMatrixH4(environment, typeB);
        if (aMatH4 && bMatH4) {
          return CanTestEqualityResult.YesButViaNear;
        }
      }

      final var aDynamic = RuleSetCommon.IsDynamic(environment, typeA, true);
      final var bDynamic = RuleSetCommon.IsDynamic(environment, typeB, true);
      if (aDynamic && bDynamic) {
        return CanTestEqualityResult.Yes;
      }
      final var aAsset = RuleSetCommon.IsAsset(environment, typeA, true);
      final var bAsset = RuleSetCommon.IsAsset(environment, typeB, true);
      if (aAsset && bAsset) {
        return CanTestEqualityResult.Yes;
      }
      final var aString = RuleSetCommon.IsString(environment, typeA, true);
      final var bString = RuleSetCommon.IsString(environment, typeB, true);
      if (aString && bString) {
        return CanTestEqualityResult.Yes;
      }
      final var aBool = RuleSetCommon.IsBoolean(environment, typeA, true);
      final var bBool = RuleSetCommon.IsBoolean(environment, typeB, true);
      if (aBool && bBool) {
        return CanTestEqualityResult.Yes;
      }
      final var aStateMachineRef = RuleSetStateMachine.IsStateMachineRef(environment, typeA, true);
      final var bStateMachineRef = RuleSetStateMachine.IsStateMachineRef(environment, typeB, true);
      if (aStateMachineRef && bStateMachineRef) {
        return CanTestEqualityResult.Yes;
      }
      final var aClient = RuleSetAsync.IsPrincipal(environment, typeA, true) || RuleSetAsync.IsSecurePrincipal(environment, typeA, true);
      final var bClient = RuleSetAsync.IsPrincipal(environment, typeB, true) || RuleSetAsync.IsSecurePrincipal(environment, typeB, true);
      if (aClient && bClient) {
        return CanTestEqualityResult.Yes;
      }
      final var aEnum = RuleSetEnums.IsEnum(environment, typeA, true);
      final var bEnum = RuleSetEnums.IsEnum(environment, typeB, true);
      if (aEnum && bEnum) {
        if (((IsEnum) typeA).name().equals(((IsEnum) typeB).name())) {
          return CanTestEqualityResult.Yes;
        } else if (!silent) {
          environment.document.createError(DocumentPosition.sum(typeA, typeB), String.format("Type check failure: enum types are incompatible '%s' vs '%s'.", typeA.getAdamaType(), typeB.getAdamaType()));
        }
        return CanTestEqualityResult.No;
      }
      if (!silent) {
        environment.document.createError(DocumentPosition.sum(typeA, typeB), String.format("Type check failure: unable to compare types '%s' and '%s' for equality.", typeA.getAdamaType(), typeB.getAdamaType()));
      }
    }
    return CanTestEqualityResult.No;
  }
}
