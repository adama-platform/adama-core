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
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.definitions.DefineStateTransition;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeStateMachineRef;
import ape.translator.tree.types.reactive.TyReactiveStateMachineRef;

public class RuleSetStateMachine {
  public static DefineStateTransition FindStateMachineStep(final Environment environment, final String name, final DocumentPosition position, final boolean silent) {
    final var defineStateTransition = environment.document.transitions.get(name);
    if (defineStateTransition != null) {
      return defineStateTransition;
    } else if (!silent) {
      environment.document.createError(position, String.format("State machine transition not found: a state machine label '%s' was not found.", name));
    }
    return null;
  }

  public static boolean IsStateMachineRef(final Environment environment, final TyType tyTypeOriginal, final boolean silent) {
    final var tyType = RuleSetCommon.Resolve(environment, tyTypeOriginal, silent);
    if (tyType != null) {
      if (tyType instanceof TyNativeStateMachineRef || tyType instanceof TyReactiveStateMachineRef) {
        return true;
      }
      RuleSetCommon.SignalTypeFailure(environment, new TyNativeStateMachineRef(TypeBehavior.ReadOnlyNativeValue, null, null), tyTypeOriginal, silent);
    }
    return false;
  }
}
