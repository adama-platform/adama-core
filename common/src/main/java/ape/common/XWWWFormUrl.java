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
import java.util.Iterator;
import java.util.Map;

/** convert a JSON object into a application/x-www-form-urlencoded string */
public class XWWWFormUrl {
  private static void pump(ObjectNode node, String prefix, ArrayList<String> output) {
    Iterator<Map.Entry<String, JsonNode>> it = node.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> field = it.next();
      if (field.getValue().isObject()) {
        pump((ObjectNode) field.getValue(), prefix + field.getKey() + ".", output);
      } else if (field.getValue().isArray()) {
        // TODO: figure this out
      } else {
        if (field.getValue().isTextual()) {
          output.add(prefix + field.getKey() + "=" + URL.encode(field.getValue().textValue(), false));
        } else {
          output.add(prefix + field.getKey() + "=" + URL.encode(field.getValue().toString(), false));
        }
      }
    }
  }

  public static String encode(ObjectNode node) {
    ArrayList<String> output = new ArrayList<>();
    pump(node, "", output);
    return String.join("&", output);
  }
}
