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
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.IsStructure;

import java.util.Map;

public class RuleSetStructures {
  // can type A project into type B, that is B is the new type, and A may have
  // more stuff but everything from B must be found in A
  public static boolean CanStructureAProjectIntoStructureB(final Environment environment, final TyType typeA, final TyType typeB, final boolean silent) {
    var result = false;
    if (typeA != null && typeB != null && typeA instanceof IsStructure && typeB instanceof IsStructure) {
      result = true;
      final var storA = ((IsStructure) typeA).storage();
      final var storB = ((IsStructure) typeB).storage();
      for (final Map.Entry<String, FieldDefinition> elementB : storB.fields.entrySet()) {
        final var other = storA.fields.get(elementB.getKey());
        if (other != null) {
          RuleSetAssignment.CanTypeAStoreTypeB(environment, other.type, elementB.getValue().type, StorageTweak.None, silent);
        } else {
          if (!silent) {
            environment.document.createError(typeA, String.format("The type '%s' contains field '%s' which is not found within '%s'.", typeB.getAdamaType(), elementB.getKey(), typeA.getAdamaType()));
          }
          result = false;
        }
      }
    }
    return result;
  }

  public static boolean IsStructure(final Environment environment, final TyType tyTypeOriginal, final boolean silent) {
    var tyType = tyTypeOriginal;
    if (tyType != null) {
      tyType = RuleSetCommon.Resolve(environment, tyType, silent);
      if (tyType != null && (tyType instanceof IsStructure)) {
        return true;
      } else if (!silent) {
        environment.document.createError(tyTypeOriginal, String.format("Type check failure: must have a type of 'record' or 'message', but got a type of '%s'.", tyTypeOriginal.getAdamaType()));
      }
    }
    return false;
  }

  public static boolean IsRxStructure(final Environment environment, final TyType tyTypeOriginal, final boolean silent) {
    var tyType = tyTypeOriginal;
    if (tyType != null) {
      tyType = RuleSetCommon.Resolve(environment, tyType, silent);
      if (tyType != null && (tyType instanceof TyReactiveRecord)) {
        return true;
      } else if (!silent) {
        environment.document.createError(tyTypeOriginal, String.format("Type check failure: must have a type of 'record', but got a type of '%s'.", tyTypeOriginal.getAdamaType()));
      }
    }
    return false;
  }
}
