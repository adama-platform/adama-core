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

import ape.runtime.exceptions.AbortMessageException;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtMessageBase;
import ape.runtime.natives.algo.HashBuilder;

public class MockMessage extends NtMessageBase {
  public int x;
  public int y;

  public MockMessage() {
    x = 42;
    y = 13;
  }

  public MockMessage(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public MockMessage(JsonStreamReader reader) {
    __ingest(reader);
  }

  @Override
  public void __ingest(JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        switch (reader.fieldName()) {
          case "x":
            x = reader.readInteger();
            break;
          case "y":
            y = reader.readInteger();
            break;
          default:
            reader.skipValue();
        }
      }
    }
  }

  @Override
  public int[] __getIndexValues() {
    return new int[0];
  }

  @Override
  public String[] __getIndexColumns() {
    return new String[0];
  }

  @Override
  public void __writeOut(final JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("x");
    writer.writeInteger(x);
    writer.writeObjectFieldIntro("y");
    writer.writeInteger(y);
    writer.endObject();
  }

  @Override
  public void __hash(HashBuilder __hash) {
    __hash.hashInteger(x);
    __hash.hashInteger(y);
  }

  @Override
  public String toString() {
    JsonStreamWriter writer = new JsonStreamWriter();
    __writeOut(writer);
    return writer.toString();
  }

  @Override
  public long __memory() {
    return 128;
  }

  @Override
  public void __parsed() throws AbortMessageException {
  }
}
