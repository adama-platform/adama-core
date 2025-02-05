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
package ape.rxhtml.preprocess.expand;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** utility classes for ExpandStaticObjects */
public class Extraction {
  /** convert the page into a map of object names to the associated properties */
  public static HashMap<String, HashMap<String, String>> propertiesByObject(Element page, Feedback feedback, AtomicInteger genId) {
    HashMap<String, HashMap<String, String>> properties = new HashMap<>();
    for (Attribute attr : page.attributes()) {
      if (attr.getKey().startsWith("static[")) {
        String key = attr.getKey();
        int bracketStart = key.indexOf('[');
        int bracketEnd = key.indexOf(']');
        if (bracketEnd > bracketStart) {
          String objectKey = key.substring(bracketStart + 1, bracketEnd);
          HashMap<String, String> staticObjectValues = properties.get(objectKey);
          if (staticObjectValues == null) {
            staticObjectValues = new HashMap<>();
            properties.put(objectKey, staticObjectValues);
            staticObjectValues.put("uri", page.attr("uri"));
          }
          int colon = key.indexOf(':');
          String path = key.substring(colon + 1);
          String val = attr.getValue();
          if (val == null) {
            val = "";
          } else {
            val = val.trim();
          }
          if ("%%%GENERATE%%%".equals(val)) {
            val = "G" + genId.incrementAndGet();
          }
          staticObjectValues.put(path, val);
        } else {
          feedback.warn(page, "missing ] on attribute:" + key);
        }
      }
    }
    return properties;
  }

  /** extract the static configs from a document */
  public static HashMap<String, StaticConfig> staticConfigs(Document document) {
    HashMap<String, StaticConfig> configs = new HashMap<>();
    for (Element configElement : document.getElementsByTag("static-config")) {
      StaticConfig config = new StaticConfig(configElement);
      if (config.name != null) {
        configs.put(config.name, config);
      }
    }
    return configs;
  }

  public static HashMap<String, ObjectNode> staticObjects(Document document) {
    HashMap<String, ObjectNode> objects = new HashMap<>();
    for (Element configElement : document.getElementsByTag("static-object")) {
      objects.put(configElement.attr("name"), Json.parseJsonObject(configElement.text()));
    }
    return objects;
  }
}
