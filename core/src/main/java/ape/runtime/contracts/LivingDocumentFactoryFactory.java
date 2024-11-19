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
package ape.runtime.contracts;

import ape.common.Callback;
import ape.runtime.data.Key;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.jvm.LivingDocumentFactory;

import java.util.Collection;
import java.util.HashMap;

/** This represents where scripts live such that deployments can pull versions based on the key */
public interface LivingDocumentFactoryFactory {
  /** fetch the factory for the given key */
  void fetch(Key key, Callback<LivingDocumentFactory> callback);

  /** account for the memory of the factory */
  void account(HashMap<String, PredictiveInventory.MeteringSample> sample);

  /** fetch the available spaces */
  Collection<String> spacesAvailable();
}
