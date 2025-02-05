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
import ape.runtime.natives.NtPrincipal;

import java.util.Objects;

/** the inputs of a service call */
public class RemoteInvocation implements Comparable<RemoteInvocation> {
  public final String service;
  public final String method;
  public final NtPrincipal who;
  public final String parameter;

  public RemoteInvocation(String service, String method, NtPrincipal who, String parameter) {
    this.service = service;
    this.method = method;
    this.who = who;
    this.parameter = parameter;
  }

  public RemoteInvocation(JsonStreamReader reader) {
    String _service = null;
    String _method = null;
    NtPrincipal _who = null;
    String _parameter = null;
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        switch (reader.fieldName()) {
          case "service":
            _service = reader.readString();
            break;
          case "method":
            _method = reader.readString();
            break;
          case "who":
            _who = reader.readNtPrincipal();
            break;
          case "parameter":
            _parameter = reader.skipValueIntoJson();
            break;
          default:
            reader.skipValue();
        }
      }
    }
    this.service = _service;
    this.method = _method;
    this.who = _who;
    this.parameter = _parameter;
  }

  @Override
  public int hashCode() {
    return Objects.hash(service, method, who, parameter);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RemoteInvocation that = (RemoteInvocation) o;
    return Objects.equals(service, that.service) && Objects.equals(method, that.method) && Objects.equals(who, that.who) && Objects.equals(parameter, that.parameter);
  }

  @Override
  public int compareTo(RemoteInvocation o) {
    int delta = service.compareTo(o.service);
    if (delta != 0) {
      return delta;
    }
    delta = method.compareTo(o.method);
    if (delta != 0) {
      return delta;
    }
    delta = who.compareTo(o.who);
    if (delta != 0) {
      return delta;
    }
    return parameter.compareTo(o.parameter);
  }

  public void write(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("service");
    writer.writeString(service);
    writer.writeObjectFieldIntro("method");
    writer.writeString(method);
    writer.writeObjectFieldIntro("who");
    writer.writeNtPrincipal(who);
    writer.writeObjectFieldIntro("parameter");
    writer.injectJson(parameter);
    writer.endObject();
  }
}
