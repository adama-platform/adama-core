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
package ape.runtime.sys.web;

import ape.runtime.data.Key;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.CoreRequestContext;

/** the context (who, from:origin+ip) for a web request */
public class WebContext {
  public final NtPrincipal who;
  public final String origin;
  public final String ip;

  public WebContext(NtPrincipal who, String origin, String ip) {
    this.who = who;
    this.origin = origin;
    this.ip = ip;
  }

  public static WebContext readFromObject(JsonStreamReader reader) {
    if (reader.startObject()) {
      NtPrincipal _who = null;
      String _origin = null;
      String _ip = null;
      while (reader.notEndOfObject()) {
        switch (reader.fieldName()) {
          case "who":
            _who = reader.readNtPrincipal();
            break;
          case "origin":
            _origin = reader.readString();
            break;
          case "ip":
            _ip = reader.readString();
            break;
          default:
            reader.skipValue();
        }
      }
      return new WebContext(_who, _origin, _ip);
    } else {
      reader.skipValue();
    }
    return null;
  }

  public CoreRequestContext toCoreRequestContext(Key key) {
    return new CoreRequestContext(who, origin, ip, key.key);
  }

  public void writeAsObject(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("who");
    writer.writeNtPrincipal(who);
    writer.writeObjectFieldIntro("origin");
    writer.writeString(origin);
    writer.writeObjectFieldIntro("ip");
    writer.writeString(ip);
    writer.endObject();
  }
}
