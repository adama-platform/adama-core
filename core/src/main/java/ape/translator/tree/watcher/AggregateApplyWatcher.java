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
import ape.translator.tree.types.natives.TyNativeGlobalObject;

import java.util.TreeMap;

/** specific: watch for variables within an aggregate to escape them */
public class AggregateApplyWatcher implements Watcher {

  private final Environment environment;
  private final TreeMap<String, TyType> closureTyTypes;

  public AggregateApplyWatcher(Environment environment, TreeMap<String, TyType> closureTyTypes) {
    this.environment = environment;
    this.closureTyTypes = closureTyTypes;
  }

  @Override
  public void observe(String name, TyType type) {
    TyType ty = environment.rules.Resolve(type, false);
    if (ty instanceof TyNativeGlobalObject) {
      return;
    }
    if (!closureTyTypes.containsKey(name) && ty != null) {
      closureTyTypes.put(name, ty);
    }
  }

  @Override
  public void assoc(String name) {

  }
}
