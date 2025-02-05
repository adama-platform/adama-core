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

/** part of a URI. For example, if the URI = is /XYZ/123 then [XYZ] and [123] are fragments. */
public class WebFragment {
  public final String uri;
  public final String fragment;
  public final int tail;
  public final Boolean val_boolean;
  public final Integer val_int;
  public final Long val_long;
  public final Double val_double;

  public WebFragment(String uri, String fragment, int tail) {
    this.uri = uri;
    this.fragment = fragment;
    this.tail = tail;
    this.val_boolean = parseBoolean(fragment);
    this.val_int = parseInteger(fragment);
    this.val_long = parseLong(fragment);
    this.val_double = parseDouble(fragment);
  }

  private static final Boolean parseBoolean(String x) {
    if ("true".equalsIgnoreCase(x)) {
      return true;
    }
    if ("false".equalsIgnoreCase(x)) {
      return false;
    }
    return null;
  }

  private static final Integer parseInteger(String x) {
    try {
      return Integer.parseInt(x);
    } catch (NumberFormatException nfe) {
      return null;
    }
  }

  private static final Long parseLong(String x) {
    try {
      return Long.parseLong(x);
    } catch (NumberFormatException nfe) {
      return null;
    }
  }

  private static final Double parseDouble(String x) {
    try {
      return Double.parseDouble(x);
    } catch (NumberFormatException nfe) {
      return null;
    }
  }

  public String tail() {
    return uri.substring(tail);
  }
}
