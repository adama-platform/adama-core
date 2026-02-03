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
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Static utility methods for working with Jackson JSON library.
 * Provides convenient factory methods for creating JSON nodes and
 * type-safe field accessors for reading ObjectNode properties.
 */
public class Json {
  public static final JsonMapper MAPPER = new JsonMapper();

  public static ObjectNode newJsonObject() {
    return MAPPER.createObjectNode();
  }

  public static ArrayNode newJsonArray() {
    return MAPPER.createArrayNode();
  }

  public static ObjectNode parseJsonObject(final String json) {
    try {
      return parseJsonObjectThrows(json);
    } catch (final Exception jpe) {
      throw new RuntimeException(jpe);
    }
  }

  public static ObjectNode parseJsonObjectThrows(final String json) throws Exception {
    final var node = MAPPER.readTree(json);
    if (node instanceof ObjectNode) {
      return (ObjectNode) node;
    }
    throw new Exception("given json is not an ObjectNode at root");
  }


  public static JsonNode parse(final String json) {
    try {
      return MAPPER.readTree(json);
    } catch (Exception jpe) {
      throw new RuntimeException(jpe);
    }
  }

  public static ArrayNode parseJsonArray(final String json) {
    try {
      return parseJsonArrayThrows(json);
    } catch (final Exception jpe) {
      throw new RuntimeException(jpe);
    }
  }

  public static ArrayNode parseJsonArrayThrows(final String json) throws Exception {
    final var node = MAPPER.readTree(json);
    if (node instanceof ArrayNode) {
      return (ArrayNode) node;
    }
    throw new Exception("given json is not an ArrayNode at root");
  }

  public static String readString(ObjectNode tree, String field) {
    JsonNode node = tree.get(field);
    if (node == null || node.isNull()) {
      return null;
    }
    if (node.isTextual()) {
      return node.textValue();
    }
    return node.toString();
  }

  public static String readStringAndRemove(ObjectNode tree, String field) {
    JsonNode node = tree.remove(field);
    if (node == null || node.isNull()) {
      return null;
    }
    if (node.isTextual()) {
      return node.textValue();
    }
    return node.toString();
  }

  public static Boolean readBool(ObjectNode tree, String field) {
    JsonNode node = tree.get(field);
    if (node == null || node.isNull() || !node.isBoolean()) {
      return null;
    }
    return node.booleanValue();
  }


  public static boolean readBool(ObjectNode tree, String field, boolean defaultValue) {
    JsonNode node = tree.get(field);
    if (node == null || node.isNull() || !node.isBoolean()) {
      return defaultValue;
    }
    return node.booleanValue();
  }

  public static Integer readInteger(ObjectNode tree, String field) {
    Long lng = readLong(tree, field);
    if (lng != null) {
      return lng.intValue();
    }
    return null;
  }

  public static int readInteger(ObjectNode tree, String field, int defaultValue) {
    Integer ival = readInteger(tree, field);
    if (ival == null) {
      return defaultValue;
    }
    return ival.intValue();
  }

  public static Long readLong(ObjectNode tree, String field) {
    JsonNode node = tree.get(field);
    if (node == null || node.isNull()) {
      return null;
    }
    if (node.isIntegralNumber()) {
      return node.longValue();
    }
    if (node.isTextual()) {
      try {
        return Long.parseLong(node.textValue());
      } catch (NumberFormatException nfe) {
        return null;
      }
    }
    return null;
  }

  public static ObjectNode readObject(ObjectNode tree, String field) {
    JsonNode node = tree.get(field);
    if (node == null || node.isNull() || !node.isObject()) {
      return null;
    }
    return (ObjectNode) node;
  }

  public static JsonNode readJsonNode(ObjectNode tree, String field) {
    return tree.get(field);
  }
}
