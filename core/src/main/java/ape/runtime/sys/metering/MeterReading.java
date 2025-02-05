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

import ape.common.ExceptionLogger;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.sys.PredictiveInventory;

/** a billing for a space */
public class MeterReading {
  private static final ExceptionLogger LOGGER = ExceptionLogger.FOR(MeterReading.class);
  public final long time;
  public final long timeframe;
  public final String space;
  public final String hash;

  public final long memory; // standing --> p95
  public final long cpu; // total --> sum
  public final long count; // standing --> p95
  public final long messages; // total --> sum
  public final long connections; // standing --> p95
  public final long bandwidth; // total -> sum
  public final long first_party_service_calls; // total -> sum
  public final long third_party_service_calls; // total -> sum
  public final long cpu_ms; // total -> sum
  public final long backup_bytes_hours; // total -> sum

  public MeterReading(long time, long timeframe, String space, String hash, PredictiveInventory.MeteringSample meteringSample) {
    this.time = time;
    this.timeframe = timeframe;
    this.space = space;
    this.hash = hash;
    this.memory = meteringSample.memory;
    this.cpu = meteringSample.cpu;
    this.count = meteringSample.count;
    this.messages = meteringSample.messages;
    this.connections = meteringSample.connections;
    this.bandwidth = meteringSample.bandwidth;
    this.first_party_service_calls = meteringSample.first_party_service_calls;
    this.third_party_service_calls = meteringSample.third_party_service_calls;
    this.cpu_ms = meteringSample.cpu_milliseconds;
    this.backup_bytes_hours = meteringSample.backup_byte_hours;
  }

  public static MeterReading unpack(JsonStreamReader reader) {
    try {
      if (!reader.end() && reader.startArray()) {
        String version = reader.readString();
        if ("v0".equals(version)) {
          long time = reader.readLong();
          long timeframe = reader.readLong();
          String space = reader.readString();
          String hash = reader.readString();
          long memory = reader.readLong();
          long cpu = reader.readLong();
          long count = reader.readLong();
          long messages = reader.readLong();
          long connections = reader.readLong();
          long bandwidth = reader.readLong();
          long first_party_service_calls = reader.readLong();
          long third_party_service_calls = reader.readLong();
          long cpu_ms = reader.readLong();
          long backup_bytes_hours = reader.readLong();
          if (!reader.notEndOfArray()) {
            return new MeterReading(time, timeframe, space, hash, new PredictiveInventory.MeteringSample(memory, cpu, count, messages, connections, bandwidth, first_party_service_calls, third_party_service_calls, cpu_ms, backup_bytes_hours));
          }
        }
        while (reader.notEndOfArray()) {
          reader.skipValue();
        }
      }
    } catch (Exception ex) {
      LOGGER.convertedToErrorCode(ex, -1);
    }
    return null;
  }

  public String packup() {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.beginArray();
    writer.writeString("v0");
    writer.writeLong(time);
    writer.writeLong(timeframe);
    writer.writeString(space);
    writer.writeString(hash);
    writer.writeLong(memory);
    writer.writeLong(cpu);
    writer.writeLong(count);
    writer.writeLong(messages);
    writer.writeLong(connections);
    writer.writeLong(bandwidth);
    writer.writeLong(first_party_service_calls);
    writer.writeLong(third_party_service_calls);
    writer.writeLong(cpu_ms);
    writer.writeLong(backup_bytes_hours);
    writer.endArray();
    return writer.toString();
  }
}
