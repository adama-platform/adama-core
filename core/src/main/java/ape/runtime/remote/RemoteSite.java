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
import ape.runtime.natives.NtResult;

import java.util.Objects;
import java.util.function.Function;

/** the binding site where inputs are connected to outputs within a differential state machine */
public class RemoteSite {
  public final int id;
  private RemoteInvocation invocation;
  private RemoteResult backup;
  private RemoteResult value;
  private Object cached;

  public RemoteSite(int id, RemoteInvocation invocation) {
    this.id = id;
    this.invocation = invocation;
    this.backup = RemoteResult.NULL;
    this.value = backup;
    this.cached = null;
  }

  public RemoteSite(int id, JsonStreamReader reader) {
    this.id = id;
    patch(reader);
    this.backup = this.value;
  }

  public void patch(JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        switch (reader.fieldName()) {
          case "invoke":
            invocation = new RemoteInvocation(reader);
            break;
          case "result":
            value = new RemoteResult(reader);
            break;
          default:
            reader.skipValue();
        }
      }
    } else {
      reader.skipValue();
    }
  }

  public RemoteInvocation invocation() {
    return this.invocation;
  }

  public <T> NtResult<T> of(Function<String, T> parser) {
    if (cached == null && this.value.result != null) {
      cached = parser.apply(this.value.result);
    }
    if (cached != null) {
      return new NtResult<>((T) cached, false, 0, "OK");
    } else {
      if (this.value.failure != null) {
        return new NtResult<>(null, true, this.value.failureCode, this.value.failure);
      } else {
        return new NtResult<>(null, false, 0, "In progress");
      }
    }
  }

  public void deliver(RemoteResult result) {
    this.value = result;
  }

  public void dump(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("invoke");
    invocation.write(writer);
    writer.writeObjectFieldIntro("result");
    value.write(writer);
    writer.endObject();
  }

  public void writeValue(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("result");
    value.write(writer);
    writer.endObject();
  }

  public void writeBackup(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("result");
    backup.write(writer);
    writer.endObject();
  }

  public boolean shouldCommit() {
    return value != backup;
  }

  public void commit() {
    backup = value;
  }

  public void revert() {
    value = backup;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, invocation, backup, value, cached);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RemoteSite that = (RemoteSite) o;
    return id == that.id && Objects.equals(invocation, that.invocation) && Objects.equals(backup, that.backup) && Objects.equals(value, that.value) && Objects.equals(cached, that.cached);
  }
}
