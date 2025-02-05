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

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.Caller;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtResult;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.remote.replication.Replicator;

import java.util.function.Function;

/** a service is responsible for executing a method with a message */
public interface Service {

  Replicator UNAVAILABLE = new Replicator() {
    @Override
    public String key() {
      return null;
    }

    @Override
    public void complete(Callback<Void> callback) {
      callback.failure(new ErrorCodeException(ErrorCodes.REPLICATION_NOT_AVAILABLE));
    }
  };

  Service FAILURE = new Service() {
    @Override
    public <T> NtResult<T> invoke(Caller caller, String method, RxCache cache, NtPrincipal agent, NtToDynamic request, Function<String, T> result) {
      return new NtResult<>(null, true, 500, "Service failed to resolve");
    }

    @Override
    public Replicator beginCreateReplica(String method, NtToDynamic body) {
      return UNAVAILABLE;
    }

    @Override
    public void deleteReplica(String method, String key, Callback<Void> callback) {
      UNAVAILABLE.complete(callback);
    }
  };

  Service NOT_READY = new Service() {
    @Override
    public <T> NtResult<T> invoke(Caller caller, String method, RxCache cache, NtPrincipal agent, NtToDynamic request, Function<String, T> result) {
      return new NtResult<>(null, true, ErrorCodes.DOCUMENT_NOT_READY, "Document is creating");
    }

    @Override
    public Replicator beginCreateReplica(String method, NtToDynamic body) {
      return UNAVAILABLE;
    }

    @Override
    public void deleteReplica(String method, String key, Callback<Void> callback) {
      UNAVAILABLE.complete(callback);
    }
  };

  /** invoke the given method */
  <T> NtResult<T> invoke(Caller caller, String method, RxCache cache, NtPrincipal agent, NtToDynamic request, Function<String, T> result);

  public Replicator beginCreateReplica(String method, NtToDynamic body);

  public void deleteReplica(String method, String key, Callback<Void> callback);
}
