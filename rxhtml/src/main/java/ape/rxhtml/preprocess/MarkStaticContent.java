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
package ape.rxhtml.preprocess;

import ape.rxhtml.template.Rules;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/** mark content that is static */
public class MarkStaticContent {
  public static boolean sweepAndMark(Element element) {
    boolean root = Rules.isRoot(element.tagName());
    if (!root) {
      if (Rules.isSpecialElement(element.tagName())) {
        return false;
      }
    }
    for (Attribute attribute : element.attributes()) {
      if (Rules.isSpecialAttribute(attribute.getKey())) {
        return false;
      }
    }
    boolean result = true;
    ArrayList<Element> toMark = new ArrayList<>(element.childNodeSize());
    for (Element child : element.children()) {
      if (sweepAndMark(child)) {
        toMark.add(child);
      } else {
        result = false;
      }
    }
    if (!result || root) {
      for (Element mark : toMark) {
        mark.attr("static:content", true);
      }
    }
    return result;
  }
  public static void mark(Document document) {
    for (Element element : document.getElementsByTag("template")) {
      sweepAndMark(element);
    }
    for (Element element : document.getElementsByTag("page")) {
      sweepAndMark(element);
    }
  }
}
