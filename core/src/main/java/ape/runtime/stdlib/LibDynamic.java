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
package ape.runtime.stdlib;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDate;
import ape.runtime.natives.NtDateTime;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.lists.ArrayNtList;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;
import ape.translator.reflect.HiddenTypes2;
import ape.translator.reflect.UseName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibDynamic {
  @Extension
  public static NtDynamic dyn(boolean x) {
    return new NtDynamic("" + x);
  }

  @Extension
  public static NtDynamic dyn(int x) {
    return new NtDynamic("" + x);
  }

  @Extension
  public static NtDynamic dyn(long x) {
    return new NtDynamic("" + x);
  }

  @Extension
  public static NtDynamic dyn(double x) {
    return new NtDynamic("" + x);
  }

  @Extension
  public static NtDynamic dyn(String x) {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.writeString(x);
    return new NtDynamic(writer.toString());
  }

  @Extension
  public static @HiddenType(clazz = String.class) NtMaybe<String> str(@HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> dyn) {
    if (dyn.has()) {
      return str(dyn.get());
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = String.class) NtMaybe<String> str(NtDynamic dyn) {
    Object o = dyn.cached();
    if (o instanceof String) {
      return new NtMaybe<>((String) o);
    } else if (o instanceof Long || o instanceof Integer || o instanceof Double || o instanceof Boolean) {
      return new NtMaybe<>(o + "");
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> d(@HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> dyn) {
    if (dyn.has()) {
      return d(dyn.get());
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> d(NtDynamic dyn) {
    Object o = dyn.cached();
    if (o instanceof Double) {
      return new NtMaybe<>((Double) o);
    } else if (o instanceof Long) {
      return new NtMaybe<>((double) ((Long) o));
    } else if (o instanceof Integer) {
      return new NtMaybe<>((double) ((Integer) o));
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> l(@HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> dyn) {
    if (dyn.has()) {
      return l(dyn.get());
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> l(NtDynamic dyn) {
    Object o = dyn.cached();
    if (o instanceof Long) {
      return new NtMaybe<>((Long) o);
    } else if (o instanceof Integer) {
      return new NtMaybe<>((long) ((Integer) o));
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> i(@HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> dyn) {
    if (dyn.has()) {
      return i(dyn.get());
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> i(NtDynamic dyn) {
    Object o = dyn.cached();
    if (o instanceof Integer) {
      return new NtMaybe<>((Integer) o);
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Boolean.class) NtMaybe<Boolean> b(@HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> dyn) {
    if (dyn.has()) {
      return b(dyn.get());
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Boolean.class) NtMaybe<Boolean> b(NtDynamic dyn) {
    Object o = dyn.cached();
    if (o instanceof Boolean) {
      return new NtMaybe<>((Boolean) o);
    } else if ("true".equals(o)) {
      return new NtMaybe<>(true);
    } else if ("false".equals(o)) {
      return new NtMaybe<>(false);
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = Boolean.class) NtMaybe<Boolean> b(NtDynamic dyn, String field) {
    if (dyn.cached() instanceof Map) {
      Object o = ((Map<?, ?>) dyn.cached()).get(field);
      if (o instanceof Boolean) {
        return new NtMaybe<>((Boolean) o);
      } else if ("true".equals(o)) {
        return new NtMaybe<>(true);
      } else if ("false".equals(o)) {
        return new NtMaybe<>(false);
      }
    }
    return new NtMaybe<>();
  }

  @Extension
  public static boolean b(NtDynamic dyn, String field, boolean defaultValue) {
    if (dyn.cached() instanceof Map) {
      Object o = ((Map<?, ?>) dyn.cached()).get(field);
      if (o instanceof Boolean) {
        return (Boolean) o;
      } else if ("true".equals(o)) {
        return true;
      } else if ("false".equals(o)) {
        return false;
      }
    }
    return defaultValue;
  }

  @Extension
  public static @HiddenType(clazz = String.class) NtMaybe<String> str(NtDynamic dyn, String field) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        return new NtMaybe<>((String) value);
      }
      if (value instanceof Double || value instanceof Integer || value instanceof Long) {
        return new NtMaybe<>("" + value);
      }
    }
    return new NtMaybe<>();
  }

  @Extension
  public static String str(NtDynamic dyn, String field, String defaultValue) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        return (String) value;
      }
      if (value instanceof Double || value instanceof Integer || value instanceof Long) {
        return "" + value;
      }
    }
    return defaultValue;
  }

  private static NtMaybe<NtDate> dateOf(NtMaybe<String> str) {
    if (str.has()) {
      return new NtMaybe<>(NtDate.parse(str.get()));
    } else {
      return new NtMaybe<>();
    }
  }

  @Extension
  public static @HiddenType(clazz=NtDate.class) NtMaybe<NtDate> date(NtDynamic dyn) {
    return dateOf(str(dyn));
  }

  @Extension
  public static @HiddenType(clazz=NtDate.class) NtMaybe<NtDate> date(NtDynamic dyn, String field) {
    return dateOf(str(dyn, field));
  }

  @Extension
  public static NtDate date(NtDynamic dyn, String field, String defaultValue) {
    return NtDate.parse(str(dyn, field, defaultValue));
  }

  private static NtMaybe<NtDateTime> datetimeOf(NtMaybe<String> str) {
    if (str.has()) {
      return new NtMaybe<>(NtDateTime.parse(str.get()));
    } else {
      return new NtMaybe<>();
    }
  }

  @Extension
  public static @HiddenType(clazz=NtDateTime.class) NtMaybe<NtDateTime> datetime(NtDynamic dyn) {
    return datetimeOf(str(dyn));
  }

  @Extension
  public static @HiddenType(clazz=NtDateTime.class) NtMaybe<NtDateTime> datetime(NtDynamic dyn, String field) {
    return datetimeOf(str(dyn, field));
  }

  @Extension
  public static NtDateTime datetime(NtDynamic dyn, String field, String defaultValue) {
    return NtDateTime.parse(str(dyn, field, defaultValue));
  }

  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> i(NtDynamic dyn, String field) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        try {
          return new NtMaybe<>(Integer.parseInt((String) value));
        } catch (NumberFormatException nfe) {
          return new NtMaybe<>();
        }
      }
      if (value instanceof Integer) {
        return new NtMaybe<>((Integer) value);
      }
    }
    return new NtMaybe<>();
  }

  @Extension
  public static int i(NtDynamic dyn, String field, int defaultValue) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        try {
          return Integer.parseInt((String) value);
        } catch (NumberFormatException nfe) {
          return defaultValue;
        }
      }
      if (value instanceof Integer) {
        return (Integer) value;
      }
    }
    return defaultValue;
  }

  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> l(NtDynamic dyn, String field) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        try {
          return new NtMaybe<>(Long.parseLong((String) value));
        } catch (NumberFormatException nfe) {
          return new NtMaybe<>();
        }
      }
      if (value instanceof Integer) {
        return new NtMaybe<>((long) ((Integer) value));
      }
      if (value instanceof Long) {
        return new NtMaybe<>((Long) value);
      }
    }
    return new NtMaybe<>();
  }

  @Extension
  public static long l(NtDynamic dyn, String field, long defaultValue) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        try {
          return Long.parseLong((String) value);
        } catch (NumberFormatException nfe) {
          return defaultValue;
        }
      }
      if (value instanceof Integer) {
        return (long) ((Integer) value);
      }
      if (value instanceof Long) {
        return (Long) value;
      }
    }
    return defaultValue;
  }

  @Extension
  public static @HiddenType(clazz = String.class) NtMaybe<Double> d(NtDynamic dyn, String field) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        try {
          return new NtMaybe<>(Double.parseDouble((String) value));
        } catch (NumberFormatException nfe) {
          return new NtMaybe<>();
        }
      }
      if (value instanceof Double) {
        return new NtMaybe<>((Double) value);
      } else if (value instanceof Integer) {
        return new NtMaybe<>((double) ((Integer) value));
      } else if (value instanceof Long) {
        return new NtMaybe<>((double) ((Long) value));
      }
    }
    return new NtMaybe<>();
  }

  @Extension
  public static double d(NtDynamic dyn, String field, double defaultValue) {
    if (dyn.cached() instanceof Map) {
      Object value = ((Map<?, ?>) dyn.cached()).get(field);
      if (value instanceof String) {
        try {
          return Double.parseDouble((String) value);
        } catch (NumberFormatException nfe) {
          return defaultValue;
        }
      }
      if (value instanceof Double) {
        return (Double) value;
      } else if (value instanceof Integer) {
        return (double) ((Integer) value);
      } else if (value instanceof Long) {
        return (double) ((Long) value);
      }
    }
    return defaultValue;
  }

  @Extension
  public static @HiddenType(clazz = Boolean.class) NtMaybe<Boolean> is_null(NtDynamic dyn, String field) {
    if (dyn.cached() instanceof Map) {
      if (((Map<?, ?>) dyn.cached()).containsKey(field)) {
        return new NtMaybe<>(((Map<?, ?>) dyn.cached()).get(field) == null);
      }
    }
    return new NtMaybe<>();
  }

  @Extension
  public static @HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> to_dyn(String json) {
    try {
      return new NtMaybe<>(new JsonStreamReader(json).readNtDynamic());
    } catch (Exception ex) {
      return new NtMaybe<>();
    }
  }

  @Extension
  public static String to_str(NtDynamic d) {
    return d.json;
  }

  @Extension
  public static @HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> parse(String str) {
    NtDynamic dyn = new NtDynamic(str);
    if (dyn.cached() != null) {
      return new NtMaybe<>(dyn);
    }
    return new NtMaybe<>();
  }

  /** Return the number of entries: array length or object key count. Empty if not array/object. */
  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> size(NtDynamic dyn) {
    Object o = dyn.cached();
    if (o instanceof List) {
      return new NtMaybe<>(((List<?>) o).size());
    }
    if (o instanceof Map) {
      return new NtMaybe<>(((Map<?, ?>) o).size());
    }
    return new NtMaybe<>();
  }

  /** Return the number of entries (Maybe variant). */
  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> size(@HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> dyn) {
    if (dyn.has()) {
      return size(dyn.get());
    }
    return new NtMaybe<>();
  }

  /** Return true if the dynamic value is a JSON array. */
  @Extension
  public static boolean is_array(NtDynamic dyn) {
    return dyn.cached() instanceof List;
  }

  /** Return true if the dynamic value is a JSON object. */
  @Extension
  public static boolean is_object(NtDynamic dyn) {
    return dyn.cached() instanceof Map;
  }

  /** Return true if the dynamic value is a JSON string. */
  @Extension
  public static boolean is_string(NtDynamic dyn) {
    return dyn.cached() instanceof String;
  }

  /** Return true if the dynamic value is a JSON number. */
  @Extension
  public static boolean is_number(NtDynamic dyn) {
    Object o = dyn.cached();
    return o instanceof Integer || o instanceof Long || o instanceof Double;
  }

  /** Return true if the dynamic value is a JSON boolean. */
  @Extension
  public static boolean is_bool(NtDynamic dyn) {
    return dyn.cached() instanceof Boolean;
  }

  /** Return true if the dynamic value is a JSON object that contains the given field. */
  @Extension
  public static boolean has(NtDynamic dyn, String field) {
    Object o = dyn.cached();
    if (o instanceof Map) {
      return ((Map<?, ?>) o).containsKey(field);
    }
    return false;
  }

  /** Return the list of keys from a JSON object. Returns empty list if not an object. */
  @Extension
  public static @HiddenType(clazz = String.class) NtList<String> keys(NtDynamic dyn) {
    Object o = dyn.cached();
    if (o instanceof Map) {
      ArrayList<String> result = new ArrayList<>();
      for (Object key : ((Map<?, ?>) o).keySet()) {
        result.add(key.toString());
      }
      return new ArrayNtList<>(result);
    }
    return new ArrayNtList<>(new ArrayList<>());
  }

  /** Access an element from a JSON array by index. Returns null dynamic if not an array or out of bounds. */
  @Extension
  @UseName(name = "at")
  public static NtDynamic atIndex(NtDynamic dyn, int index) {
    Object o = dyn.cached();
    if (o instanceof List) {
      List<?> list = (List<?>) o;
      if (index >= 0 && index < list.size()) {
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeTree(list.get(index));
        return new NtDynamic(writer.toString());
      }
    }
    return NtDynamic.NULL;
  }

  /** Access a field from a JSON object by name. Returns null dynamic if not an object or field not found. */
  @Extension
  @UseName(name = "at")
  public static NtDynamic atField(NtDynamic dyn, String field) {
    Object o = dyn.cached();
    if (o instanceof Map) {
      Object value = ((Map<?, ?>) o).get(field);
      if (value != null) {
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeTree(value);
        return new NtDynamic(writer.toString());
      }
    }
    return NtDynamic.NULL;
  }

  /** Deep merge two JSON objects per RFC 7396 (JSON Merge Patch). */
  @Extension
  public static NtDynamic merge(NtDynamic target, NtDynamic patch) {
    Object t = target.cached();
    Object p = patch.cached();
    if (!(p instanceof Map)) {
      return patch;
    }
    Map<String, Object> result;
    if (t instanceof Map) {
      result = new HashMap<>((Map<String, Object>) t);
    } else {
      result = new HashMap<>();
    }
    Map<String, Object> patchMap = (Map<String, Object>) p;
    for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
      if (entry.getValue() == null) {
        result.remove(entry.getKey());
      } else {
        Object existing = result.get(entry.getKey());
        if (existing != null) {
          JsonStreamWriter ew = new JsonStreamWriter();
          ew.writeTree(existing);
          NtDynamic existingDyn = new NtDynamic(ew.toString());
          JsonStreamWriter pw = new JsonStreamWriter();
          pw.writeTree(entry.getValue());
          NtDynamic patchDyn = new NtDynamic(pw.toString());
          NtDynamic merged = merge(existingDyn, patchDyn);
          result.put(entry.getKey(), merged.cached());
        } else {
          result.put(entry.getKey(), entry.getValue());
        }
      }
    }
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.writeTree(result);
    return new NtDynamic(writer.toString());
  }

  /** Build a JSON array from a list of dynamics. */
  @Extension
  public static NtDynamic arr(@HiddenType(clazz = NtDynamic.class) NtList<NtDynamic> items) {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.beginArray();
    for (NtDynamic item : items) {
      writer.injectJson(item.json);
    }
    writer.endArray();
    return new NtDynamic(writer.toString());
  }
}
