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
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** manages the relationship between compiling directly and using an external bytecode cache */
public class ManagedAsyncByteCodeCache implements AsyncByteCodeCache {
  private static final Logger LOG = LoggerFactory.getLogger(ManagedAsyncByteCodeCache.class);
  private final ExternalByteCodeSystem extern;
  private final SimpleExecutor offload;
  private final DeploymentMetrics metrics;

  public ManagedAsyncByteCodeCache(ExternalByteCodeSystem extern, SimpleExecutor offload, DeploymentMetrics metrics) {
    this.extern = extern;
    this.offload = offload;
    this.metrics = metrics;
  }

  @Override
  public void fetchOrCompile(String spaceName, String className, String javaSource, String reflection, Callback<CachedByteCode> callback) {
    extern.fetchByteCode(className, new Callback<CachedByteCode>() {
      @Override
      public void success(CachedByteCode value) {
        metrics.deploy_bytecode_found.run();
        callback.success(value);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        offload.execute(new NamedRunnable("compiler-offload") {
          @Override
          public void execute() throws Exception {
            try {
              CachedByteCode code = SyncCompiler.compile(spaceName, className, javaSource, reflection);
              metrics.deploy_bytecode_compiled.run();
              extern.storeByteCode(className, code, new Callback<>() {
                @Override
                public void success(Void value) {
                  metrics.deploy_bytecode_stored.run();
                  callback.success(code);
                }

                @Override
                public void failure(ErrorCodeException ex) {
                  LOG.error("failed-store-code:" + ex.code);
                  callback.success(code);
                }
              });
            } catch (ErrorCodeException problem) {
              metrics.deploy_bytecode_compile_failed.run();
              callback.failure(problem);
            }
          }
        });
      }
    });
  }
}
