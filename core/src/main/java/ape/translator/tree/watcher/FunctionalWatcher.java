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
package ape.translator.tree.watcher;

import ape.translator.env.Environment;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.TyNativeFunctional;
import ape.translator.tree.types.natives.TyNativeGlobalObject;
import ape.translator.tree.types.natives.TyNativeService;

import java.util.Set;

/** specific: collect the dependencies of all functions */
public class FunctionalWatcher implements Watcher {
  private final Environment environment;
  private final Set<String> depends;
  private final Set<String> assocs;

  public FunctionalWatcher(Environment environment, Set<String> depends, Set<String> assocs) {
    this.environment = environment;
    this.depends = depends;
    this.assocs = assocs;
  }

  @Override
  public void observe(String name, TyType type) {
    TyType resolved = environment.rules.Resolve(type, true);
    if (resolved instanceof TyNativeGlobalObject) return;
    if (resolved instanceof TyNativeFunctional) {
      depends.addAll(((TyNativeFunctional) resolved).gatherDependencies());
      assocs.addAll(((TyNativeFunctional) resolved).gatherAssocs());
      return;
    }
    if (resolved instanceof TyNativeService) return;
    depends.add(name);
  }

  @Override
  public void assoc(String name) {
    assocs.add(name);
  }
}
