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
package ape.runtime.data;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.common.metrics.ItemActionMonitor;
import ape.common.queue.ItemAction;
import ape.common.queue.ItemQueue;
import ape.runtime.contracts.AdamaStream;

import java.util.function.Consumer;

/** implements a AdamaStream which is delayed against an executor such that it can change */
public class DelayAdamaStream implements AdamaStream {
  private final SimpleExecutor executor;
  private final ItemQueue<AdamaStream> queue;
  private final ItemActionMonitor monitor;

  public DelayAdamaStream(SimpleExecutor executor, ItemActionMonitor monitor) {
    this.executor = executor;
    this.queue = new ItemQueue<>(executor, 16, 2500);
    this.monitor = monitor;
  }

  public void ready(AdamaStream stream) {
    executor.execute(new NamedRunnable("adama-stream-delay-ready") {
      @Override
      public void execute() throws Exception {
        queue.ready(stream);
      }
    });
  }

  public void unready() {
    executor.execute(new NamedRunnable("adama-stream-delay-unready") {
      @Override
      public void execute() throws Exception {
        queue.unready();
      }
    });
  }

  @Override
  public void update(String newViewerState, Callback<Void> callback) {
    buffer((stream) -> stream.update(newViewerState, callback), callback);
  }

  public void buffer(Consumer<AdamaStream> consumer, Callback<?> callback) {
    ItemActionMonitor.ItemActionMonitorInstance instance = monitor.start();
    executor.execute(new NamedRunnable("adama-stream-delay") {
      @Override
      public void execute() throws Exception {
        queue.add(new ItemAction<AdamaStream>(ErrorCodes.CORE_DELAY_ADAMA_STREAM_TIMEOUT, ErrorCodes.CORE_DELAY_ADAMA_STREAM_REJECTED, instance) {
          @Override
          protected void executeNow(AdamaStream stream) {
            consumer.accept(stream);
          }

          @Override
          protected void failure(int code) {
            callback.failure(new ErrorCodeException(code));
          }
        });
      }
    });
  }

  @Override
  public void send(String channel, String marker, String message, Callback<Integer> callback) {
    buffer((stream) -> stream.send(channel, marker, message, callback), callback);
  }

  @Override
  public void password(String password, Callback<Integer> callback) {
    buffer((stream) -> stream.password(password, callback), callback);
  }

  @Override
  public void canAttach(Callback<Boolean> callback) {
    buffer((stream) -> stream.canAttach(callback), callback);
  }

  @Override
  public void attach(String id, String name, String contentType, long size, String md5, String sha384, Callback<Integer> callback) {
    buffer((stream) -> stream.attach(id, name, contentType, size, md5, sha384, callback), callback);
  }

  @Override
  public void close() {
    buffer((stream) -> stream.close(), Callback.DONT_CARE_VOID);
  }
}
