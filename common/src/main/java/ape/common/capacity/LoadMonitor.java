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
import ape.common.jvm.MachineHeat;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/** Monitors the heat on the machine and fires LoadEvents */
public class LoadMonitor {
  private final AtomicBoolean alive;
  private final Sample[] samples;
  private final ArrayList<LoadEvent> monitorCPU;
  private final ArrayList<LoadEvent> monitorMemory;
  private final SimpleExecutor executor;

  public LoadMonitor(SimpleExecutor executor, AtomicBoolean alive) {
    this.executor = executor;
    this.alive = alive;
    this.samples = new Sample[30];
    samples[0] = new Sample();
    for (int k = 1; k < samples.length; k++) {
      samples[k] = samples[0];
    }
    this.monitorCPU = new ArrayList<>();
    this.monitorMemory = new ArrayList<>();
    if (alive.get()) {
      this.executor.schedule(new NamedRunnable("load-signal") {
        int at = 0;

        @Override
        public void execute() throws Exception {
          samples[at] = new Sample();
          at++;
          at %= samples.length;
          double sum_cpu = 0;
          double sum_memory = 0;
          for (Sample sample : samples) {
            sum_cpu += sample.cpu;
            sum_memory += sample.memory;
          }
          sum_cpu /= samples.length;
          sum_memory /= samples.length;
          for (LoadEvent e : monitorCPU) {
            e.at(sum_cpu);
          }
          for (LoadEvent e : monitorMemory) {
            e.at(sum_memory);
          }
          if (alive.get()) {
            executor.schedule(this, 250);
          }
        }
      }, 10);
    }
  }

  public void cpu(LoadEvent e) {
    executor.execute(new NamedRunnable("add-cpu-load-event") {
      @Override
      public void execute() throws Exception {
        monitorCPU.add(e);
      }
    });
  }

  public void memory(LoadEvent e) {
    executor.execute(new NamedRunnable("add-cpu-load-event") {
      @Override
      public void execute() throws Exception {
        monitorMemory.add(e);
      }
    });
  }

  private class Sample {
    public final double cpu;
    public final double memory;

    public Sample() {
      this.cpu = MachineHeat.cpu();
      this.memory = MachineHeat.memory();
    }
  }
}
