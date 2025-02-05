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
package ape.runtime.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;

import java.util.Iterator;
import java.util.TreeSet;

/** sum a bunch of Objects that have objects/ints within */
public class JsonSum {

  private static void mergeSum(ObjectNode basis, ObjectNode arg) {
    TreeSet<String> fields = new TreeSet<>();
    Iterator<String> it = arg.fieldNames();
    while (it.hasNext()) {
      fields.add(it.next());
    }
    for (String field: fields) {
      JsonNode left = basis.get(field);
      JsonNode right = arg.get(field);
      if (left == null || left.isNull()) {
        basis.set(field, right);
      } else if (left.isObject() && right.isObject()) {
        mergeSum((ObjectNode) left, (ObjectNode) right);
      } else if (left.isIntegralNumber() && right.isIntegralNumber()) {
        basis.put(field, left.intValue() + right.intValue());
      }
    }
  }

  public static ObjectNode sum(ObjectNode... args) {
    ObjectNode basis = Json.newJsonObject();
    for (ObjectNode arg : args) {
      mergeSum(basis, arg);
    }
    return basis;
  }

  public static ObjectNode sum(Iterable<ObjectNode> args) {
    ObjectNode basis = Json.newJsonObject();
    for (ObjectNode arg : args) {
      mergeSum(basis, arg);
    }
    return basis;
  }
}
