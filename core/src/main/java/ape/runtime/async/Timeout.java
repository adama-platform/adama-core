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
package ape.runtime.async;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;

/** represents a serialized timeout that has been persisted in the document to track timeouts of actions */
public class Timeout {
  public final long timestamp;
  public final double timeoutSeconds;

  public Timeout(long timestamp, double timeoutSeconds) {
    this.timestamp = timestamp;
    this.timeoutSeconds = timeoutSeconds;
  }

  /** read a timeout object */
  public static Timeout readFrom(JsonStreamReader reader) {
    if (reader.startObject()) {
      long timestamp = 0L;
      double timeoutSeconds = 0.0;
      while (reader.notEndOfObject()) {
        final var f = reader.fieldName();
        switch (f) {
          case "timestamp":
            timestamp = reader.readLong();
            break;
          case "timeout":
            timeoutSeconds = reader.readDouble();
            break;
          default:
            reader.skipValue();
        }
      }
      return new Timeout(timestamp, timeoutSeconds);
    } else {
      reader.skipValue();
    }
    return null;
  }

  /** write out a timeout object */
  public void write(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("timestamp");
    writer.writeLong(timestamp);
    writer.writeObjectFieldIntro("timeout");
    writer.writeDouble(timeoutSeconds);
    writer.endObject();
  }
}
