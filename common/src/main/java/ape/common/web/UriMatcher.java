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
package ape.common.web;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * URI pattern matcher for routing requests to handlers.
 * Matches URI paths segment-by-segment against a list of matchers,
 * with optional trailing wildcard (*) support for prefix matching.
 */
public class UriMatcher {
  public final String name;
  private final ArrayList<Function<String, Boolean>> matchers;
  private final boolean lastHasStar;

  public UriMatcher(String name, ArrayList<Function<String, Boolean>> matchers, boolean lastHasStar) {
    this.name = name;
    this.matchers = matchers;
    this.lastHasStar = lastHasStar;
  }

  /** the uri to test if it matches */
  public boolean matches(String uri) {
    String[] parts = uri.substring(1).split(Pattern.quote("/"), -1);
    int at = 0;
    for (Function<String, Boolean> match : matchers) {
      if (at < parts.length) {
        if (!match.apply(parts[at])) {
          return false;
        }
      } else {
        return false;
      }
      at++;
    }
    if (at < parts.length) {
      return lastHasStar;
    } else {
      return true;
    }
  }
}
