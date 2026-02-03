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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

/**
 * Type-safe wrapper around Jackson ObjectNode for configuration access.
 * Provides convenient accessors with default values for primitives, strings,
 * and nested objects. Auto-populates missing values with defaults to enable
 * configuration introspection.
 */
public class ConfigObject {
  public final ObjectNode node;

  public ConfigObject(ObjectNode node) {
    this.node = node;
  }

  public int intOf(String key, int defaultValue) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull() || !v.isInt()) {
      node.put(key, defaultValue);
      return defaultValue;
    } else {
      return v.intValue();
    }
  }

  public boolean boolOf(String key, boolean defaultValue) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull() || !v.isBoolean()) {
      return defaultValue;
    } else {
      return v.booleanValue();
    }
  }

  public String strOf(String key, String defaultValue) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull() || !v.isTextual()) {
      node.put(key, defaultValue);
      return defaultValue;
    } else {
      return v.textValue();
    }
  }

  public String strOfButCrash(String key, String errorMessage) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull() || !v.isTextual()) {
      throw new NullPointerException(errorMessage);
    } else {
      return v.textValue();
    }
  }

  public ConfigObject childSearchMustExist(String message, String... keys) {
    for (String key : keys) {
      JsonNode v = node.get(key);
      if (!(v == null || v.isNull() || !v.isObject())) {
        return new ConfigObject((ObjectNode) v);
      }
    }
    throw new NullPointerException(message);
  }

  public ConfigObject child(String key) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull() || !v.isObject()) {
      return new ConfigObject(node.putObject(key));
    }
    return new ConfigObject((ObjectNode) v);
  }

  public String[] stringsOf(String key, String errorMessage) {
    JsonNode vs = node.get(key);
    if (vs.isArray()) {
      ArrayList<String> strings = new ArrayList<>();
      for (int k = 0; k < vs.size(); k++) {
        if (vs.get(k).isTextual()) {
          strings.add(vs.get(k).textValue());
        }
      }
      return strings.toArray(new String[strings.size()]);
    }
    throw new NullPointerException(errorMessage);
  }

  public String[] stringsOf(String key, String[] defaultStrings) {
    JsonNode vs = node.get(key);
    if (vs != null && vs.isArray()) {
      ArrayList<String> strings = new ArrayList<>();
      for (int k = 0; k < vs.size(); k++) {
        if (vs.get(k).isTextual()) {
          strings.add(vs.get(k).textValue());
        }
      }
      return strings.toArray(new String[strings.size()]);
    }
    return defaultStrings;
  }
}
