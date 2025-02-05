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
import ape.runtime.json.JsonStreamWriter;

import java.util.HashMap;
import java.util.Map;

public class MeterReducerReader {
  public static Map<String, String> convertMapToBillingMessages(String batch, String region, String machine) {
    HashMap<String, String> messages = new HashMap<>();

    HashMap<String, String> records = new HashMap<>();
    long time = 0;
    JsonStreamReader reader = new JsonStreamReader(batch);
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        switch (reader.fieldName()) {
          case "time":
            time = reader.readLong();
            break;
          case "spaces":
            if (reader.startObject()) {
              while (reader.notEndOfObject()) {
                String space = reader.fieldName();
                records.put(space, reader.skipValueIntoJson());
              }
            } else {
              reader.skipValue();
            }
            break;
          default:
            reader.skipValue();
        }
      }
    }
    for (Map.Entry<String, String> entry : records.entrySet()) {
      JsonStreamWriter message = new JsonStreamWriter();
      message.beginObject();
      message.writeObjectFieldIntro("timestamp");
      message.writeLong(time);
      message.writeObjectFieldIntro("space");
      message.writeString(entry.getKey());
      message.writeObjectFieldIntro("region");
      message.writeString(region);
      message.writeObjectFieldIntro("machine");
      message.writeString(machine);
      message.writeObjectFieldIntro("record");
      message.injectJson(entry.getValue());
      message.endObject();
      messages.put(entry.getKey(), message.toString());
    }
    return messages;
  }
}
