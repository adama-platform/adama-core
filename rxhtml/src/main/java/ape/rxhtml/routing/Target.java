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
package ape.rxhtml.routing;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

/** a simple static target for HTTP */
public class Target implements Pathable {
  public final int status;
  public final TreeMap<String, String> headers;
  public final byte[] body;
  private final long memory;
  public final BiFunction<Target, TreeMap<String, String>, Target> mutation;

  public Target(int status, TreeMap<String, String> headers, byte[] body, BiFunction<Target, TreeMap<String, String>, Target> mutation) {
    this.status = status;
    this.headers = headers;
    this.body = body;
    long _memory = 64;
    if (headers != null) {
      for(Map.Entry<String, String> entry : headers.entrySet()) {
        _memory += 64 + entry.getKey().length() + entry.getValue().length();
      }
    }
    if (body != null) {
      _memory += 64 + body.length;
    }
    if (mutation != null) {
      _memory += 1024;
    }
    this.memory = _memory;
    this.mutation = mutation;
  }

  @Override
  public long memory() {
    return memory;
  }
}
