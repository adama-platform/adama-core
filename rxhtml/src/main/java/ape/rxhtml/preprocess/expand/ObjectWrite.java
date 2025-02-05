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

import java.util.HashMap;
import java.util.Map;

public class ObjectWrite {
  public final StaticConfig config;
  public final String ordering;
  public final String code;
  public final String push;
  public final String parent;
  public final String id;
  public final HashMap<String, String> properties;

  private ObjectNode cached;

  public ObjectWrite(StaticConfig config, HashMap<String, String> properties) {
    this.cached = null;
    this.config = config;
    String _id;
    if (properties.containsKey(config.order)) {
      this.ordering = properties.remove(config.order);
    } else {
      this.ordering = "";
    }
    if (properties.containsKey(config.code)) {
      this.code = properties.get(config.code);
    } else {
      this.code = null;
    }
    if (properties.containsKey(config.push)) {
      this.push = properties.remove(config.push);
    } else {
      this.push = null;
    }
    _id = this.code;
    if (properties.containsKey(config.id)) {
      _id = properties.get(config.id);
    }
    this.id = _id;
    if (properties.containsKey(config.parent)) {
      this.parent = properties.remove(config.parent);
    } else {
      this.parent = null;
    }
    properties.remove(config.children);
    properties.remove("has_" + config.children);
    this.properties = properties;
  }

  public ObjectNode convertToNode() {
    if (cached == null) {
      cached = materialize();
    }
    return cached;
  }

  private ObjectNode materialize() {
    ObjectNode child = Json.newJsonObject();
    for (Map.Entry<String, String> property : properties.entrySet()) {
      try {
        child.put(property.getKey(), Double.parseDouble(property.getValue()));
      } catch (NumberFormatException nfeD) {
        try {
          child.put(property.getKey(), Integer.parseInt(property.getValue()));
        } catch (NumberFormatException nfeI) {
          if (property.getValue().equals("true")) {
            child.put(property.getKey(), true);
          } else if (property.getValue().equals("false")) {
            child.put(property.getKey(), false);
          } else {
            child.put(property.getKey(), property.getValue());
          }
        }
      }
    }
    return child;
  }
}
