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
import java.util.PrimitiveIterator;

/** when we want to encode multiple strings into one string easily */
public class SlashStringArrayEncoder {
  /** encode by packing with slashes as an escaped ('-') delimiter */
  public static String encode(String... fragments) {
    StringBuilder fixed = new StringBuilder();
    boolean appendSlash = false;
    for (String fragment : fragments) {
      if (appendSlash) {
        fixed.append("/");
      } else {
        appendSlash = true;
      }
      PrimitiveIterator.OfInt it = fragment.codePoints().iterator();
      while (it.hasNext()) {
        int cp = it.next();
        switch (cp) {
          case '/':
            fixed.append("-/");
            break;
          case '-':
            fixed.append("--");
            break;
          default:
            fixed.append(Character.toChars(cp));
            break;
        }
      }
    }
    return fixed.toString();
  }

  /** unpack a joined string into an array */
  public static String[] decode(String joined) {
    ArrayList<String> fragments = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    PrimitiveIterator.OfInt it = joined.codePoints().iterator();
    while (it.hasNext()) {
      int cp = it.next();
      switch (cp) {
        case '/':
          fragments.add(current.toString());
          current.setLength(0);
          break;
        case '-':
          current.append(Character.toChars(it.next()));
          break;
        default:
          current.append(Character.toChars(cp));
          break;
      }
    }
    fragments.add(current.toString());
    return fragments.toArray(new String[fragments.size()]);
  }
}
