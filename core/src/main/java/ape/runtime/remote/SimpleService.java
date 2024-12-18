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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.AtomicCallbackWrapper;
import ape.runtime.contracts.Caller;
import ape.runtime.contracts.InstantCallbackWrapper;
import ape.runtime.data.Key;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtResult;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.remote.replication.Replicator;

import java.util.function.Function;

/** simplifies the connecting of the dots */
public abstract class SimpleService implements Service {
  private final String name;
  private final NtPrincipal agent;
  private final boolean firstParty;

  public SimpleService(String name, NtPrincipal agent, boolean firstParty) {
    this.name = name;
    this.agent = agent;
    this.firstParty = firstParty;
  }

  @Override
  public <T> NtResult<T> invoke(Caller caller, String method, RxCache cache, NtPrincipal who, NtToDynamic request, Function<String, T> parser) {
    return cache.answer(name, method, who, request, parser, (id, json) -> {
      InstantCallbackWrapper instant = new InstantCallbackWrapper();
      AtomicCallbackWrapper<String> wrapper = new AtomicCallbackWrapper<>(instant);
      request(who, method, json, wrapper);
      Callback<String> async = new Callback<String>() {
        @Override
        public void success(String value) {
          deliver(new RemoteResult(value, null, null));
        }

        @Override
        public void failure(ErrorCodeException ex) {
          deliver(new RemoteResult(null, "" + ex.getMessage(), ex.code));
        }

        public void deliver(RemoteResult result) {
          Key key = new Key(caller.__getSpace(), caller.__getKey());
          caller.__getDeliverer().deliver(agent, key, id, result, firstParty, Callback.DONT_CARE_INTEGER);
        }
      };
      wrapper.set(async);
      return instant.convert();
    });
  }

  public abstract void request(NtPrincipal who, String method, String request, Callback<String> callback);

  @Override
  public Replicator beginCreateReplica(String method, NtToDynamic body) {
    return null;
  }

  @Override
  public void deleteReplica(String method, String key, Callback<Void> callback) {
    Service.UNAVAILABLE.complete(callback);
  }
}
