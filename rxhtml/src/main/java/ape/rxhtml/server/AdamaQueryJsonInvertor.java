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
package ape.rxhtml.server;

import ape.common.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/** for reasons, Adama converts the map<string, list<string>> into a JSON. This inverts that */
public class AdamaQueryJsonInvertor {

  private static String to_string(JsonNode node) {
    if (node.isTextual()) {
      return node.asText();
    }
    return node.toString();
  }

  public static TreeMap<String, List<String>> toGet(TreeMap<String, String> captured, String json) {
    TreeMap<String, List<String>> result = new TreeMap<>();
    // invert ape.web.service.AdamaWebRequest
    ObjectNode node = Json.parseJsonObject(json);
    { // just simple entries
      Iterator<Map.Entry<String, JsonNode>> simple = node.fields();
      while (simple.hasNext()) {
        Map.Entry<String, JsonNode> entry = simple.next();
        if (!entry.getValue().isArray()) {
          String value = to_string(entry.getValue());
          result.put(entry.getKey(), Collections.singletonList(value));
        }
      }
    }
    { // arrays
      Iterator<Map.Entry<String, JsonNode>> arrays = node.fields();
      while (arrays.hasNext()) {
        Map.Entry<String, JsonNode> entry = arrays.next();
        if (entry.getValue().isArray()) {
          ArrayNode array = (ArrayNode) entry.getValue();
          ArrayList<String> values = new ArrayList<>();
          for (JsonNode value : array) {
            values.add(to_string(value));
          }
          if (entry.getKey().endsWith("*")) {
            String newKey = entry.getKey().substring(0, entry.getKey().lastIndexOf("*"));
            result.put(newKey, values);
          } else {
            result.put(entry.getKey(), values);
          }
        }
      }
    }
    for (Map.Entry<String, String> entry : captured.entrySet()) {
      result.put(entry.getKey(), Collections.singletonList(entry.getValue()));
    }
    return result;
  }
}
