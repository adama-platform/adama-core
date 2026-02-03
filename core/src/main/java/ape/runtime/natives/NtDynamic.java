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
package ape.runtime.natives;

import ape.runtime.json.JsonStreamReader;

/**
 * Opaque JSON blob for schema-free embedded data.
 * Stores arbitrary JSON as a string with lazy parsing to Java tree on
 * demand via cached(). Comparison is lexicographic on raw JSON. Used for
 * dynamic fields that store user-defined structures without compile-time
 * schema. Converts to NtJson for programmatic traversal.
 */
public class NtDynamic implements Comparable<NtDynamic>, NtToDynamic {
  public static final NtDynamic NULL = new NtDynamic("null");
  public final String json;
  private Object cached;

  public NtDynamic(String json) {
    this.json = json;
    this.cached = null;
  }

  public Object cached() {
    if (cached != null) {
      return cached;
    }
    try {
      cached = new JsonStreamReader(json).readJavaTree();
    } catch (Exception ex) {
      cached = null;
    }
    return cached;
  }

  public NtJson to_json() {
    cached = null;
    return new NtJson(cached());
  }

  @Override
  public NtDynamic to_dynamic() {
    return this;
  }

  @Override
  public int compareTo(final NtDynamic other) {
    return json.compareTo(other.json);
  }

  @Override
  public int hashCode() {
    return json.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof NtDynamic) {
      return ((NtDynamic) o).json.equals(json);
    }
    return false;
  }

  @Override
  public String toString() {
    return json;
  }

  public long memory() {
    return json.length() * 2L;
  }
}
