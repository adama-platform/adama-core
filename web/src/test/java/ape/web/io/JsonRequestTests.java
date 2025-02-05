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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.ErrorCodeException;
import ape.common.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

public class JsonRequestTests {
  public static final ConnectionContext CONTEXT = new ConnectionContext( "http://blah","1.1.1.1", "agent", new TreeMap<>());

  @Test
  public void noMethod() throws Exception {
    JsonRequest request = new JsonRequest(of("{}"), CONTEXT);
    try {
      request.method();
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(213708, ece.code);
    }
  }

  private static ObjectNode of(String json) throws Exception {
    return Json.parseJsonObject(json);
  }

  @Test
  public void noID() throws Exception {
    JsonRequest request = new JsonRequest(of("{}"), CONTEXT);
    try {
      request.id();
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(233120, ece.code);
    }
  }

  @Test
  public void bothIdAndMethod() throws Exception {
    JsonRequest request = new JsonRequest(of("{\"id\":123,\"method\":\"m\"}"), CONTEXT);
    Assert.assertEquals(123, request.id());
    Assert.assertEquals("m", request.method());
  }

  @Test
  public void getString() throws Exception {
    JsonRequest request = new JsonRequest(of("{\"x\":true,\"y\":null,\"z\":42.5,\"w\":\"xyz\"}"), CONTEXT);
    try {
      request.getString("x", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getString("x", false, 123));
    try {
      request.getString("y", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getString("y", false, 123));
    Assert.assertEquals("42.5", request.getString("z", true, 4));
    Assert.assertEquals("xyz", request.getString("w", true, 42));
    Assert.assertEquals("xyz", request.getString("w", false, 42));
    try {
      request.getString("t", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getString("t", false, 123));
  }

  @Test
  public void getStringNormalize() throws Exception {
    JsonRequest request = new JsonRequest(of("{\"w\":\" XyzNow \"}"), CONTEXT);
    Assert.assertEquals("xyznow", request.getStringNormalize("w", true, 1));
    Assert.assertNull(request.getStringNormalize("xxx", false, 1));
  }

  @Test
  public void getBoolean() throws Exception {
    JsonRequest request = new JsonRequest(of("{\"x\":true,\"y\":null,\"z\":42.5,\"w\":\"xyz\"}"), CONTEXT);
    Assert.assertTrue(request.getBoolean("x", true, 123));
    Assert.assertNull(request.getBoolean("y", false, -1));
    Assert.assertNull(request.getBoolean("z", false, -1));
    try {
      Assert.assertNull(request.getBoolean("y", true, -123));
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(-123, exc.code);
    }
  }

  @Test
  public void logging() throws Exception {
    JsonRequest request = new JsonRequest(of("{\"x\":true,\"y\":null,\"z\":42.5,\"w\":4,\"w2\":\"5\",\"w3\":\"x\"}"), CONTEXT);
    ObjectNode logItem = Json.newJsonObject();
    request.dumpIntoLog(logItem);
    Assert.assertEquals("{\"ip\":\"1.1.1.1\",\"origin\":\"http://blah\"}", logItem.toString());
  }

  @Test
  public void getInteger() throws Exception {
    JsonRequest request =
        new JsonRequest(of("{\"x\":true,\"y\":null,\"z\":42.5,\"w\":4,\"w2\":\"5\",\"w3\":\"x\"}"), CONTEXT);
    try {
      request.getInteger("x", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getInteger("x", false, 123));
    try {
      request.getInteger("y", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getInteger("y", false, 123));
    try {
      request.getInteger("z", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertEquals(4, (int) request.getInteger("w", true, 42));
    Assert.assertEquals(4, (int) request.getInteger("w", false, 42));
    Assert.assertEquals(5, (int) request.getInteger("w2", true, 42));
    Assert.assertEquals(5, (int) request.getInteger("w2", false, 42));
    try {
      request.getInteger("w3", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    try {
      request.getInteger("t", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getInteger("t", false, 123));
  }

  @Test
  public void getLong() throws Exception {
    JsonRequest request =
        new JsonRequest(of("{\"x\":true,\"y\":null,\"z\":42.5,\"w\":4,\"w2\":\"5\",\"w3\":\"x\"}"), CONTEXT);
    try {
      request.getLong("x", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getLong("x", false, 123));
    try {
      request.getLong("y", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getLong("y", false, 123));
    try {
      request.getLong("z", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertEquals(4, (long) request.getLong("w", true, 42));
    Assert.assertEquals(4, (long) request.getLong("w", false, 42));
    Assert.assertEquals(5, (long) request.getLong("w2", true, 42));
    Assert.assertEquals(5, (long) request.getLong("w2", false, 42));
    try {
      request.getLong("w3", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    try {
      request.getLong("t", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getLong("t", false, 123));
  }

  @Test
  public void getObject() throws Exception {
    JsonRequest request = new JsonRequest(of("{\"x\":true,\"y\":null,\"z\":{}}"), CONTEXT);
    try {
      request.getObject("x", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getObject("x", false, 123));
    try {
      request.getObject("y", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getObject("y", false, 123));
    Assert.assertNotNull(request.getObject("z", true, 2));
    Assert.assertNotNull(request.getObject("z", false, 2));
  }

  @Test
  public void getNode() throws Exception {
    JsonRequest request = new JsonRequest(of("{\"x\":true,\"y\":null,\"z1\":{},\"z2\":[]}"), CONTEXT);
    try {
      request.getJsonNode("x", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getObject("x", false, 123));
    try {
      request.getJsonNode("y", true, 123);
    } catch (ErrorCodeException exc) {
      Assert.assertEquals(123, exc.code);
    }
    Assert.assertNull(request.getJsonNode("y", false, 123));
    Assert.assertNotNull(request.getJsonNode("z1", true, 2));
    Assert.assertNotNull(request.getJsonNode("z1", false, 2));
    Assert.assertNotNull(request.getJsonNode("z2", true, 2));
    Assert.assertNotNull(request.getJsonNode("z2", false, 2));
  }
}
