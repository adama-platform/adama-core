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
import ape.runtime.reactives.RxTime;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;

public class CronPredictTests {
  @Test
  public void merge_sanity() {
    Assert.assertEquals(4L, (long) CronPredict.merge(4L, 5L));
    Assert.assertEquals(4L, (long) CronPredict.merge(4L, null));
    Assert.assertEquals(4L, (long) CronPredict.merge(null, 4L));
  }
  @Test
  public void predict_hourly() {
    Assert.assertEquals(3012385L, (long) CronPredict.hourly(null, 1703013887615L, 15, ZoneId.of("America/Chicago"), ZoneId.of("US/Hawaii")));
  }
  @Test
  public void predict_hourly_rx() {
    Assert.assertEquals(3012385L, (long) CronPredict.hourly(null, 1703013887615L, new RxInt32(null, 15), ZoneId.of("America/Chicago"), ZoneId.of("US/Hawaii")));
  }
  @Test
  public void predict_daily() {
    Assert.assertEquals(115512385L, (long) CronPredict.daily(null, 1703013887615L, 17, 30, ZoneId.of("America/Chicago"), ZoneId.of("US/Hawaii")));
  }
  @Test
  public void predict_daily_rx() {
    Assert.assertEquals(115512385L, (long) CronPredict.daily(null, 1703013887615L, new RxTime(null, new NtTime(17, 30)), ZoneId.of("America/Chicago"), ZoneId.of("US/Hawaii")));
  }
  @Test
  public void predict_monthly() {
    Assert.assertEquals(2298912385L, (long) CronPredict.monthly(null, 1703013887615L, 15, ZoneId.of("America/Chicago"), ZoneId.of("US/Hawaii")));
  }
  @Test
  public void predict_monthly_rx() {
    Assert.assertEquals(2298912385L, (long) CronPredict.monthly(null, 1703013887615L, new RxInt32(null, 15), ZoneId.of("America/Chicago"), ZoneId.of("US/Hawaii")));
  }
}
