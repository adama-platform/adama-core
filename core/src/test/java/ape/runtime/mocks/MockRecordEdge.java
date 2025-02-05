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
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.reactives.RxRecordBase;
import ape.runtime.reactives.RxString;
import ape.runtime.reactives.tables.TablePubSub;

import java.util.Set;

public class MockRecordEdge extends RxRecordBase<MockRecordEdge> {
  public final RxString data;
  public int id;

  public int from;
  public int to;

  @SuppressWarnings("unchecked")
  public MockRecordEdge(final RxParent p) {
    super(p);
    id = 0;
    data = new RxString(this, "");
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
  public MockRecordEdge __link() {
    return this;
  }

  public static MockRecordEdge make(final int id) {
    final var mr = new MockRecordEdge(null);
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
      __lowerDirtyRevert();
    }
  }

  @Override
  public void __deindex() {
  }

  @Override
  public String[] __getIndexColumns() {
    return new String[] {"index"};
  }

  @Override
  public int[] __getIndexValues() {
    return new int[] {};
  }

  @Override
  public String __name() {
    return null;
  }

  @Override
  public void __reindex() {
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
