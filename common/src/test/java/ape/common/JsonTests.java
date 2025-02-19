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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

public class JsonTests {
  @Test
  public void coverage() throws Exception {
    Json.newJsonObject();
    Json.parseJsonObject("{}");
    Json.newJsonArray();
    boolean failure = true;
    try {
      Json.parseJsonObjectThrows("x");
      failure = false;
    } catch (Exception ex) {

    }
    try {
      Json.parseJsonObjectThrows("[]");
      failure = false;
    } catch (Exception ex) {

    }
    try {
      Json.parseJsonObject("x");
      failure = false;
    } catch (RuntimeException ex) {
    }
    try {
      Json.parseJsonObject("[]");
      failure = false;
    } catch (RuntimeException ex) {
    }
    Json.parseJsonArray("[]");
    try {
      Json.parseJsonArray("{}");
      failure = false;
    } catch (Exception ex) {

    }
    try {
      Json.parseJsonArray("x");
      failure = false;
    } catch (Exception ex) {

    }
    Assert.assertTrue(failure);
  }

  @Test
  public void just_parse() {
    Json.parse("1");
    Json.parse("[]");
    Json.parse("{}");
    try {
      Json.parse("x");
      Assert.fail();
    } catch (Exception ex) {

    }
  }

  @Test
  public void subfield_bool() {
    Assert.assertNull(Json.readBool(Json.parseJsonObject("{\"x\":\"1234\"}"), "x"));
    Assert.assertNull(Json.readBool(Json.parseJsonObject("{\"x\":123}"), "x"));
    Assert.assertNull(Json.readBool(Json.parseJsonObject("{}"), "x"));
    Assert.assertFalse(Json.readBool(Json.parseJsonObject("{\"x\":false}"), "x"));
    Assert.assertTrue(Json.readBool(Json.parseJsonObject("{\"x\":true}"), "x"));
  }

  @Test
  public void subfield_str() {
    Assert.assertEquals("1234", Json.readString(Json.parseJsonObject("{\"x\":\"1234\"}"), "x"));
    Assert.assertEquals("123", Json.readString(Json.parseJsonObject("{\"x\":123}"), "x"));
    Assert.assertNull(Json.readString(Json.parseJsonObject("{}"), "x"));
  }

  @Test
  public void str_remove2() {
    Assert.assertNull(Json.readStringAndRemove(Json.parseJsonObject("{\"y\":\"1234\"}"), "x"));
    Assert.assertEquals("true", Json.readStringAndRemove(Json.parseJsonObject("{\"x\":true}"), "x"));
  }

  @Test
  public void str_remove1() {
    ObjectNode node = Json.parseJsonObject("{\"x\":\"1234\"}");
    Assert.assertEquals("1234", Json.readStringAndRemove(node, "x"));
    Assert.assertEquals("{}", node.toString());
  }

  @Test
  public void subfield_lng() {
    Assert.assertEquals(1234, (long) Json.readLong(Json.parseJsonObject("{\"x\":\"1234\"}"), "x"));
    Assert.assertEquals(123L, (long) Json.readLong(Json.parseJsonObject("{\"x\":123}"), "x"));
    Assert.assertNull(Json.readLong(Json.parseJsonObject("{}"), "x"));
    Assert.assertNull(Json.readLong(Json.parseJsonObject("{\"x\":null}"), "x"));
    Assert.assertNull(Json.readLong(Json.parseJsonObject("{\"x\":true}"), "x"));
    Assert.assertNull(Json.readLong(Json.parseJsonObject("{\"x\":\"zep\"}"), "x"));
  }

  @Test
  public void subfield_int() {
    Assert.assertEquals(1234, (int) Json.readInteger(Json.parseJsonObject("{\"x\":\"1234\"}"), "x"));
    Assert.assertEquals(123, (int) Json.readInteger(Json.parseJsonObject("{\"x\":123}"), "x"));
    Assert.assertNull(Json.readInteger(Json.parseJsonObject("{}"), "x"));
    Assert.assertNull(Json.readInteger(Json.parseJsonObject("{\"x\":null}"), "x"));
    Assert.assertNull(Json.readInteger(Json.parseJsonObject("{\"x\":\"zep\"}"), "x"));
  }

  @Test
  public void subfield_int_def() {
    Assert.assertEquals(123, Json.readInteger(Json.parseJsonObject("{}"), "x", 123));
    Assert.assertEquals(42, Json.readInteger(Json.parseJsonObject("{\"x\":42}"), "x", 123));
  }

  @Test
  public void subfield_bool_def() {
    Assert.assertEquals(false, Json.readBool(Json.parseJsonObject("{}"), "x", false));
    Assert.assertEquals(true, Json.readBool(Json.parseJsonObject("{}"), "x", true));
    Assert.assertEquals(true, Json.readBool(Json.parseJsonObject("{\"x\":true}"), "x", true));
    Assert.assertEquals(true, Json.readBool(Json.parseJsonObject("{\"x\":true}"), "x", false));
  }

  @Test
  public void subfield_objs() {
    Assert.assertNotNull(Json.readObject(Json.parseJsonObject("{\"x\":{}}"), "x"));
    Assert.assertNull(Json.readObject(Json.parseJsonObject("{}"), "x"));
    Assert.assertNotNull(Json.readJsonNode(Json.parseJsonObject("{\"x\":{}}"), "x"));
  }
}
