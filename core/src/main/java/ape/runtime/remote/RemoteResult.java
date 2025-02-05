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
package ape.runtime.remote;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;

import java.util.Objects;

/** the output of a service call */
public class RemoteResult {
  public static RemoteResult NULL = new RemoteResult(null, null, null);
  public final String result;
  public final String failure;
  public final Integer failureCode;

  public RemoteResult(String result, String failure, Integer failureCode) {
    this.result = result;
    this.failure = failure;
    this.failureCode = failureCode;
  }

  public RemoteResult(JsonStreamReader reader) {
    String _result = null;
    String _failure = null;
    Integer _failureCode = null;
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        switch (reader.fieldName()) {
          case "result":
            if (reader.testLackOfNull()) {
              _result = reader.skipValueIntoJson();
            }
            break;
          case "failure":
            if (reader.testLackOfNull()) {
              _failure = reader.readString();
            }
            break;
          case "failure_code":
            if (reader.testLackOfNull()) {
              _failureCode = reader.readInteger();
            }
            break;
        }
      }
    }
    this.result = _result;
    this.failure = _failure;
    this.failureCode = _failureCode;
  }

  public void write(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("result");
    if (result != null) {
      writer.injectJson(result);
    } else {
      writer.writeNull();
    }
    writer.writeObjectFieldIntro("failure");
    if (failure != null) {
      writer.writeString(failure);
    } else {
      writer.writeNull();
    }
    writer.writeObjectFieldIntro("failure_code");
    if (failureCode != null) {
      writer.writeInteger(failureCode);
    } else {
      writer.writeNull();
    }
    writer.endObject();
  }

  @Override
  public int hashCode() {
    return Objects.hash(result, failure, failureCode);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RemoteResult that = (RemoteResult) o;
    return Objects.equals(result, that.result) && Objects.equals(failure, that.failure) && Objects.equals(failureCode, that.failureCode);
  }
}
