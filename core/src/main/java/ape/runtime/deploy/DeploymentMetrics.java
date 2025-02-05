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

import ape.common.metrics.CallbackMonitor;
import ape.common.metrics.MetricsFactory;

/** metrics for the deployment agent */
public class DeploymentMetrics {
  public final Runnable deploy_cache_hit;
  public final Runnable deploy_cache_miss;
  public final CallbackMonitor deploy_plan_fetch;
  public final CallbackMonitor deploy_plan_push;
  public final Runnable deploy_undo;

  public final Runnable deploy_bytecode_found;
  public final Runnable deploy_bytecode_compiled;
  public final Runnable deploy_bytecode_stored;
  public final Runnable deploy_bytecode_compile_failed;

  public DeploymentMetrics(MetricsFactory factory) {
    this.deploy_cache_hit = factory.counter("deploy_cache_hit");
    this.deploy_cache_miss = factory.counter("deploy_cache_miss");
    this.deploy_plan_fetch = factory.makeCallbackMonitor("deploy_plan_fetch");
    this.deploy_plan_push = factory.makeCallbackMonitor("deploy_plan_push");
    this.deploy_undo = factory.counter("deploy_undo");
    this.deploy_bytecode_found = factory.counter("deploy_bytecode_found");
    this.deploy_bytecode_compiled = factory.counter("deploy_bytecode_compiled");
    this.deploy_bytecode_stored = factory.counter("deploy_bytecode_stored");
    this.deploy_bytecode_compile_failed = factory.counter("deploy_bytecode_compile_failed");
  }
}
