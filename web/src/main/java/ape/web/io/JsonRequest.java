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
package ape.web.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.ErrorCodes;
import ape.common.ErrorCodeException;

import java.util.Locale;

/** a thin wrapper for easy access to a JSON request */
public class JsonRequest {
  public final ConnectionContext context;
  private final ObjectNode node;

  public JsonRequest(ObjectNode node, ConnectionContext context) {
    this.node = node;
    this.context = context;
  }

  public int id() throws ErrorCodeException {
    return getInteger("id", true, ErrorCodes.USERLAND_REQUEST_NO_ID_PROPERTY);
  }

  public Integer getInteger(String field, boolean mustExist, int errorIfDoesnt) throws ErrorCodeException {
    final var fieldNode = node.get(field);
    if (fieldNode == null || fieldNode.isNull() || !(fieldNode.isNumber() && fieldNode.isIntegralNumber() || fieldNode.isTextual())) {
      if (mustExist) {
        throw new ErrorCodeException(errorIfDoesnt);
      } else {
        return null;
      }
    }
    if (fieldNode.isTextual()) {
      try {
        return Integer.parseInt(fieldNode.textValue());
      } catch (NumberFormatException nfe) {
        throw new ErrorCodeException(errorIfDoesnt);
      }
    }
    return fieldNode.intValue();
  }

  public void dumpIntoLog(ObjectNode logItem) {
    logItem.put("ip", context.remoteIp);
    logItem.put("origin", context.origin);
  }

  public String method() throws ErrorCodeException {
    return getString("method", true, ErrorCodes.USERLAND_REQUEST_NO_METHOD_PROPERTY);
  }

  public String getString(String field, boolean mustExist, int errorIfDoesnt) throws ErrorCodeException {
    final var fieldNode = node.get(field);
    if (fieldNode == null || fieldNode.isNull() || !(fieldNode.isTextual() || fieldNode.isNumber())) {
      if (mustExist) {
        throw new ErrorCodeException(errorIfDoesnt);
      }
      return null;
    }
    if (fieldNode.isNumber()) {
      return fieldNode.numberValue().toString();
    }
    return fieldNode.textValue();
  }

  public String getStringNormalize(String field, boolean mustExist, int errorIfDoesnt) throws ErrorCodeException {
    String value = getString(field, mustExist, errorIfDoesnt);
    if (value != null) {
      return value.toLowerCase(Locale.ENGLISH).trim();
    }
    return value;
  }

  public Boolean getBoolean(String field, boolean mustExist, int errorIfDoesnt) throws ErrorCodeException {
    final var fieldNode = node.get(field);
    if (fieldNode == null || fieldNode.isNull() || !fieldNode.isBoolean()) {
      if (mustExist) {
        throw new ErrorCodeException(errorIfDoesnt);
      }
      return null;
    }
    return fieldNode.booleanValue();
  }

  public Long getLong(String field, boolean mustExist, int errorIfDoesnt) throws ErrorCodeException {
    final var fieldNode = node.get(field);
    if (fieldNode == null || fieldNode.isNull() || !(fieldNode.isNumber() && fieldNode.isIntegralNumber() || fieldNode.isTextual())) {
      if (mustExist) {
        throw new ErrorCodeException(errorIfDoesnt);
      } else {
        return null;
      }
    }
    if (fieldNode.isTextual()) {
      try {
        return Long.parseLong(fieldNode.textValue());
      } catch (NumberFormatException nfe) {
        throw new ErrorCodeException(errorIfDoesnt);
      }
    }
    return fieldNode.longValue();
  }

  public ObjectNode getObject(String field, boolean mustExist, int errorIfDoesnt) throws ErrorCodeException {
    final var fieldNode = node.get(field);
    if (fieldNode == null || fieldNode.isNull() || !fieldNode.isObject()) {
      if (mustExist) {
        throw new ErrorCodeException(errorIfDoesnt);
      } else {
        return null;
      }
    }
    return (ObjectNode) fieldNode;
  }

  public JsonNode getJsonNode(String field, boolean mustExist, int errorIfDoesnt) throws ErrorCodeException {
    final var fieldNode = node.get(field);
    if (fieldNode == null || fieldNode.isNull() || !(fieldNode.isObject() || fieldNode.isArray())) {
      if (mustExist) {
        throw new ErrorCodeException(errorIfDoesnt);
      } else {
        return null;
      }
    }
    return fieldNode;
  }
}
