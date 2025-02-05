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
package ape.rxhtml.typing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

/** simple tooling to walk the fields and find unused fields by the lacking of the used marking */
public interface UnusedReport {
  public void reportUnused(String type, String field);

  /** drive the reporting, this forest MUST come from post-typing since there is a side effect in the ObjectNode tree */
  public static void drive(ObjectNode forest, UnusedReport report) {
    Iterator<Map.Entry<String, JsonNode>> itTypes = forest.get("types").fields();
    while (itTypes.hasNext()) {
      Map.Entry<String, JsonNode> struct = itTypes.next();
      Iterator<Map.Entry<String, JsonNode>> fieldTypes = struct.getValue().get("fields").fields();
      while (fieldTypes.hasNext()) {
        Map.Entry<String, JsonNode> field = fieldTypes.next();
        if (!field.getValue().has("used")) {
          report.reportUnused(struct.getKey(), field.getKey());
        }
      }
    }
  }
}
