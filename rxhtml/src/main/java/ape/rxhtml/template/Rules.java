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
package ape.rxhtml.template;

/** common rules for tools */
public class Rules {
  public static boolean isRoot(String tag) {
    if (tag.equalsIgnoreCase("page") || tag.equalsIgnoreCase("template")) {
      return true;
    }
    return false;
  }
  public static boolean isSpecialElement(String tag) {
    String tagNameNormal = Base.normalizeTag(tag);
    try {
      Elements.class.getMethod(tagNameNormal, Environment.class);
      return true;
    } catch (NoSuchMethodException nsme) {
      return false;
    }
  }

  public static boolean isSpecialAttribute(String name) {
    if (name.startsWith("rx:")) {
      return true;
    }
    if (name.equals("href")) {
      return true;
    }
    if (name.equals("for-env")) {
      return true;
    }
    return false;
  }
}
