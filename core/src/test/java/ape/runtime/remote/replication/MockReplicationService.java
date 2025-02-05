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
package ape.runtime.remote.replication;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.Caller;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtResult;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.remote.RxCache;
import ape.runtime.remote.Service;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class MockReplicationService implements Service {
  private ArrayList<String> log;

  public String at(int k) {
    Assert.assertTrue(k < log.size());
    return log.get(k);
  }

  public MockReplicationService() {
    this.log = new ArrayList<>();
  }

  @Override
  public <T> NtResult<T> invoke(Caller caller, String method, RxCache cache, NtPrincipal agent, NtToDynamic request, Function<String, T> result) {
    return Service.FAILURE.invoke(caller, method, cache, agent, request, result);
  }

  public static NtToDynamic SIMPLE_KEY_OBJECT(String key) {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("key");
    writer.writeString(key);
    writer.endObject();
    return new NtDynamic(writer.toString());
  }

  private synchronized void writeLog(String ln) {
    log.add(ln);
  }


  private boolean failNextComplete = false;
  private boolean failNextDelete = false;

  public void raiseFailNextComplete() {
    failNextComplete = true;
  }

  public void raiseFailNextDelete() {
    failNextDelete = true;
  }

  @Override
  public Replicator beginCreateReplica(String method, NtToDynamic body) {
    if ("nope".equals(method)) {
      return null;
    }
    final String key;
    Object map = body.to_dynamic().cached();
    if (map instanceof Map<?,?>) {
      key = ((Map<String, Object>) map).get("key").toString();
    } else {
      key = null;
    }

    writeLog("BEGIN[" + method + "]:" + key);

    return new Replicator() {
      @Override
      public String key() {
        writeLog("ASK[" + method + "]:" + key);
        return key;
      }

      @Override
      public void complete(Callback<Void> callback) {
        if ("failure".equals(method) || failNextComplete) {
          failNextComplete = false;
          writeLog("FAILED[" + method + "]:" + key);
          callback.failure(new ErrorCodeException(-42));
        } else {
          writeLog("SUCCESS[" + method + "]:" + key);
          callback.success(null);
        }
      }
    };
  }

  @Override
  public void deleteReplica(String method, String key, Callback<Void> callback) {
    if ("failure".equals(method) || failNextDelete) {
      failNextDelete = false;
      writeLog("NODELETE[" + method + "]:" + key);
      callback.failure(new ErrorCodeException(-42));
    } else {
      writeLog("DELETED[" + method + "]:" + key);
      callback.success(null);
    }
  }
}
