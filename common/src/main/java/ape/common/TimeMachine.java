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

import java.util.function.Consumer;

public class TimeMachine implements TimeSource {
  private final Consumer<String> log;
  private final TimeSource proxy;
  private final SimpleExecutor executor;
  private final Runnable forceUpdate;
  private long shift;

  public TimeMachine(TimeSource proxy, SimpleExecutor executor, Runnable forceUpdate, Consumer<String> log) {
    this.proxy = proxy;
    this.executor = executor;
    this.forceUpdate = forceUpdate;
    this.shift = 0;
    this.log = log;
  }

  @Override
  public long nowMilliseconds() {
    return proxy.nowMilliseconds() + shift;
  }

  public void add(long deltaMilliseconds, int seconds) {
    int ticks = seconds * 10;
    AddStateMachine asm = new AddStateMachine(ticks, 100, deltaMilliseconds / ticks);
    executor.execute(asm);
  }

  public void reset(Runnable done) {
    executor.execute(new ResetStateMachine(done));
  }

  private class ResetStateMachine extends NamedRunnable {
    private int ticksLeft;
    private long delta;
    private Runnable done;

    public ResetStateMachine(Runnable done) {
      super("reset-time-machine");
      this.ticksLeft = 10;
      this.delta = -shift / 10;
      this.done = done;
    }

    @Override
    public void execute() throws Exception {
      shift += delta;
      ticksLeft--;
      if (ticksLeft > 0) {
        forceUpdate.run();
        log.accept("shift[" + delta + "]");
        executor.schedule(this,  100);
      } else {
        log.accept("reset complete");
        shift = 0;
        forceUpdate.run();
        done.run();
      }
    }
  }

  private class AddStateMachine extends NamedRunnable {
    private int ticksLeft;
    private final int msPerTick;
    private final long addPerTick;

    public AddStateMachine(int ticksLeft, int msPerTick, long addPerTick) {
      super("time-machine-go");
      this.ticksLeft = ticksLeft;
      this.msPerTick = msPerTick;
      this.addPerTick = addPerTick;
    }

    @Override
    public void execute() throws Exception {
      shift += addPerTick;
      ticksLeft--;
      if (ticksLeft > 0 && addPerTick > 0) {
        log.accept("shift[" + addPerTick + "]");
        executor.schedule(this, msPerTick);
      } else {
        log.accept("done");
      }
      forceUpdate.run();
    }
  }
}
