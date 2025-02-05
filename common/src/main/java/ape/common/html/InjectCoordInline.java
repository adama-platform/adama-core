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
package ape.common.html;

import java.util.Iterator;

/** inject coordinates into HTML */
public class InjectCoordInline {
  public static String execute(String html, String name) {
    Iterator<Token> it = Tokenizer.of(html);
    StringBuilder sb = new StringBuilder();
    while (it.hasNext()) {
      Token token = it.next();
      if (token.type == Type.ElementOpen) {
        final String prefix;
        final String suffix;
       if (token.text.endsWith("/>")) {
         prefix = token.text.substring(0, token.text.length() - 2);
         suffix = " />";
        } else {
         prefix = token.text.substring(0, token.text.length() - 1);
         suffix = ">";
       }
       int leftCut = prefix.stripTrailing().length();
       String right = leftCut == prefix.length() ? "" : prefix.substring(leftCut);
       sb.append(prefix, 0, leftCut).append(" ln:ch=\"").append(token.coords()).append(";").append(name).append("\"").append(right).append(suffix);
      } else {
        sb.append(token.text);
      }
    }
    return sb.toString();
  }
}
