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

import ape.rxhtml.ProductionMode;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/** handle the mobile: prefix attributes */
public class Mobilify {
  private static final String MOBILE_PREFIX = "mobile:";
  private static int MOBILE_PREFIX_LENGTH = MOBILE_PREFIX.length();

  private static void walk(Document document, BiConsumer<Element, HashMap<String, String>> consumer) {
    for (Element element : document.getAllElements()) {
      HashMap<String, String> mobile = new HashMap<>();
      ArrayList<String> remove = new ArrayList<>();
      for (Attribute attr : element.attributes()) {
        if (attr.getKey().startsWith(MOBILE_PREFIX)) {
          if (attr.hasDeclaredValue()) {
            mobile.put(attr.getKey().substring(MOBILE_PREFIX_LENGTH), attr.getValue());
          } else {
            mobile.put(attr.getKey().substring(MOBILE_PREFIX_LENGTH), null);
          }
          remove.add(attr.getKey());
        }
      }
      if (mobile.size() > 0) {
        for (String toRemove : remove) {
          element.removeAttr(toRemove);
        }
        consumer.accept(element, mobile);
      }
    }
  }

  public static void go(Document document, ProductionMode mode) {
    if (mode == ProductionMode.MobileApp) {
      walk(document, (e, m) -> {
        for (Map.Entry<String, String> entry : m.entrySet()) {
          if (entry.getValue() == null) {
            e.attr(entry.getKey(), true);
          } else {
            e.attr(entry.getKey(), entry.getValue());
          }

        }
      });
    } else {
      walk(document, (e, m) -> {});
    }
  }
}
