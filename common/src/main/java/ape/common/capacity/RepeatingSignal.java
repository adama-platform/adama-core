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
package ape.common.capacity;

import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;

import java.util.concurrent.atomic.AtomicBoolean;

/** repeat the last signal given every period */
public class RepeatingSignal implements BoolConsumer {
  private final SimpleExecutor executor;
  private final AtomicBoolean alive;
  private final int freq_ms;
  private final BoolConsumer event;
  private final AtomicBoolean valueToUse;
  private long lastSent;

  public RepeatingSignal(SimpleExecutor executor, AtomicBoolean alive, int freq_ms, BoolConsumer event) {
    this.executor = executor;
    this.alive = alive;
    this.event = event;
    this.freq_ms = freq_ms;
    this.valueToUse = new AtomicBoolean(false);
    this.executor.schedule(new NamedRunnable("repeat-signal") {
      @Override
      public void execute() throws Exception {
        if (alive.get()) {
          if ((System.currentTimeMillis() - lastSent) + 1 >= freq_ms) {
            event.accept(valueToUse.get());
          }
          executor.schedule(this, freq_ms);
          lastSent = System.currentTimeMillis();
        }
      }
    }, 1);
  }

  @Override
  public void accept(final boolean value) {
    valueToUse.set(value);
    this.executor.execute(new NamedRunnable("send-now") {
      @Override
      public void execute() throws Exception {
        event.accept(valueToUse.get());
        lastSent = System.currentTimeMillis();
      }
    });
  }
}
