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
package ape.common;

import java.util.concurrent.*;

/**
 * Simplified executor interface wrapping Java's ScheduledExecutorService.
 * Provides immediate execution, delayed scheduling (in milliseconds or nanoseconds),
 * and graceful shutdown. Executors use NamedRunnable for better debugging
 * and monitoring of async task execution.
 */
public interface SimpleExecutor {
  /** a default instance for doing things NOW */
  SimpleExecutor NOW = new SimpleExecutor() {
    @Override
    public void execute(NamedRunnable command) {
      command.run();
    }

    @Override
    public Runnable schedule(NamedRunnable command, long milliseconds) {
      return () -> {
      };
    }

    @Override
    public Runnable scheduleNano(NamedRunnable command, long nanoseconds) {
      return () -> {
      };
    }

    @Override
    public CountDownLatch shutdown() {
      return new CountDownLatch(0);
    }
  };

  // TODO: Finishing NamedRunnable instrumentation
  static SimpleExecutor create(String name) {
    ScheduledExecutorService realExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name));
    return new SimpleExecutor() {
      @Override
      public void execute(NamedRunnable command) {
        command.bind(name);
        realExecutor.execute(command);
      }

      @Override
      public Runnable schedule(NamedRunnable command, long milliseconds) {
        command.bind(name);
        command.delay(milliseconds);
        ScheduledFuture<?> future = realExecutor.schedule(command, milliseconds, TimeUnit.MILLISECONDS);
        return () -> future.cancel(false);
      }

      @Override
      public Runnable scheduleNano(NamedRunnable command, long nanoseconds) {
        command.bind(name);
        command.delay(nanoseconds / 1000000);
        ScheduledFuture<?> future = realExecutor.schedule(command, nanoseconds, TimeUnit.NANOSECONDS);
        return () -> future.cancel(false);
      }

      @Override
      public Future<?> submit(NamedRunnable command) {
        command.bind(name);
        return realExecutor.submit(() -> {
          command.execute();
          return null;
        });
      }

      @Override
      public CountDownLatch shutdown() {
        CountDownLatch latch = new CountDownLatch(1);
        realExecutor.execute(() -> {
          latch.countDown();
          realExecutor.shutdown();
        });
        return latch;
      }
    };
  }

  /** execute the given command in the executor */
  void execute(NamedRunnable command);

  /** schedule the given command to run after some milliseconds within the executor */
  Runnable schedule(NamedRunnable command, long milliseconds);

  Runnable scheduleNano(NamedRunnable command, long nanoseconds);

  /** submit the given command for execution, returning a Future that captures exceptions from execute() */
  default Future<?> submit(NamedRunnable command) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      command.execute();
      future.complete(null);
    } catch (Exception ex) {
      future.completeExceptionally(ex);
    }
    return future;
  }

  /** shutdown the executor */
  CountDownLatch shutdown();
}
