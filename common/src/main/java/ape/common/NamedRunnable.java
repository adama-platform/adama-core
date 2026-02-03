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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable wrapper with human-readable names for debugging and monitoring.
 * Tracks creation time, queue delay, and execution time for performance analysis.
 * Automatically logs exceptions while filtering noise from expected errors
 * like RejectedExecutionException. Fatal errors trigger immediate shutdown.
 */
public abstract class NamedRunnable implements Runnable {
  private static final Logger PERF_LOG = LoggerFactory.getLogger("perf");
  private static final Logger RUNNABLE_LOGGER = LoggerFactory.getLogger("nrex");
  public final String __runnableName;
  private String runningIn = "unknown";
  private long created;
  private long delay;

  public NamedRunnable(String first, String... tail) {
    if (tail == null || tail.length == 0) {
      this.__runnableName = first;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(first);
      for (String fragment : tail) {
        sb.append("/");
        sb.append(fragment);
      }
      this.__runnableName = sb.toString();
    }
    this.created = System.currentTimeMillis();
    this.delay = 0;
  }

  public void bind(String executorName) {
    this.runningIn = executorName;
  }

  public void delay(long ms) {
    this.delay = ms;
  }

  @Override
  public void run() {
    long ran = System.currentTimeMillis();
    try {
      execute();
      long now = System.currentTimeMillis();
      long queueTime = now - created - delay;
      long runTime = now - ran;
      this.created = now; // for periodic tasks
      /*
      boolean skip = queueTime <= 1 && runTime <= 1;
      if (!skip) {
        ObjectNode entry = Json.newJsonObject();
        entry.put("@timestamp", LogTimestamp.now());
        entry.put("type", "executor");
        entry.put("name", __runnableName);
        entry.put("executor", runningIn);
        entry.put("queueTime", queueTime);
        entry.put("queueStatus", (queueTime > 500 ? "delayed" : "quick"));
        entry.put("runTime", runTime);
        entry.put("runStatus", (runTime > 25 ? "slow" : "fast"));
        PERF_LOG.error(entry.toString());
      }
      */
    } catch (Exception ex) {
      boolean noise = noisy(ex);
      if (!noise) {
        RUNNABLE_LOGGER.error(__runnableName, ex);
      }
    } catch (Error er) {
      RUNNABLE_LOGGER.error(__runnableName, er);
      er.printStackTrace(System.err);
      System.exit(1);
    }
  }

  public abstract void execute() throws Exception;

  public static boolean noisy(Throwable ex) {
    return ex instanceof java.util.concurrent.RejectedExecutionException;
  }

  @Override
  public String toString() {
    return __runnableName;
  }
}
