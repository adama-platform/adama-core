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
import ape.runtime.reactives.RxInt64;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** track the timeouts */
public class TimeoutTracker {
  public final RxInt64 time;
  public final HashMap<Integer, Timeout> timeouts;
  public final HashSet<Integer> created;

  public TimeoutTracker(RxInt64 time) {
    this.time = time;
    this.timeouts = new HashMap<>();
    this.created = new HashSet<>();
  }

  /** dump timeouts out under the living document __timeouts field */
  public void dump(JsonStreamWriter writer) {
    if (timeouts.size() > 0) {
      writer.writeObjectFieldIntro("__timeouts");
      writer.beginObject();
      for (Map.Entry<Integer, Timeout> timeout : timeouts.entrySet()) {
        writer.writeObjectFieldIntro(timeout.getKey());
        timeout.getValue().write(writer);
      }
      writer.endObject();
    }
  }

  /** restore the timeouts from a snapshot or patch */
  public void hydrate(JsonStreamReader reader) {
    if (reader.testLackOfNull()) {
      if (reader.startObject()) {
        while (reader.notEndOfObject()) {
          final var timeoutId = Integer.parseInt(reader.fieldName());
          if (reader.testLackOfNull()) {
            Timeout timeout = Timeout.readFrom(reader);
            if (timeout != null) {
              timeouts.put(timeoutId, timeout);
            }
          } else {
            timeouts.remove(timeoutId);
          }
        }
      }
    } else {
      timeouts.clear();
    }
  }

  public Timeout create(int id, double timeout) {
    Timeout to = timeouts.get(id);
    if (to != null) {
      return to;
    }
    to = new Timeout(time.get(), timeout);
    timeouts.put(id, to);
    created.add(id);
    return to;
  }

  public boolean needsInvalidationAndUpdateNext(RxInt64 next) {
    long expectedNext = next.get();
    boolean forceSetFirst = expectedNext <= time.get();
    for (Timeout to : timeouts.values()) {
      long computedNext = to.timestamp + (long) (to.timeoutSeconds * 1000L);
      if (computedNext < expectedNext || forceSetFirst) {
        next.set(computedNext);
        forceSetFirst = false;
      }
    }
    return timeouts.size() > 0;
  }

  public int size() {
    return timeouts.size();
  }

  public void revert() {
    for (Integer keyCreated : created) {
      timeouts.remove(keyCreated);
    }
    created.clear();
  }

  public void commit(JsonStreamWriter forward, JsonStreamWriter reverse) {
    if (timeouts.size() > 0) {
      forward.writeObjectFieldIntro("__timeouts");
      forward.beginObject();
      reverse.writeObjectFieldIntro("__timeouts");
      reverse.beginObject();
      for (Integer keyCreated : created) {
        Timeout timeout = timeouts.get(keyCreated);
        forward.writeObjectFieldIntro(keyCreated);
        timeout.write(forward);
        reverse.writeObjectFieldIntro(keyCreated);
        reverse.writeNull();
      }
      forward.endObject();
      reverse.endObject();
      created.clear();
    }
  }

  public void nuke(JsonStreamWriter forward, JsonStreamWriter reverse) {
    if (timeouts.size() > 0) {
      forward.writeObjectFieldIntro("__timeouts");
      forward.beginObject();
      reverse.writeObjectFieldIntro("__timeouts");
      reverse.beginObject();
      for (Map.Entry<Integer, Timeout> timeout : timeouts.entrySet()) {
        forward.writeObjectFieldIntro(timeout.getKey());
        forward.writeNull();
        reverse.writeObjectFieldIntro(timeout.getKey());
        timeout.getValue().write(reverse);
      }
      forward.endObject();
      reverse.endObject();
    }
  }
}
