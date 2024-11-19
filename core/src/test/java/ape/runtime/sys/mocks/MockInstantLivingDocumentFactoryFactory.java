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
package ape.runtime.sys.mocks;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.jvm.LivingDocumentFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class MockInstantLivingDocumentFactoryFactory implements LivingDocumentFactoryFactory {
  private LivingDocumentFactory factory;

  public MockInstantLivingDocumentFactoryFactory(LivingDocumentFactory factory) {
    this.factory = factory;
  }

  public synchronized void set(LivingDocumentFactory factory) {
    this.factory = factory;
  }

  @Override
  public synchronized void fetch(Key key, Callback<LivingDocumentFactory> callback) {
    if (factory != null) {
      callback.success(factory);
    } else {
      callback.failure(new ErrorCodeException(999));
    }
  }

  @Override
  public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {

  }

  @Override
  public Collection<String> spacesAvailable() {
    return Collections.singleton("space");
  }
}
