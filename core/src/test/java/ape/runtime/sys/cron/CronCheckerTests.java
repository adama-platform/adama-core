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
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CronCheckerTests {
  @Test
  public void daily_chicago_to_hawaii() {
    RxInt64 last = new RxInt64(null, 0L);
    long now = 1703013887615L;
    ZoneId sys = ZoneId.of("America/Chicago");
    ZoneId doc = ZoneId.of("US/Hawaii");
    { // catch up
      CronTask task = CronChecker.daily(last, now, 17, 30, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703129400000L, task.next);
      Assert.assertEquals("2023-12-20T17:30-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-20T21:30-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // prevent
      now += 60 * 1000;
      CronTask task = CronChecker.daily(last, now, 17, 30, sys, doc);
      Assert.assertFalse(task.fire);
      Assert.assertEquals(1703129400000L, task.next);
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // advance time
      now = 1703129400001L;
      CronTask task = CronChecker.daily(last, now, 17, 30, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703215800000L, task.next);
      Assert.assertEquals("2023-12-21T17:30-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-21T21:30-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703129400001L, (long) last.get());
    }
  }

  @Test
  public void daily_chicago_to_hawaii_rx() {
    RxInt64 last = new RxInt64(null, 0L);
    long now = 1703013887615L;
    ZoneId sys = ZoneId.of("America/Chicago");
    ZoneId doc = ZoneId.of("US/Hawaii");
    RxTime time = new RxTime(null, new NtTime(17, 30));
    { // catch up
      CronTask task = CronChecker.daily(last, now, time, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703129400000L, task.next);
      Assert.assertEquals("2023-12-20T17:30-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-20T21:30-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // prevent
      now += 60 * 1000;
      CronTask task = CronChecker.daily(last, now, time, sys, doc);
      Assert.assertFalse(task.fire);
      Assert.assertEquals(1703129400000L, task.next);
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // advance time
      now = 1703129400001L;
      CronTask task = CronChecker.daily(last, now, time, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703215800000L, task.next);
      Assert.assertEquals("2023-12-21T17:30-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-21T21:30-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703129400001L, (long) last.get());
    }
  }

  @Test
  public void hourly() {
    RxInt64 last = new RxInt64(null, 0L);
    long now = 1703013887615L;
    ZoneId sys = ZoneId.of("America/Chicago");
    ZoneId doc = ZoneId.of("US/Hawaii");
    { // catch up
      CronTask task = CronChecker.hourly(last, now, 17, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703017020000L, task.next);
      Assert.assertEquals("2023-12-19T10:17-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-19T14:17-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // prevent
      now += 45 * 60 * 1000;
      CronTask task = CronChecker.hourly(last, now, 17, sys, doc);
      Assert.assertFalse(task.fire);
      Assert.assertEquals(1703017020000L, task.next);
      Assert.assertEquals("2023-12-19T10:17-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-19T14:17-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // fire_again
      now += 45 * 60 * 1000;
      CronTask task = CronChecker.hourly(last, now, 17, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703020620000L, task.next);
      Assert.assertEquals("2023-12-19T11:17-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-19T15:17-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703019287615L, (long) last.get());
    }
  }

  @Test
  public void hourly_rx() {
    RxInt64 last = new RxInt64(null, 0L);
    RxInt32 min = new RxInt32(null, 17);
    long now = 1703013887615L;
    ZoneId sys = ZoneId.of("America/Chicago");
    ZoneId doc = ZoneId.of("US/Hawaii");
    { // catch up
      CronTask task = CronChecker.hourly(last, now, min, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703017020000L, task.next);
      Assert.assertEquals("2023-12-19T10:17-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-19T14:17-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // prevent
      now += 45 * 60 * 1000;
      CronTask task = CronChecker.hourly(last, now, min, sys, doc);
      Assert.assertFalse(task.fire);
      Assert.assertEquals(1703017020000L, task.next);
      Assert.assertEquals("2023-12-19T10:17-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-19T14:17-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // fire_again
      now += 45 * 60 * 1000;
      CronTask task = CronChecker.hourly(last, now, min, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1703020620000L, task.next);
      Assert.assertEquals("2023-12-19T11:17-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2023-12-19T15:17-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703019287615L, (long) last.get());
    }
  }

  @Test
  public void monthly() {
    RxInt64 last = new RxInt64(null, 0L);
    long now = 1703013887615L;
    ZoneId sys = ZoneId.of("America/Chicago");
    ZoneId doc = ZoneId.of("US/Hawaii");
    { // catch up
      CronTask task = CronChecker.monthly(last, now, 5, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1704448800000L, task.next);
      Assert.assertEquals("2024-01-05T00:00-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2024-01-05T04:00-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // prevent
      now += 10 * 24 * 60 * 60 * 1000;
      CronTask task = CronChecker.monthly(last, now, 5, sys, doc);
      Assert.assertFalse(task.fire);
      Assert.assertEquals(1704448800000L, task.next);
      Assert.assertEquals("2024-01-05T00:00-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2024-01-05T04:00-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // fire_again
      now += 10 * 24 * 60 * 60 * 1000;
      CronTask task = CronChecker.monthly(last, now, 5, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1707127200000L, task.next);
      Assert.assertEquals("2024-02-05T00:00-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2024-02-05T04:00-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1704741887615L, (long) last.get());
    }
  }


  @Test
  public void monthly_rx() {
    RxInt64 last = new RxInt64(null, 0L);
    long now = 1703013887615L;
    ZoneId sys = ZoneId.of("America/Chicago");
    ZoneId doc = ZoneId.of("US/Hawaii");
    RxInt32 dom = new RxInt32(null, 5);
    { // catch up
      CronTask task = CronChecker.monthly(last, now, dom, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1704448800000L, task.next);
      Assert.assertEquals("2024-01-05T00:00-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2024-01-05T04:00-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // prevent
      now += 10 * 24 * 60 * 60 * 1000;
      CronTask task = CronChecker.monthly(last, now, dom, sys, doc);
      Assert.assertFalse(task.fire);
      Assert.assertEquals(1704448800000L, task.next);
      Assert.assertEquals("2024-01-05T00:00-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2024-01-05T04:00-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1703013887615L, (long) last.get());
    }
    { // fire_again
      now += 10 * 24 * 60 * 60 * 1000;
      CronTask task = CronChecker.monthly(last, now, dom, sys, doc);
      Assert.assertTrue(task.fire);
      Assert.assertEquals(1707127200000L, task.next);
      Assert.assertEquals("2024-02-05T00:00-10:00[US/Hawaii]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).withZoneSameInstant(doc).toString());
      Assert.assertEquals("2024-02-05T04:00-06:00[America/Chicago]", ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.next), sys).toString());
      Assert.assertEquals(1704741887615L, (long) last.get());
    }
  }
}
