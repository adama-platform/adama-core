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
package ape.web.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.common.LogTimestamp;

import java.util.Map;
import java.util.TreeSet;

/** a simplified http request */
public class SimpleHttpRequest {
  public final String method;
  public final String url;
  public final Map<String, String> headers;
  public final SimpleHttpRequestBody body;

  public SimpleHttpRequest(String method, String url, Map<String, String> headers, SimpleHttpRequestBody body) {
    this.method = method;
    this.url = url;
    this.headers = headers;
    this.body = body;
  }

  public ObjectNode toJsonLongEntry(TreeSet<String> secretHeadersToIgnore) {
    ObjectNode result = Json.newJsonObject();
    result.put("@timestamp", LogTimestamp.now());
    result.put("method", method);
    result.put("url", url);
    ObjectNode headersNode = result.putObject("headers");
    for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
      if (!secretHeadersToIgnore.contains(headerEntry.getKey())) {
        headersNode.put(headerEntry.getKey(), headerEntry.getValue());
      }
    }
    body.pumpLogEntry(result.putObject("body"));
    return result;
  }
}
