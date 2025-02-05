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
import ape.runtime.reactives.RxInt32;

import java.util.ArrayList;

/** a memoization of the produced ids. This serves to make injected table ids to be deterministic during an async flow */
public class IdHistoryLog {
  private final ArrayList<Integer> history;
  private int at;
  private boolean dirty;

  public IdHistoryLog() {
    this.history = new ArrayList<>();
    this.at = 0;
    this.dirty = false;
  }

  public void revert() {
    at = 0;
  }

  public boolean has() {
    return history.size() > 0;
  }

  public void dump(JsonStreamWriter writer) {
    writer.beginArray();
    for (int id : history) {
      writer.writeInteger(id);
    }
    writer.endArray();
  }

  public void readInline(JsonStreamReader reader) {
    at = 0;
    dirty = false;
    history.clear();
    if (reader.startArray()) {
      while (reader.notEndOfArray()) {
        history.add(reader.readInteger());
      }
    } else {
      reader.skipValue();
    }
  }

  public static IdHistoryLog read(JsonStreamReader reader) {
    IdHistoryLog log = new IdHistoryLog();
    log.readInline(reader);
    return log;
  }

  public int next(RxInt32 basis) {
    final int v;
    if (at < history.size()) {
      v = history.get(at);
      if (basis.get() < v) {
        basis.set(v);
      }
    } else {
      v = basis.bumpUpPre();
      history.add(v);
      dirty = true;
    }
    at++;
    return v;
  }

  public boolean isDirty() {
    return dirty;
  }

  public boolean resetDirtyGetPriorDirty() {
    if (dirty) {
      dirty = false;
      return true;
    }
    return false;
  }
}
