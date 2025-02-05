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

import java.io.CharArrayWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** URL Encoding support */
public class URL {
  /** encode a simple parameter map into a string */
  public static String parameters(Map<String, String> parameters) {
    if (parameters != null) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Map.Entry<String, String> param : parameters.entrySet()) {
        if (first) {
          sb.append("?");
          first = false;
        } else {
          sb.append("&");
        }
        sb.append(param.getKey()).append("=").append(URL.encode(param.getValue(), false));
      }
      return sb.toString();
    }
    return "";
  }

  /** urlencode the string */
  public static String encode(final String s, final boolean ignoreSlashes) {
    StringBuilder out = new StringBuilder(s.length());
    for (int j = 0; j < s.length(); ) {
      int c = s.charAt(j);
      if (plain(c, ignoreSlashes)) {
        out.append((char) c);
        j++;
      } else {
        CharArrayWriter buffer = new CharArrayWriter();
        do {
          buffer.write(c);
          if (c >= 0xD800 && c <= 0xDBFF) {
            if ((j + 1) < s.length()) {
              int d = s.charAt(j + 1);
              if (d >= 0xDC00 && d <= 0xDFFF) {
                buffer.write(d);
                j++;
              }
            }
          }
          j++;
        } while (j < s.length() && !plain((c = s.charAt(j)), ignoreSlashes));
        buffer.flush();
        String str = buffer.toString();
        byte[] ba = str.getBytes(StandardCharsets.UTF_8);
        for (byte b : ba) {
          out.append("%");
          out.append(Hex.of(b).toUpperCase());
        }
      }
    }
    return out.toString();
  }

  /** should the given character (c) not be encoded */
  public static boolean plain(int c, boolean ignoreSlashes) {
    return c == '/' && ignoreSlashes || 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || '0' <= c && c <= '9' || c == '.' || c == '_' || c == '-' || c == '~';
  }
}
