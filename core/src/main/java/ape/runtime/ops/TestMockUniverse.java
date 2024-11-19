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
package ape.runtime.ops;

import ape.common.Callback;
import ape.runtime.data.Key;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.remote.RemoteResult;
import ape.runtime.remote.Service;
import ape.runtime.remote.ServiceRegistry;
import ape.runtime.remote.*;
import ape.runtime.sys.LivingDocument;

public class TestMockUniverse extends ServiceRegistry implements Deliverer {
  private final LivingDocument document;

  public TestMockUniverse(LivingDocument document) {
    this.document = document;
  }

  @Override
  public Service find(String name) {
    return ServiceRegistry.NOT_READY.find(name);
    /*
    return new SimpleService(name, NtPrincipal.NO_ONE, true) {
      @Override
      public void request(NtPrincipal who, String method, String request, Callback<String> callback) {

      }
    };
    */
  }

  @Override
  public void deliver(NtPrincipal agent, Key key, int id, RemoteResult result, boolean firstParty, Callback<Integer> callback) {
    document.__forceDeliverForTest(id, result);
  }
}
