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
package ape.runtime.sys.metering;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.sys.PredictiveInventory;
import org.junit.Assert;
import org.junit.Test;

public class MeterReadingTests {
  @Test
  public void flow() {
    MeterReading meterReading =
        new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(100, 200, 42, 1000, 13, 123, 456, 789, 13, 420));
    Assert.assertEquals(42L, meterReading.time);
    Assert.assertEquals(123, meterReading.timeframe);
    Assert.assertEquals("space", meterReading.space);
    Assert.assertEquals("hash", meterReading.hash);
    Assert.assertEquals(100, meterReading.memory);
    Assert.assertEquals(200, meterReading.cpu);
    Assert.assertEquals(42, meterReading.count);
    Assert.assertEquals(1000, meterReading.messages);
  }

  @Test
  public void packings() {
    {
      MeterReading meterReading =
          new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(100, 200, 42, 1000, 13, 123, 456, 789, 13, 420));
      Assert.assertEquals(
          "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"100\",\"200\",\"42\",\"1000\",\"13\",\"123\",\"456\",\"789\",\"13\",\"420\"]",
          meterReading.packup());
    }
    {
      MeterReading meterReading =
          new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(100, 0, 0, 0, 0, 123, 456, 789, 13, 420));
      Assert.assertEquals(
          "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"100\",\"0\",\"0\",\"0\",\"0\",\"123\",\"456\",\"789\",\"13\",\"420\"]",
          meterReading.packup());
    }
    {
      MeterReading meterReading =
          new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(0, 200, 0, 0, 0, 123, 456, 789, 13, 420));
      Assert.assertEquals(
          "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"0\",\"200\",\"0\",\"0\",\"0\",\"123\",\"456\",\"789\",\"13\",\"420\"]",
          meterReading.packup());
    }
    {
      MeterReading meterReading =
          new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(0, 0, 42, 0, 0, 123, 456, 789, 13, 420));
      Assert.assertEquals(
          "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"0\",\"0\",\"42\",\"0\",\"0\",\"123\",\"456\",\"789\",\"13\",\"420\"]",
          meterReading.packup());
    }
    {
      MeterReading meterReading =
          new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(0, 0, 0, 1000, 0, 123, 456, 789, 13, 420));
      Assert.assertEquals(
          "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"0\",\"0\",\"0\",\"1000\",\"0\",\"123\",\"456\",\"789\",\"13\",\"420\"]",
          meterReading.packup());
    }
    {
      MeterReading meterReading =
          new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(0, 0, 0, 0, 13, 123, 456, 789, 13, 420));
      Assert.assertEquals(
          "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"0\",\"0\",\"0\",\"0\",\"13\",\"123\",\"456\",\"789\",\"13\",\"420\"]",
          meterReading.packup());
    }
  }

  @Test
  public void unpack() {
    MeterReading meterReading =
        new MeterReading(42, 123, "space", "hash", new PredictiveInventory.MeteringSample(100, 200, 42, 1000, 13, 123, 456, 789, 13, 420));
    JsonStreamReader reader = new JsonStreamReader(meterReading.packup() + meterReading.packup() + meterReading.packup());
    MeterReading a = MeterReading.unpack(reader);
    MeterReading b = MeterReading.unpack(reader);
    MeterReading c = MeterReading.unpack(reader);
    MeterReading d = MeterReading.unpack(reader);
    Assert.assertEquals(42, b.time);
    Assert.assertEquals(42, c.time);
    Assert.assertEquals(42, a.time);
    Assert.assertEquals(123, a.timeframe);
    Assert.assertEquals("space", a.space);
    Assert.assertEquals("hash", a.hash);
    Assert.assertEquals(100, a.memory);
    Assert.assertEquals(200, a.cpu);
    Assert.assertEquals(42, a.count);
    Assert.assertEquals(1000, a.messages);
    Assert.assertEquals(13, a.connections);
    Assert.assertNull(d);
  }

  @Test
  public void badversion() {
    JsonStreamReader reader =
        new JsonStreamReader(
            "[\"v1\",\"42\",\"123\",\"space\",\"hash\",\"100\",\"200\",\"42\",\"1000\",\"17\",\"13\",\"420\"]"
                + "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"100\",\"200\",\"42\",\"1000\",\"17\", \"123\", \"456\", \"1313\",\"13\",\"420\"]"
                + "[\"v1\",\"42\",\"123\",\"space\",\"hash\",\"100\",\"200\",\"42\",\"1000\",\"17\",\"13\",\"420\"]"
                + "[\"v0\",\"42\",\"123\",\"space\",\"hash\",\"100\",\"200\",\"42\",\"1000\",\"17\", \"123\", \"456\", \"1313\",\"13\",\"420\"]");
    MeterReading bad1 = MeterReading.unpack(reader);
    MeterReading a = MeterReading.unpack(reader);
    MeterReading bad2 = MeterReading.unpack(reader);
    MeterReading b = MeterReading.unpack(reader);
    Assert.assertNull(bad1);
    Assert.assertNull(bad2);
    Assert.assertEquals(42, a.time);
    Assert.assertEquals(123, a.timeframe);
    Assert.assertEquals("space", a.space);
    Assert.assertEquals("hash", a.hash);
    Assert.assertEquals(100, a.memory);
    Assert.assertEquals(200, a.cpu);
    Assert.assertEquals(42, a.count);
    Assert.assertEquals(1000, a.messages);
    Assert.assertEquals(17, a.connections);
    Assert.assertEquals(13, a.cpu_ms);
    Assert.assertEquals(420, a.backup_bytes_hours);
    Assert.assertEquals(42, b.time);
    Assert.assertEquals(123, b.timeframe);
    Assert.assertEquals("space", b.space);
    Assert.assertEquals("hash", b.hash);
    Assert.assertEquals(100, b.memory);
    Assert.assertEquals(200, b.cpu);
    Assert.assertEquals(42, b.count);
    Assert.assertEquals(1000, b.messages);
    Assert.assertEquals(17, b.connections);
    Assert.assertEquals(13, a.cpu_ms);
    Assert.assertEquals(420, a.backup_bytes_hours);
  }
}
