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
package ape.common.jvm;

import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;
import java.lang.management.ManagementFactory;

/**
 * JVM resource monitoring utilities for CPU and memory utilization.
 * Provides CPU load percentage via OperatingSystemMXBean and memory
 * usage ratio via Runtime. Used by LoadMonitor for capacity management.
 */
public class MachineHeat {
  private static MBeanServerConnection mbsc = null;
  private static OperatingSystemMXBean os = null;

  public static void install() throws Exception {
    mbsc = ManagementFactory.getPlatformMBeanServer();
    os = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
  }

  public static double cpu() {
    double cpu = -1;
    if (os != null) {
      cpu = os.getProcessCpuLoad();
    }
    return cpu;
  }

  public static double memory() {
    double free = Math.ceil(Runtime.getRuntime().freeMemory() / (1024 * 1024.0));
    double total = Math.floor(Runtime.getRuntime().totalMemory() / (1024 * 1024.0));
    return (total - free) / total;
  }
}
