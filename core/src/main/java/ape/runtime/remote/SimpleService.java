/**
 * MIT License
 * 
 * Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
