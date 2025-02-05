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
package ape.runtime.sys.web.partial;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtDynamic;
import ape.runtime.sys.web.WebContext;
import ape.runtime.sys.web.WebItem;
import ape.runtime.sys.web.WebPut;

import java.util.TreeMap;

public class WebPutPartial implements WebPartial {
  public final String uri;
  public final TreeMap<String, String> headers;
  public final NtDynamic parameters;
  public String bodyJson;

  public WebPutPartial(String uri, TreeMap<String, String> headers, NtDynamic parameters, String bodyJson) {
    this.headers = headers;
    this.uri = uri;
    this.parameters = parameters;
    this.bodyJson = bodyJson;
  }

  public static WebPutPartial read(JsonStreamReader reader) {
    String uri = null;
    NtDynamic parameters = null;
    TreeMap<String, String> headers = null;
    String bodyJson = null;

    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        final var fieldName = reader.fieldName();
        switch (fieldName) {
          case "uri":
            uri = reader.readString();
            break;
          case "headers":
            if (reader.startObject()) {
              headers = new TreeMap<>();
              while (reader.notEndOfObject()) {
                String key = reader.fieldName();
                headers.put(key, reader.readString());
              }
            } else {
              reader.skipValue();
            }
            break;
          case "parameters":
            parameters = reader.readNtDynamic();
            break;
          case "bodyJson":
            bodyJson = reader.skipValueIntoJson();
            break;
          default:
            reader.skipValue();
        }
      }
    }
    return new WebPutPartial(uri, headers, parameters, bodyJson);
  }

  @Override
  public WebItem convert(WebContext context) {
    if (uri != null && headers != null && parameters != null && bodyJson != null) {
      return new WebPut(context, uri, headers, parameters, bodyJson);
    }
    return null;
  }
}
