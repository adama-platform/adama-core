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

  public TestMockUniverse(String space, LivingDocument document) {
    super(space);
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
