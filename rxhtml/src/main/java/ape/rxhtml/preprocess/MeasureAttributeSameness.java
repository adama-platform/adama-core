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

import java.util.HashMap;

/** measure attributes that are the same */
public class MeasureAttributeSameness {
  private static void account(HashMap<String, HashMap<String, Integer>> nameToValueCounts, Element element) {
    if (!Rules.isSpecialElement(element.tagName())) {
      for (Attribute attribute : element.attributes()) {
        if (!Rules.isSpecialAttribute(attribute.getKey())) {
          HashMap<String, Integer> countsByValue = nameToValueCounts.get(attribute.getKey());
          if (countsByValue == null) {
            countsByValue = new HashMap<>();
            nameToValueCounts.put(attribute.getKey(), countsByValue);
          }
          Integer prior = countsByValue.get(attribute.getValue());
          if (prior == null) {
            prior = 0;
          }
          countsByValue.put(attribute.getValue(), prior + 1);
        }
      }
    }
    for (Element child : element.children()) {
      account(nameToValueCounts, child);
    }
  }

  public static HashMap<String, HashMap<String, Integer>> measure(Document document) {
    HashMap<String, HashMap<String, Integer>> nameToValueCounts = new HashMap<>();
    for (Element element : document.getElementsByTag("template")) {
      account(nameToValueCounts, element);
    }
    for (Element element : document.getElementsByTag("page")) {
      account(nameToValueCounts, element);
    }
    return nameToValueCounts;
  }
}
