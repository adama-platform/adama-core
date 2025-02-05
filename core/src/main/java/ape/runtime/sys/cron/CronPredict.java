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
package ape.runtime.sys.cron;

import ape.runtime.reactives.RxInt32;
import ape.runtime.reactives.RxInt64;
import ape.runtime.reactives.RxTime;

import java.time.ZoneId;

public class CronPredict {
  public static Long merge(Long a, Long b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    return Math.min(a, b);
  }

  public static Long predictWhen(long now, CronTask task) {
    if (!task.fire) {
      long delta = task.next - now;
      if (delta > 0) {
        return delta;
      }
    }
    return 0L;
  }

  public static Long hourly(Long prior, long currentTime, int minutes, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return merge(prior, predictWhen(currentTime, CronChecker.hourly(new RxInt64(null, currentTime), currentTime, minutes, sysTimeZone, docTimeZone)));
  }

  public static Long hourly(Long prior, long currentTime, RxInt32 minutes, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return merge(prior, predictWhen(currentTime, CronChecker.hourly(new RxInt64(null, currentTime), currentTime, minutes, sysTimeZone, docTimeZone)));
  }

  public static Long daily(Long prior, long currentTime, int hour, int minute, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return merge(prior, predictWhen(currentTime, CronChecker.daily(new RxInt64(null, currentTime), currentTime, hour, minute, sysTimeZone, docTimeZone)));
  }

  public static Long daily(Long prior, long currentTime, RxTime time, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return merge(prior, predictWhen(currentTime, CronChecker.daily(new RxInt64(null, currentTime), currentTime, time, sysTimeZone, docTimeZone)));
  }

  public static Long monthly(Long prior, long currentTime, int dayOfMonthGiven, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return merge(prior, predictWhen(currentTime, CronChecker.monthly(new RxInt64(null, currentTime), currentTime, dayOfMonthGiven, sysTimeZone, docTimeZone)));
  }

  public static Long monthly(Long prior, long currentTime, RxInt32 dayOfMonth, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return merge(prior, predictWhen(currentTime, CronChecker.monthly(new RxInt64(null, currentTime), currentTime, dayOfMonth, sysTimeZone, docTimeZone)));
  }
}
