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
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.natives.TyNativeReactiveRecordPtr;
import ape.translator.tree.types.reactive.TyReactiveMap;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.IsMap;
import ape.translator.tree.types.traits.IsStructure;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.Map;

public class RuleSetIngestion {
  public static boolean CanAIngestB(final Environment environment, final TyType originalTypeA, final TyType originalTypeB, final boolean silent) {
    final var typeLeft = environment.rules.ResolvePtr(originalTypeA, silent);
    final var typeRight = environment.rules.ResolvePtr(originalTypeB, silent);
    if (typeLeft == null || typeRight == null) {
      return false;
    }
    if (RuleSetMap.IsMap(environment, typeLeft) && RuleSetMap.IsMap(environment, typeRight)) {
      if (RuleSetAssignment.CanTypeAStoreTypeB(environment, ((IsMap) typeLeft).getDomainType(environment), ((IsMap) typeRight).getDomainType(environment), StorageTweak.None, silent)) {
        if (RuleSetIngestion.IngestionLeftSidePossible(environment, ((IsMap) typeLeft).getRangeType(environment))) {
          if (CanAIngestB(environment, ((IsMap) typeLeft).getRangeType(environment), ((IsMap) typeRight).getRangeType(environment), silent)) {
            return true;
          } else if (!silent) {
            environment.document.createError(originalTypeA, String.format("Type check failure: ranges are incompatble for ingestion.", typeLeft.getAdamaType()));
          }
        } else {
          return RuleSetAssignment.CanTypeAStoreTypeB(environment, ((IsMap) typeLeft).getRangeType(environment), ((IsMap) typeRight).getRangeType(environment), StorageTweak.None, silent);
        }
        return false;
      } else if (!silent) {
        environment.document.createError(originalTypeA, String.format("Type check failure: domains are incompatble for ingestion %s <- %s.", ((IsMap) typeLeft).getDomainType(environment).getAdamaType(), ((IsMap) typeRight).getDomainType(environment).getAdamaType()));
      }
    }
    TyType leftStructureType = null;
    TyType rightStructureType = null;
    if (environment.rules.IsStructure(typeLeft, true)) {
      leftStructureType = typeLeft;
    } else if (environment.rules.IsTable(typeLeft, true) || typeLeft instanceof TyNativeReactiveRecordPtr) {
      leftStructureType = environment.rules.ExtractEmbeddedType(typeLeft, silent);
    } else if (environment.rules.IsMaybe(typeLeft, true)) {
      return CanAIngestB(environment, environment.rules.ExtractEmbeddedType(typeLeft, silent), typeRight, silent);
    } else if (!silent) {
      environment.document.createError(originalTypeA, String.format("Type check failure: unable to ingest into this type '%s'.", typeLeft.getAdamaType()));
    }
    if (environment.rules.IsStructure(typeRight, true)) {
      rightStructureType = typeRight;
    } else if (environment.rules.IsNativeArrayOfStructure(typeRight, true)) {
      rightStructureType = environment.rules.ExtractEmbeddedType(typeRight, silent);
    } else if (environment.rules.IsNativeListOfStructure(typeRight, true)) {
      rightStructureType = environment.rules.ExtractEmbeddedType(typeRight, silent);
    } else if (!silent) {
      environment.document.createError(originalTypeB, String.format("Type check failure: '%s' unable to produce data to ingest into '%s'.", typeRight.getAdamaType(), typeLeft.getAdamaType()));
    }
    leftStructureType = environment.rules.Resolve(leftStructureType, silent);
    rightStructureType = environment.rules.Resolve(rightStructureType, silent);
    if (rightStructureType == null || leftStructureType == null) {
      return false;
    }
    final var leftStorage = ((IsStructure) leftStructureType).storage();
    final var rightStorage = ((IsStructure) rightStructureType).storage();
    var possible = true;
    for (final Map.Entry<String, FieldDefinition> leftFieldEntry : leftStorage.fields.entrySet()) {
      if (leftFieldEntry.getValue().isRequired()) {
        if (!rightStorage.fields.containsKey(leftFieldEntry.getKey())) {
          environment.document.createError(originalTypeB, String.format("Type check failure: The field '%s' was required for ingestion and is not present", leftFieldEntry.getKey()));
          possible = false;
        }
      }
    }
    for (final Map.Entry<String, FieldDefinition> rightFieldEntry : rightStorage.fields.entrySet()) {
      final var leftField = leftStorage.fields.get(rightFieldEntry.getKey());
      final var rightField = rightFieldEntry.getValue();
      if (leftField == null) {
        if (!rightField.isLossy()) {
          environment.document.createError(originalTypeB, String.format("Type check failure: The field '%s' was lost during ingestion", rightFieldEntry.getKey()));
          possible = false;
        }
      } else {
        if (RuleSetIngestion.IngestionLeftSidePossible(environment, leftField.type)) {
          if (!CanAIngestB(environment, leftField.type, rightField.type, silent)) {
            possible = false;
          }
        } else {
          if (RuleSetMaybe.IsMaybe(environment, rightField.type, true)) {
            // type <- maybe<type>; ingestion will fold the value as an optional set
            TyType maybeEmbed = ((DetailContainsAnEmbeddedType) rightField.type).getEmbeddedType(environment);
            if (!RuleSetAssignment.CanTypeAStoreTypeB(environment, leftField.type, maybeEmbed, StorageTweak.None, silent)) {
              possible = false;
            }
          } else {
            if (!RuleSetAssignment.CanTypeAStoreTypeB(environment, leftField.type, rightField.type, StorageTweak.None, silent)) {
              possible = false;
            }
          }
        }
      }
    }
    return possible;
  }

  public static boolean IngestionLeftElementRequiresRecursion(final Environment environment, final TyType originalTypeA) {
    final var typeLeft = environment.rules.Resolve(originalTypeA, true);
    if (environment.rules.IsMaybe(typeLeft, true)) {
      return IngestionLeftElementRequiresRecursion(environment, environment.rules.ExtractEmbeddedType(typeLeft, true));
    }
    return environment.rules.IsTable(typeLeft, true) || environment.rules.IsStructure(typeLeft, true) || typeLeft instanceof TyReactiveMap || typeLeft instanceof TyNativeReactiveRecordPtr;
  }

  static boolean IngestionLeftSidePossible(final Environment environment, final TyType originalTypeA) {
    final var typeLeft = environment.rules.Resolve(originalTypeA, true);
    if (environment.rules.IsMaybe(typeLeft, true)) {
      return IngestionLeftSidePossible(environment, environment.rules.ExtractEmbeddedType(typeLeft, true));
    }
    return environment.rules.IsStructure(typeLeft, true) || environment.rules.IsTable(typeLeft, true) || typeLeft instanceof TyReactiveMap || typeLeft instanceof TyNativeReactiveRecordPtr;
  }

  public static boolean IngestionLeftSideRequiresBridgeCreate(final Environment environment, final TyType originalTypeA) {
    final var typeLeft = environment.rules.Resolve(originalTypeA, true);
    return environment.rules.IsTable(typeLeft, true) || environment.rules.IsMaybe(typeLeft, true);
  }

  public static boolean IngestionRightSideRequiresIteration(final Environment environment, final TyType originalTypeB) {
    final var typeRight = environment.rules.Resolve(originalTypeB, true);
    return environment.rules.IsNativeArrayOfStructure(typeRight, true) || environment.rules.IsNativeListOfStructure(typeRight, true);
  }
}
