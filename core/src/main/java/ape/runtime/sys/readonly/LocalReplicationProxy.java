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
package ape.runtime.sys.readonly;

import ape.common.Callback;
import ape.runtime.data.DataObserver;
import ape.runtime.data.Key;
import ape.runtime.sys.CoreService;

/** a basic proxy implementation of ReplicationInitiator which will adapt CoreServce to ReplicationInitiator IF it is the machine requested or no machine was requested */
public class LocalReplicationProxy implements ReplicationInitiator {
  private final String localName;
  private final ReplicationInitiator proxy;
  private CoreService service = null;

  public LocalReplicationProxy(String me, ReplicationInitiator proxy) {
    this.localName = me;
    this.proxy = proxy;
  }

  public void initialize(CoreService service) {
    this.service = service;
  }

  @Override
  public void startDocumentReplication(Key key, DataObserver observer, Callback<Runnable> cancel) {
    String machineHint = observer.machine();
    if (service != null && (machineHint == null || localName.equals(machineHint))) {
      service.watch(key, observer);
      cancel.success(() -> {
        service.unwatch(key, observer);
      });
    } else {
      proxy.startDocumentReplication(key, observer, cancel);
    }
  }
}
