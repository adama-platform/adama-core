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
package ape.common.csv;

import java.util.ArrayList;
import java.util.PrimitiveIterator;

/**
 * RFC 4180 compliant CSV line parser.
 * Handles comma-separated fields with quoted strings containing
 * commas, newlines, and escaped double quotes (doubled quotes).
 */
public class LineReader {
  /** parse a line assuming a well parsed line */
  public static String[] parse(String ln) {
    ArrayList<String> parts = new ArrayList<>();
    StringBuilder current = new StringBuilder();

    Runnable cut = () -> {
      parts.add(current.toString());
      current.setLength(0);
    };

    PrimitiveIterator.OfInt it = ln.codePoints().iterator();
    while (it.hasNext()) {
      int cp = it.nextInt();
      switch (cp) {
        case ',':
          cut.run();
          break;
        case '"':
          while (it.hasNext()) {
            int cp2 = it.nextInt();
            if (cp2 == '"') {
              if (it.hasNext()) {
                int cp3 = it.nextInt();
                if (cp3 == '"') {
                  current.append(Character.toString(cp3));
                } else {
                  cut.run();
                  break;
                }
              } else {
                break;
              }
            } else {
              current.append(Character.toString(cp2));
            }
          }
          break;
        default:
          current.append(Character.toString(cp));
      }
    }
    cut.run();
    return parts.toArray(new String[parts.size()]);
  }
}
