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
package ape.translator.tree.types.checking.ruleset;

import ape.translator.env.Environment;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.TyNativeMaybe;
import ape.translator.tree.types.reactive.TyReactiveMaybe;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

public class RuleSetMaybe {
  public static boolean IsMaybe(final Environment environment, final TyType tyTypeOriginal, final boolean silent) {
    final var tyType = RuleSetCommon.Resolve(environment, tyTypeOriginal, silent);
    if (tyType != null) {
      if (tyType instanceof TyNativeMaybe || tyType instanceof TyReactiveMaybe) {
        return true;
      }
      if (!silent) {
        environment.document.createError(tyTypeOriginal, String.format("Type check failure: the type '%s' was expected to be a maybe<?>", tyTypeOriginal.getAdamaType()));
      }
    }
    return false;
  }

  public static boolean IsMaybeIntegerOrJustInteger(final Environment environment, final TyType tyTypeOriginal, final boolean silent) {
    final var tyType = RuleSetCommon.Resolve(environment, tyTypeOriginal, silent);
    if (tyType != null) {
      if (tyType instanceof TyNativeMaybe || tyType instanceof TyReactiveMaybe) {
        return RuleSetCommon.IsInteger(environment, ((DetailContainsAnEmbeddedType) tyType).getEmbeddedType(environment), silent);
      }
      return RuleSetCommon.IsInteger(environment, tyType, silent);
    }
    return false;
  }
}
