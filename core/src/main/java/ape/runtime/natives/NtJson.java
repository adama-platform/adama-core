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
package ape.runtime.natives;

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.lists.ArrayNtList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Typed accessor for navigating parsed JSON trees.
 * Wraps a Java object tree (Map/List/primitives) with safe traversal
 * via deref() and type extraction via to_s(), to_i(), to_d(), to_l(),
 * to_b(). Returns NtMaybe for type conversions to handle missing or
 * wrong-typed values. Converts back to NtDynamic for storage.
 */
public class NtJson {
  public final Object tree;

  public NtJson() {
    tree = null;
  }

  public NtJson(Object tree) {
    this.tree = tree;
  }

  public NtDynamic to_dynamic() {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.writeTree(tree);
    return new NtDynamic(writer.toString());
  }

  public NtJson deref(String field) {
    if (tree instanceof Map<?,?>) {
      return new NtJson(((Map<?, ?>) tree).get(field));
    } else if (tree instanceof List<?>) {
      List<?> l = (List<?>) tree;
      try {
        int idx = Integer.parseInt(field);
        if (0 <= idx && idx < l.size()) {
          return new NtJson(l.get(idx));
        }
      } catch (NumberFormatException nfe) {
      }
    }
    return NULL_VALUE;
  }

  public NtJson deref(int idx) {
    if (tree instanceof Map<?,?>) {
      return new NtJson(((Map<?, ?>) tree).get("" + idx));
    } else if (tree instanceof List<?>) {
      List<?> l = (List<?>) tree;
      if (0 <= idx && idx < l.size()) {
        return new NtJson(l.get(idx));
      }
    }
    return NULL_VALUE;
  }

  public NtMaybe<String> to_s() {
    if (tree instanceof String) {
      return new NtMaybe<>((String) tree);
    }
    if (tree != null && (!(tree instanceof Map<?,?> || tree instanceof List<?>))) {
      return new NtMaybe<>(tree.toString());
    }
    return new NtMaybe<>();
  }

  public NtMaybe<Integer> to_i() {
    if (tree instanceof Integer) {
      return new NtMaybe<>((int) tree);
    }
    if (tree instanceof Long) {
      return new NtMaybe<>((int) ((long) tree));
    }
    if (tree instanceof Double) {
      return new NtMaybe<>((int) ((double) tree));
    }
    return new NtMaybe<>();
  }

  public NtMaybe<Double> to_d() {
    if (tree instanceof Double) {
      return new NtMaybe<>((double) tree);
    }
    if (tree instanceof Integer) {
      return new NtMaybe<>((double) ((int) tree));
    }
    if (tree instanceof Long) {
      return new NtMaybe<>((double) ((long) tree));
    }
    return new NtMaybe<>();
  }

  public NtMaybe<Long> to_l() {
    if (tree instanceof Long) {
      return new NtMaybe<>((long) tree);
    }
    if (tree instanceof Integer) {
      return new NtMaybe<>((long)((int) tree));
    }
    if (tree instanceof Double) {
      return new NtMaybe<>((long) ((double) tree));
    }
    return new NtMaybe<>();
  }


  public NtMaybe<Boolean> to_b() {
    if (tree instanceof Boolean) {
      return new NtMaybe<>((boolean) tree);
    }
    if (tree instanceof Integer) {
      return new NtMaybe<>(((int) tree) != 0);
    }
    if (tree instanceof Long) {
      return new NtMaybe<>(((long) tree) != 0);
    }
    return new NtMaybe<>();
  }

  /** Return the number of entries in this node: array length or object key count. */
  public NtMaybe<Integer> size() {
    if (tree instanceof List) {
      return new NtMaybe<>(((List<?>) tree).size());
    }
    if (tree instanceof Map) {
      return new NtMaybe<>(((Map<?, ?>) tree).size());
    }
    return new NtMaybe<>();
  }

  /** Return the keys of this JSON object. Returns empty list if not an object. */
  public NtList<String> keys() {
    if (tree instanceof Map) {
      ArrayList<String> result = new ArrayList<>();
      for (Object key : ((Map<?, ?>) tree).keySet()) {
        result.add(key.toString());
      }
      return new ArrayNtList<>(result);
    }
    return new ArrayNtList<>(new ArrayList<>());
  }

  /** Return true if this node is a JSON array. */
  public boolean is_array() {
    return tree instanceof List;
  }

  /** Return true if this node is a JSON object. */
  public boolean is_object() {
    return tree instanceof Map;
  }

  /** Return true if this node is a JSON string. */
  public boolean is_string() {
    return tree instanceof String;
  }

  /** Return true if this node is a JSON number. */
  public boolean is_number() {
    return tree instanceof Integer || tree instanceof Long || tree instanceof Double;
  }

  /** Return true if this node is a JSON boolean. */
  public boolean is_bool() {
    return tree instanceof Boolean;
  }

  /** Return true if this node is JSON null. */
  public boolean is_null() {
    return tree == null;
  }

  /** Convert a JSON array to an iterable list of NtJson nodes. Returns empty list if not an array. */
  public NtList<NtJson> to_list() {
    if (tree instanceof List) {
      ArrayList<NtJson> result = new ArrayList<>();
      for (Object item : (List<?>) tree) {
        result.add(new NtJson(item));
      }
      return new ArrayNtList<>(result);
    }
    return new ArrayNtList<>(new ArrayList<>());
  }

  private static final NtJson NULL_VALUE = new NtJson(null);
}
