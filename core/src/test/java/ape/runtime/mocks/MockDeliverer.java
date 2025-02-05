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
package ape.runtime.mocks;

import ape.common.Callback;
import ape.runtime.data.Key;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.remote.RemoteResult;
import ape.runtime.remote.RxCache;

import java.util.ArrayList;

public class MockDeliverer implements Deliverer  {
  private static class Delivery {
    public final NtPrincipal agent;
    public final Key key;
    public final int id;
    public final RemoteResult result;
    public final boolean firstParty;
    public final Callback<Integer> callback;

    public Delivery(NtPrincipal agent, Key key, int id, RemoteResult result, boolean firstParty, Callback<Integer> callback) {
      this.agent = agent;
      this.key = key;
      this.id = id;
      this.result = result;
      this.firstParty = firstParty;
      this.callback = callback;
    }
  }

  public final ArrayList<Delivery> deliveries;

  public MockDeliverer() {
    this.deliveries = new ArrayList<>();
  }

  public void deliverAllTo(RxCache cache) {
    while (deliveries.size() > 0) {
      Delivery d = deliveries.remove(0);
      cache.deliver(d.id, d.result);
    }
    deliveries.clear();
  }

  @Override
  public synchronized void deliver(NtPrincipal agent, Key key, int id, RemoteResult result, boolean firstParty, Callback<Integer> callback) {
    this.deliveries.add(new Delivery(agent, key, id, result, firstParty, callback));
  }
}
