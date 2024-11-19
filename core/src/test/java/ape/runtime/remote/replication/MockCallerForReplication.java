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
package ape.runtime.remote.replication;

import ape.runtime.contracts.Caller;
import ape.runtime.remote.Deliverer;
import ape.runtime.remote.Service;

public class MockCallerForReplication implements Caller  {
  public final MockReplicationService service;

  public MockCallerForReplication() {
    this.service = new MockReplicationService();
  }

  @Override
  public Deliverer __getDeliverer() {
    return null;
  }

  @Override
  public String __getKey() {
    return "key";
  }

  @Override
  public String __getSpace() {
    return "space";
  }

  @Override
  public Service __findService(String name) {
    if ("service".equals(name)) {
      return service;
    }
    if ("fail".equals(name)) {
      return Service.FAILURE;
    }
    return null;
  }
}
