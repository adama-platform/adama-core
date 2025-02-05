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

public class ColorUtilTools {
  private static Boolean NO_COLOR;

  private static boolean nocolor() {
    if (NO_COLOR == null) {
      String nc = System.getenv("NO_COLOR");
      NO_COLOR = nc != null && !"0".equals(nc);
    }
    return NO_COLOR;
  }

  public static void setNoColor() {
    NO_COLOR = true;
  }

  public static void lowerNoColor() {
    NO_COLOR = false;
  }

  public static String prefix(String x, ANSI c) {
    if (nocolor()) {
      return x;
    }
    return c.ansi + x + ANSI.Reset.ansi;
  }

  public static String prefixBold(String x, ANSI c) {
    if (nocolor()) {
      return x;
    }
    return ANSI.Bold.ansi + c.ansi + x + ANSI.Reset.ansi;
  }

  public static String justifyLeft(String string, int spacing) {
    return String.format("%-" + spacing + "s", string);
  }
  public static String justifyRight(String string, int spacing) {
    return String.format("%" + spacing + "s", string);
  }
}
