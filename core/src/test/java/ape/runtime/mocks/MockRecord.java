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
package ape.runtime.mocks;

import ape.runtime.contracts.RxParent;
import ape.runtime.index.ReactiveIndexInvalidator;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.reactives.RxInt32;
import ape.runtime.reactives.RxRecordBase;
import ape.runtime.reactives.RxString;
import ape.runtime.reactives.RxTable;
import ape.runtime.reactives.tables.TablePubSub;

import java.util.Set;

public class MockRecord extends RxRecordBase<MockRecord> {
  public final RxString data;
  public final RxInt32 index;
  public final ReactiveIndexInvalidator<MockRecord> inv;
  public int id;

  @SuppressWarnings("unchecked")
  public MockRecord(final RxParent p) {
    super(p);
    id = 0;
    data = new RxString(this, "");
    index = new RxInt32(this, 0);
    if (p instanceof RxTable) {
      final var table = (RxTable<MockRecord>) p;
      inv =
          new ReactiveIndexInvalidator<>(table.getIndex((short) 0), this) {
            @Override
            public int pullValue() {
              return index.get();
            }
          };
    } else {
      inv = null;
    }
  }

  @Override
  public Object __fieldOf(String name) {
    switch (name) {
      case "id":
        return id;
      case "data":
        return data;
      default:
        return null;
    }
  }

  @Override
  public MockRecord __link() {
    return this;
  }

  public static MockRecord make(final int id) {
    final var mr = new MockRecord(null);
    mr.id = id;
    return mr;
  }

  @Override
  public void __killFields() {

  }

  @Override
  public void __commit(
      final String name, final JsonStreamWriter writer, final JsonStreamWriter reverse) {
    if (__isDirty()) {
      writer.writeObjectFieldIntro(name);
      writer.beginObject();
      reverse.writeObjectFieldIntro(name);
      reverse.beginObject();
      data.__commit("data", writer, reverse);
      index.__commit("index", writer, reverse);
      writer.endObject();
      reverse.endObject();
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("data");
    data.__dump(writer);
    writer.writeObjectFieldIntro("index");
    index.__dump(writer);
    writer.endObject();
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        final var f = reader.fieldName();
        switch (f) {
          case "id":
            id = reader.readInteger();
            break;
          case "data":
            data.__insert(reader);
            break;
          case "index":
            index.__insert(reader);
            break;
          default:
            reader.skipValue();
        }
      }
    }
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    __insert(reader);
  }

  @Override
  public void __revert() {
    if (__isDirty()) {
      __isDying = false;
      data.__revert();
      index.__revert();
      __lowerDirtyRevert();
    }
  }

  @Override
  public void __deindex() {
    if (inv != null) {
      inv.deindex();
    }
  }

  @Override
  public String[] __getIndexColumns() {
    return new String[] {"index"};
  }

  @Override
  public int[] __getIndexValues() {
    return new int[] {index.get()};
  }

  @Override
  public String __name() {
    return null;
  }

  @Override
  public void __reindex() {
    if (inv != null) {
      inv.reindex();
    }
  }

  @Override
  public void __setId(final int __id, final boolean __useForce) {
    id = __id;
  }

  @Override
  public void __invalidateIndex(TablePubSub pubsub) {
  }

  @Override
  public void __pumpIndexEvents(TablePubSub pubsub) {
    index.setWatcher(value -> pubsub.index(0, value));
  }

  @Override
  public int __id() {
    return id;
  }

  public int settled = 0;

  @Override
  public void __writeRxReport(JsonStreamWriter __writer) {
  }

  @Override
  public void __settle(Set<Integer> viewers) {
    __lowerInvalid();
    settled++;
  }
}
