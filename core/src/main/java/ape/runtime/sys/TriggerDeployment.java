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
package ape.runtime.sys;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.DeploymentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** encode the callback to trigger a deployment */
public class TriggerDeployment implements Callback<Void>, DeploymentMonitor {
  private static final Logger LOG = LoggerFactory.getLogger(TriggerDeployment.class);
  private final CoreService service;
  private final Callback<Void> other;

  public TriggerDeployment(CoreService service, Callback<Void> other) {
    this.service = service;
    this.other = other;
  }

  @Override
  public void success(Void value) {
    this.service.deploy(this);
    this.other.success(value);
  }

  @Override
  public void failure(ErrorCodeException ex) {
    witnessException(ex);
    other.failure(ex);
  }

  @Override
  public void bumpDocument(boolean changed) {
    if (changed) {
      service.metrics.trigger_deployment.run();
    }
  }

  @Override
  public void witnessException(ErrorCodeException ex) {
    LOG.error("witness-exception-deployment: {}", ex.code);
  }

  @Override
  public void finished(int ms) {
  }
}
