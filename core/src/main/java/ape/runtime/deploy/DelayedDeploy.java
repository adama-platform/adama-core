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
package ape.runtime.deploy;

import ape.common.Callback;
import ape.runtime.sys.CoreService;
import ape.runtime.sys.TriggerDeployment;

import java.util.ArrayList;
import java.util.function.BiConsumer;

/** the cyclic reference for the binding of deployments to deployment */
public class DelayedDeploy implements Deploy {
  private Deploy actual;
  private CoreService service;
  private ArrayList<BiConsumer<Deploy, CoreService>> delayed;

  public DelayedDeploy() {
    this.actual = null;
    this.delayed = new ArrayList<>();
  }

  @Override
  public synchronized void deploy(String space, Callback<Void> callback) {
    if (this.actual == null) {
      delayed.add((d, s) -> d.deploy(space, new TriggerDeployment(service, callback)));
    } else {
      this.actual.deploy(space, new TriggerDeployment(service, callback));
    }
  }

  public synchronized void set(Deploy deploy, CoreService service) {
    this.actual = deploy;
    this.service = service;
    for (BiConsumer<Deploy, CoreService> c : delayed) {
      c.accept(actual, service);
    }
    delayed = null;
  }
}
