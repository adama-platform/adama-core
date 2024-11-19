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
package ape.runtime.remote;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;
import ape.runtime.natives.NtPrincipal;

/** how results are delivered from services */
public interface Deliverer {

  Deliverer FAILURE = new Deliverer() {
    @Override
    public void deliver(NtPrincipal agent, Key key, int id, RemoteResult result, boolean firstParty, Callback<Integer> callback) {
      callback.failure(new ErrorCodeException(ErrorCodes.DELIVERER_FAILURE_NOT_SET));
    }
  };

  void deliver(NtPrincipal agent, Key key, int id, RemoteResult result, boolean firstParty, Callback<Integer> callback);
}
