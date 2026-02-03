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

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Preprocessor that applies common-page rules and template injection to pages.
 * Stamps shared attributes from common-page elements onto matching pages,
 * and optionally wraps page content in a specified template.
 */
public class Pagify {
  public static void pagify(Document document) {
    // Index all <common-page> elements and join the attributes that we can stamp out
    HashMap<String, HashMap<String, String>> apply = new HashMap<>();
    for (Element element : document.getElementsByTag("common-page")) {
      if (element.hasAttr("uri:prefix")) {
        String prefix = element.attr("uri:prefix");
        HashMap<String, String> clone = apply.get(prefix);
        if (clone == null) {
          clone = new HashMap<>();
          apply.put(prefix, clone);
        }
        for (Attribute attr : element.attributes()) {
          String key = attr.getKey();
          if (key.startsWith("static:") || key.startsWith("template:") || key.startsWith("init:") || key.startsWith("static[") || "bundle".equals(key) || "authenticate".equals(key) || "privacy".equals(key)) {
            clone.put(key, attr.getValue());
          }
        }
      }
    }

    // TODO: look into using a treemap for the prefix matching
    Function<String, HashMap<String, String>> find = (uri) -> {
      for (Map.Entry<String, HashMap<String, String>> kvp : apply.entrySet()) {
        if (uri.startsWith(kvp.getKey())) {
          return kvp.getValue();
        }
      }
      return null;
    };

    // enforce templates easy mode
    for (Element element : document.getElementsByTag("page")) {
      // STEP 1: apply a <common-page> rule
      String uri = element.attr("uri");
      HashMap<String, String> clone = find.apply(uri);
      if (clone != null) {
        for (Map.Entry<String, String> a : clone.entrySet()) {
          if (!element.hasAttr(a.getKey())) {
            element.attr(a.getKey(), a.getValue());
          }
        }
      }

      // STEP 2: inject a template
      if (element.hasAttr("template:use")) {
        String tag = element.hasAttr("template:tag") ? element.attr("template:tag") : "div";
        Element newRoot = new Element(tag);
        newRoot.attr("rx:template", element.attr("template:use"));
        for (Node node : element.childNodes()) {
          newRoot.appendChild(node.clone());
          node.remove();
        }
        element.appendChild(newRoot);
      }
    }
  }
}
