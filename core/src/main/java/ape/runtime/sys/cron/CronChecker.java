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
import ape.runtime.natives.NtTime;
import ape.runtime.reactives.RxInt32;
import ape.runtime.reactives.RxInt64;
import ape.runtime.reactives.RxTime;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/** the various rules for cron jobs */
public class CronChecker {

  public static CronTask hourly(RxInt64 lastFired, long currentTime, int minutes, ZoneId sysTimeZone, ZoneId docTimeZone) {
    // get the time we last fired within the document's time zone
    ZonedDateTime lastFiredDocTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFired.get().longValue()), sysTimeZone).withZoneSameInstant(docTimeZone);
    ZonedDateTime currentDocTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), sysTimeZone).withZoneSameInstant(docTimeZone);
    ZonedDateTime futureFire = lastFiredDocTime.truncatedTo(ChronoUnit.HOURS).plusHours(1).plusMinutes(minutes);
    if (futureFire.isBefore(currentDocTime)) {
      ZonedDateTime nextFire = currentDocTime.truncatedTo(ChronoUnit.HOURS).plusHours(1).plusMinutes(minutes);
      lastFired.set(currentTime);
      return new CronTask(true, nextFire.withZoneSameInstant(sysTimeZone).toInstant().toEpochMilli());
    } else {
      return new CronTask(false, futureFire.withZoneSameInstant(sysTimeZone).toInstant().toEpochMilli());
    }
  }

  public static CronTask hourly(RxInt64 lastFired, long currentTime, RxInt32 minutes, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return hourly(lastFired, currentTime, minutes.get(), sysTimeZone, docTimeZone);
  }

  public static CronTask daily(RxInt64 lastFired, long currentTime, int hour, int minute, ZoneId sysTimeZone, ZoneId docTimeZone) {
    // get the time we last fired within the document's time zone
    ZonedDateTime lastFiredDocTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFired.get().longValue()), sysTimeZone).withZoneSameInstant(docTimeZone);
    ZonedDateTime currentDocTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), sysTimeZone).withZoneSameInstant(docTimeZone);
    ZonedDateTime futureFire = lastFiredDocTime.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(hour).plusMinutes(minute);
    if (futureFire.isBefore(currentDocTime)) {
      ZonedDateTime nextFire = currentDocTime.truncatedTo(ChronoUnit.DAYS).plusDays(1).plusHours(hour).plusMinutes(minute);
      lastFired.set(currentTime);
      return new CronTask(true, nextFire.withZoneSameInstant(sysTimeZone).toInstant().toEpochMilli());
    } else {
      return new CronTask(false, futureFire.withZoneSameInstant(sysTimeZone).toInstant().toEpochMilli());
    }
  }

  public static CronTask daily(RxInt64 lastFired, long currentTime, RxTime time, ZoneId sysTimeZone, ZoneId docTimeZone) {
    NtTime t = time.get();
    return daily(lastFired, currentTime, t.hour, t.minute, sysTimeZone, docTimeZone);
  }

  public static CronTask monthly(RxInt64 lastFired, long currentTime, int dayOfMonthGiven, ZoneId sysTimeZone, ZoneId docTimeZone) {
    // get the time we last fired within the document's time zone
    ZonedDateTime lastFiredDocTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFired.get().longValue()), sysTimeZone).withZoneSameInstant(docTimeZone);
    ZonedDateTime currentDocTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), sysTimeZone).withZoneSameInstant(docTimeZone);
    ZonedDateTime futureFireBase = lastFiredDocTime.plusMonths(1).truncatedTo(ChronoUnit.DAYS);
    int max = futureFireBase.getMonth().maxLength();
    if (futureFireBase.getMonth() == Month.FEBRUARY) {
      max = 28; // we don't want to deal with the 29th
    }
    int dayOfMonth = Math.min(Math.max(dayOfMonthGiven, 1), max);
    ZonedDateTime futureFire = futureFireBase.withDayOfMonth(dayOfMonth);
    if (futureFire.isBefore(currentDocTime)) {
      ZonedDateTime nextFire = currentDocTime.plusMonths(1).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(dayOfMonthGiven);
      lastFired.set(currentTime);
      return new CronTask(true, nextFire.withZoneSameInstant(sysTimeZone).toInstant().toEpochMilli());
    } else {
      return new CronTask(false, futureFire.withZoneSameInstant(sysTimeZone).toInstant().toEpochMilli());
    }
  }

  public static CronTask monthly(RxInt64 lastFired, long currentTime, RxInt32 dayOfMonth, ZoneId sysTimeZone, ZoneId docTimeZone) {
    return monthly(lastFired, currentTime, dayOfMonth.get(), sysTimeZone, docTimeZone);
  }
}
