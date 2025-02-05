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
package ape.common;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** common logic for dealing with unix/web paths with / */
public class Pathing {
  private static final String SLASH = Pattern.quote("/");
  private static final String WINDOWS_SLASH = Pattern.quote("\\");
  private static final String UNIX_SLASH_REPLACE = Matcher.quoteReplacement("/");

  public static String normalize(String x) {
    return x.replaceAll(WINDOWS_SLASH, UNIX_SLASH_REPLACE);
  }

  public static String maxSharedSuffix(String a, String b) {
    String[] x = a.split(SLASH);
    String[] y = b.split(SLASH);
    int m = Math.min(x.length, y.length);
    Stack<String> reversed = new Stack<>();
    for (int k = 0; k < m; k++) {
      String u = x[x.length - 1 - k];
      String v = y[y.length - 1 - k];
      if (u.equals(v)) {
        reversed.push(u);
      } else {
        break;
      }
    }
    ArrayList<String> forward = new ArrayList<>();
    while (!reversed.isEmpty()) {
      forward.add(reversed.pop());
    }
    return String.join("/", forward);
  }

  public static String maxSharedPrefix(String a, String b) {
    String[] x = a.split(SLASH);
    String[] y = b.split(SLASH);
    ArrayList<String> common = new ArrayList<>();
    int m = Math.min(x.length, y.length);
    for (int k = 0; k < m; k++) {
      if (x[k].equals(y[k])) {
        common.add(x[k]);
      } else {
        break;
      }
    }
    return String.join("/", common);
  }

  /** find the common root between two files */
  public static String removeCommonRootFromB(String a, String b) {
    return b.substring(maxSharedPrefix(a, b).length());
  }

  public static String removeLast(String x) {
    int kLastSlash = x.lastIndexOf('/');
    if (kLastSlash > 0) {
      return x.substring(0, kLastSlash);
    }
    return x;
  }
}
