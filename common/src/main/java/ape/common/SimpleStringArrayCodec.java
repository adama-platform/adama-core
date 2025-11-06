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

import java.util.regex.Pattern;

/** a simple codec for packing multiple strings into a single string and unpacking */
public class SimpleStringArrayCodec {
  public static String pack(String... strs) {
    StringBuilder sb = new StringBuilder();
    boolean notFirst = false;
    for (String str : strs) {
      if (notFirst) {
        sb.append(":");
      }
      notFirst = true;
      sb.append(escape(str));
    }
    return sb.toString();
  }

  public static String[] unpack(String str) {
    String[] output = str.split(Pattern.quote(":"), -1);
    for (int i = 0; i < output.length; i++) {
      output[i] = unescape(output[i]);
    }
    return output;
  }

  private static String escape(String input) {
    if (input == null) return null;

    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (c == '#') {
        sb.append("##");
      } else if (c == ':') {
        sb.append("#1");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String unescape(String input) {
    if (input == null) return null;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (c == '#' && i + 1 < input.length()) {
        char next = input.charAt(i + 1);
        if (next == '#') {
          sb.append('#');
          i++; // skip the second #
        } else if (next == '1') {
          sb.append(':');
          i++; // skip the '1'
        } else {
          sb.append(c); // lone #, keep as-is
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
