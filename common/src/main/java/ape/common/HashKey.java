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

import java.util.HashMap;

/** Generate keys within a map which are unique */
public class HashKey {

  /** produce a unique key */
  public static String keyOf(String item, HashMap<String, String> map) {
    String suffix = ""; // we start optimistically, assuming no suffix to reduce compute waste
    String result = round(item, suffix, map);
    while (result == null) {
      suffix = Long.toString((long) (System.currentTimeMillis() * Math.random()), 16);
      result = round(item, suffix, map);
    }
    return result;
  }

  /** produce a unique key; single round */
  private static String round(String item, String suffix, HashMap<String, String> map) {
    String candidate = Integer.toString(Math.abs(item.hashCode()), 36) + suffix;
    for (int k = 1; k < candidate.length(); k++) {
      String test = candidate.substring(0, k);
      if (!map.containsKey(test)) {
        return test;
      }
    }
    return null;
  }
}
