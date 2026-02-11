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

import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtJson;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.lists.ArrayNtList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class LibDynamicTests {

  @Test
  public void coverage_dyn() {
    Assert.assertEquals("123", LibDynamic.dyn(123).json);
    Assert.assertEquals("3.14", LibDynamic.dyn(3.14).json);
    Assert.assertEquals("123", LibDynamic.dyn(123L).json);
    Assert.assertEquals("true", LibDynamic.dyn(true).json);
    Assert.assertEquals("\"xyz\"", LibDynamic.dyn("xyz").json);
  }

  @Test
  public void to_str() {
    Assert.assertEquals("{}", LibDynamic.to_str(new NtDynamic("{}")));
  }

  @Test
  public void coverage_raw_str() {
    Assert.assertFalse(LibDynamic.str(new NtMaybe<>()).has());
    Assert.assertFalse(LibDynamic.str(new NtDynamic("{}")).has());
    Assert.assertEquals("xyz", LibDynamic.str(LibDynamic.to_dyn("\"xyz\"")).get());
    Assert.assertEquals("123", LibDynamic.str(LibDynamic.to_dyn("123")).get());
    Assert.assertEquals("3.14", LibDynamic.str(LibDynamic.to_dyn("3.14")).get());
    Assert.assertEquals("true", LibDynamic.str(LibDynamic.to_dyn("true")).get());
    Assert.assertEquals("xyz", LibDynamic.str(new NtMaybe<>(LibDynamic.to_dyn("\"xyz\""))).get());
    Assert.assertEquals("123", LibDynamic.str(new NtMaybe<>(LibDynamic.to_dyn("123"))).get());
    Assert.assertEquals("3.14", LibDynamic.str(new NtMaybe<>(LibDynamic.to_dyn("3.14"))).get());
  }

  @Test
  public void coverage_raw_d() {
    Assert.assertFalse(LibDynamic.d(new NtMaybe<>()).has());
    Assert.assertFalse(LibDynamic.d(LibDynamic.to_dyn("\"xyz\"")).has());
    Assert.assertEquals(123, LibDynamic.d(LibDynamic.to_dyn("123")).get(), 0.01);
    Assert.assertEquals(3.14, LibDynamic.d(LibDynamic.to_dyn("3.14")).get(), 0.01);
    Assert.assertEquals(42424242424242.0, LibDynamic.d(LibDynamic.to_dyn("42424242424242")).get(), 0.01);
    Assert.assertFalse(LibDynamic.d(LibDynamic.to_dyn("false")).has());
    Assert.assertFalse(LibDynamic.d(new NtMaybe<>(LibDynamic.to_dyn("\"xyz\""))).has());
    Assert.assertEquals(123, LibDynamic.d(new NtMaybe<>(LibDynamic.to_dyn("123"))).get(), 0.01);
    Assert.assertEquals(3.14, LibDynamic.d(new NtMaybe<>(LibDynamic.to_dyn("3.14"))).get(), 0.01);
    Assert.assertFalse(LibDynamic.d(new NtMaybe<>(LibDynamic.to_dyn("false"))).has());
  }

  @Test
  public void coverage_raw_l() {
    Assert.assertFalse(LibDynamic.l(new NtMaybe<>()).has());
    Assert.assertFalse(LibDynamic.l(LibDynamic.to_dyn("\"xyz\"")).has());
    Assert.assertEquals(123, LibDynamic.l(LibDynamic.to_dyn("123")).get(), 0.01);
    Assert.assertFalse(LibDynamic.l(LibDynamic.to_dyn("3.14")).has());
    Assert.assertFalse(LibDynamic.l(LibDynamic.to_dyn("false")).has());
    Assert.assertFalse(LibDynamic.l(new NtMaybe<>(LibDynamic.to_dyn("\"xyz\""))).has());
    Assert.assertEquals(123, LibDynamic.l(new NtMaybe<>(LibDynamic.to_dyn("123"))).get(), 0.01);
    Assert.assertEquals(42424242424242L, LibDynamic.l(new NtMaybe<>(LibDynamic.to_dyn("42424242424242"))).get(), 0.01);
    Assert.assertFalse(LibDynamic.l(new NtMaybe<>(LibDynamic.to_dyn("3.14"))).has());
    Assert.assertFalse(LibDynamic.l(new NtMaybe<>(LibDynamic.to_dyn("false"))).has());
  }

  @Test
  public void coverage_raw_i() {
    Assert.assertFalse(LibDynamic.i(new NtMaybe<>()).has());
    Assert.assertFalse(LibDynamic.i(LibDynamic.to_dyn("\"xyz\"")).has());
    Assert.assertEquals(123, LibDynamic.i(LibDynamic.to_dyn("123")).get(), 0.01);
    Assert.assertFalse(LibDynamic.i(LibDynamic.to_dyn("3.14")).has());
    Assert.assertFalse(LibDynamic.i(LibDynamic.to_dyn("false")).has());
    Assert.assertFalse(LibDynamic.i(new NtMaybe<>(LibDynamic.to_dyn("\"xyz\""))).has());
    Assert.assertEquals(123, LibDynamic.i(new NtMaybe<>(LibDynamic.to_dyn("123"))).get(), 0.01);
    Assert.assertFalse(LibDynamic.i(new NtMaybe<>(LibDynamic.to_dyn("3.14"))).has());
    Assert.assertFalse(LibDynamic.i(new NtMaybe<>(LibDynamic.to_dyn("false"))).has());
  }

  @Test
  public void coverage_raw_b() {
    Assert.assertFalse(LibDynamic.b(new NtMaybe<>()).has());
    Assert.assertFalse(LibDynamic.b(LibDynamic.to_dyn("\"xyz\"")).has());
    Assert.assertTrue(LibDynamic.b(new NtMaybe<>(LibDynamic.to_dyn("true"))).get());
    Assert.assertFalse(LibDynamic.b(new NtMaybe<>(LibDynamic.to_dyn("false"))).get());
    Assert.assertTrue(LibDynamic.b(new NtMaybe<>(LibDynamic.to_dyn("\"true\""))).get());
    Assert.assertFalse(LibDynamic.b(new NtMaybe<>(LibDynamic.to_dyn("\"false\""))).get());
  }

  @Test
  public void coverage_str() {
    Assert.assertEquals("{}", LibDynamic.to_dyn("{}").get().json);
    Assert.assertFalse(LibDynamic.to_dyn("x").has());
    Assert.assertEquals("here", LibDynamic.str(new NtDynamic("{\"x\":\"here\"}"), "x").get());
    Assert.assertEquals("123", LibDynamic.str(new NtDynamic("{\"x\":123}"), "x").get());
    Assert.assertEquals("123.4", LibDynamic.str(new NtDynamic("{\"x\":123.4}"), "x").get());
    Assert.assertFalse(LibDynamic.str(new NtDynamic("{}"), "x").has());
  }

  @Test
  public void coverage_str_defaults() {
    Assert.assertEquals("{}", LibDynamic.to_dyn("{}").get().json);
    Assert.assertFalse(LibDynamic.to_dyn("x").has());
    Assert.assertEquals("here", LibDynamic.str(new NtDynamic("{\"x\":\"here\"}"), "x", "def"));
    Assert.assertEquals("123", LibDynamic.str(new NtDynamic("{\"x\":123}"), "x", "def"));
    Assert.assertEquals("123.4", LibDynamic.str(new NtDynamic("{\"x\":123.4}"), "x", "def"));
    Assert.assertEquals("def", LibDynamic.str(new NtDynamic("{}"), "x", "def"));
  }

  @Test
  public void coverage_integer() {
    Assert.assertEquals(123, (int) (LibDynamic.i(new NtDynamic("{\"x\":\"123\"}"), "x").get()));
    Assert.assertEquals(123, (int) (LibDynamic.i(new NtDynamic("{\"x\":123}"), "x").get()));
    Assert.assertFalse(LibDynamic.i(new NtDynamic("{}"), "x").has());
    Assert.assertFalse(LibDynamic.i(new NtDynamic("{\"x\":\"4.2\"}"), "x").has());
    Assert.assertFalse(LibDynamic.i(new NtDynamic("{\"x\":\"xyz\"}"), "x").has());
    Assert.assertFalse(LibDynamic.i(new NtDynamic("null"), "x").has());
  }

  @Test
  public void coverage_integer_defaults() {
    Assert.assertEquals(123, (int) (LibDynamic.i(new NtDynamic("{\"x\":\"123\"}"), "x", 0)));
    Assert.assertEquals(123, (int) (LibDynamic.i(new NtDynamic("{\"x\":123}"), "x", 0)));
    Assert.assertEquals(42, LibDynamic.i(new NtDynamic("{}"), "x", 42));
    Assert.assertEquals(42, LibDynamic.i(new NtDynamic("{\"x\":\"4.2\"}"), "x", 42));
    Assert.assertEquals(42, LibDynamic.i(new NtDynamic("{\"x\":\"xyz\"}"), "x", 42));
    Assert.assertEquals(42, LibDynamic.i(new NtDynamic("null"), "x", 42));
  }

  @Test
  public void coverage_long() {
    Assert.assertEquals(123L, (long) (LibDynamic.l(new NtDynamic("{\"x\":\"123\"}"), "x").get()));
    Assert.assertEquals(123L, (long) (LibDynamic.l(new NtDynamic("{\"x\":123}"), "x").get()));
    Assert.assertEquals(42424242424242L, (long) (LibDynamic.l(new NtDynamic("{\"x\":42424242424242}"), "x").get()));
    Assert.assertFalse(LibDynamic.l(new NtDynamic("{}"), "x").has());
    Assert.assertFalse(LibDynamic.l(new NtDynamic("{\"x\":\"4.2\"}"), "x").has());
    Assert.assertFalse(LibDynamic.l(new NtDynamic("{\"x\":\"xyz\"}"), "x").has());
    Assert.assertFalse(LibDynamic.l(new NtDynamic("null"), "x").has());
  }

  @Test
  public void coverage_long_defaults() {
    Assert.assertEquals(123L, (long) (LibDynamic.l(new NtDynamic("{\"x\":\"123\"}"), "x", 0)));
    Assert.assertEquals(123L, (long) (LibDynamic.l(new NtDynamic("{\"x\":123}"), "x", 0)));
    Assert.assertEquals(42424242424242L, (long) (LibDynamic.l(new NtDynamic("{\"x\":42424242424242}"), "x", 0)));
    Assert.assertEquals(42L, LibDynamic.l(new NtDynamic("{}"), "x", 42L));
    Assert.assertEquals(42L, LibDynamic.l(new NtDynamic("{\"x\":\"4.2\"}"), "x", 42L));
    Assert.assertEquals(42L, LibDynamic.l(new NtDynamic("{\"x\":\"xyz\"}"), "x", 42L));
    Assert.assertEquals(42L, LibDynamic.l(new NtDynamic("null"), "x", 42L));
  }

  @Test
  public void coverage_double() {
    Assert.assertEquals(42.123, (LibDynamic.d(new NtDynamic("{\"x\":\"42.123\"}"), "x").get()), 0.1);
    Assert.assertEquals(42.123, (LibDynamic.d(new NtDynamic("{\"x\":42.123}"), "x").get()), 0.1);
    Assert.assertEquals(42, (LibDynamic.d(new NtDynamic("{\"x\":42}"), "x").get()), 0.1);
    Assert.assertEquals(424242424242.0, (LibDynamic.d(new NtDynamic("{\"x\":424242424242}"), "x").get()), 0.1);
    Assert.assertFalse(LibDynamic.d(new NtDynamic("{}"), "x").has());
    Assert.assertFalse(LibDynamic.d(new NtDynamic("{\"x\":\"str\"}"), "x").has());
    Assert.assertFalse(LibDynamic.d(new NtDynamic("null"), "x").has());
  }

  @Test
  public void coverage_double_defaults() {
    Assert.assertEquals(42.123, (LibDynamic.d(new NtDynamic("{\"x\":\"42.123\"}"), "x", -1.0)), 0.1);
    Assert.assertEquals(42.123, (LibDynamic.d(new NtDynamic("{\"x\":42.123}"), "x", -1.0)), 0.1);
    Assert.assertEquals(42, (LibDynamic.d(new NtDynamic("{\"x\":42}"), "x", -1.0)), 0.1);
    Assert.assertEquals(424242424242.0, (LibDynamic.d(new NtDynamic("{\"x\":424242424242}"), "x", -1.0)), 0.1);
    Assert.assertEquals(-1, LibDynamic.d(new NtDynamic("{}"), "x", -1.0), 0.1);
    Assert.assertEquals(-1, LibDynamic.d(new NtDynamic("{\"x\":\"str\"}"), "x", -1.0), 0.1);
    Assert.assertEquals(-1, LibDynamic.d(new NtDynamic("null"), "x", -1.0), 0.1);
  }

  @Test
  public void coverage_boolean() {
    Assert.assertTrue((LibDynamic.b(new NtDynamic("{\"x\":\"true\"}"), "x").get()));
    Assert.assertFalse((LibDynamic.b(new NtDynamic("{\"x\":\"false\"}"), "x").get()));
    Assert.assertTrue((LibDynamic.b(new NtDynamic("{\"x\":true}"), "x").get()));
    Assert.assertFalse((LibDynamic.b(new NtDynamic("{\"x\":false}"), "x").get()));
    Assert.assertFalse(LibDynamic.b(new NtDynamic("{\"x\":\"4.2\"}"), "x").has());
    Assert.assertFalse(LibDynamic.b(new NtDynamic("{\"x\":4.2}"), "x").has());
    Assert.assertFalse(LibDynamic.b(new NtDynamic("null"), "x").has());
  }

  @Test
  public void coverage_boolean_defaults() {
    Assert.assertTrue((LibDynamic.b(new NtDynamic("{\"x\":\"true\"}"), "x", true)));
    Assert.assertFalse((LibDynamic.b(new NtDynamic("{\"x\":\"false\"}"), "x", true)));
    Assert.assertTrue((LibDynamic.b(new NtDynamic("{\"x\":\"true\"}"), "x", false)));
    Assert.assertFalse((LibDynamic.b(new NtDynamic("{\"x\":\"false\"}"), "x", false)));
    Assert.assertTrue((LibDynamic.b(new NtDynamic("{\"x\":true}"), "x", false)));
    Assert.assertFalse((LibDynamic.b(new NtDynamic("{\"x\":false}"), "x", false)));
    Assert.assertTrue((LibDynamic.b(new NtDynamic("{\"x\":true}"), "x", true)));
    Assert.assertFalse((LibDynamic.b(new NtDynamic("{\"x\":false}"), "x", true)));
    Assert.assertTrue(LibDynamic.b(new NtDynamic("{\"x\":\"4.2\"}"), "x", true));
    Assert.assertFalse(LibDynamic.b(new NtDynamic("{\"x\":\"4.2\"}"), "x", false));
    Assert.assertTrue(LibDynamic.b(new NtDynamic("{\"x\":4.2}"), "x", true));
    Assert.assertFalse(LibDynamic.b(new NtDynamic("{\"x\":4.2}"), "x", false));
    Assert.assertTrue(LibDynamic.b(new NtDynamic("null"), "x", true));
    Assert.assertFalse(LibDynamic.b(new NtDynamic("null"), "x", false));
  }

  @Test
  public void coverage_is_null() {
    Assert.assertFalse((LibDynamic.is_null(new NtDynamic("{\"x\":\"true\"}"), "x").get()));
    Assert.assertTrue((LibDynamic.is_null(new NtDynamic("{\"x\":null}"), "x").get()));
    Assert.assertFalse((LibDynamic.is_null(new NtDynamic("{}"), "x").has()));
    Assert.assertFalse(LibDynamic.is_null(new NtDynamic("null"), "x").has());
  }

  @Test
  public void coverage_date() {
    Assert.assertEquals("2000-07-01", LibDynamic.date(new NtDynamic("\"2000/7/1\"")).get().toString());
    Assert.assertEquals("2000-07-01", LibDynamic.date(new NtDynamic("{\"x\":\"2000/7/1\"}"), "x").get().toString());
    Assert.assertEquals("2000-07-01", LibDynamic.date(new NtDynamic("{\"x\":\"2000/7/1\"}"), "x", "-").toString());
  }

  @Test
  public void coverage_datetime() {
    Assert.assertEquals("2023-04-24T17:57:19.802528800-05:00[America/Chicago]", LibDynamic.datetime(new NtDynamic("\"2023-04-24T17:57:19.802528800-05:00[America/Chicago]\"")).get().toString());
    Assert.assertEquals("2023-04-24T17:57:19.802528800-05:00[America/Chicago]", LibDynamic.datetime(new NtDynamic("{\"x\":\"2023-04-24T17:57:19.802528800-05:00[America/Chicago]\"}"), "x").get().toString());
    Assert.assertEquals("2023-04-24T17:57:19.802528800-05:00[America/Chicago]", LibDynamic.datetime(new NtDynamic("{\"x\":\"2023-04-24T17:57:19.802528800-05:00[America/Chicago]\"}"), "x", "-").toString());
  }

  @Test
  public void parse() {
    Assert.assertTrue(LibDynamic.parse("{}").has());
    Assert.assertFalse(LibDynamic.parse("xyz").has());
  }

  @Test
  public void coverage_size() {
    Assert.assertEquals(3, (int) LibDynamic.size(new NtDynamic("[1,2,3]")).get());
    Assert.assertEquals(2, (int) LibDynamic.size(new NtDynamic("{\"a\":1,\"b\":2}")).get());
    Assert.assertFalse(LibDynamic.size(new NtDynamic("42")).has());
    Assert.assertFalse(LibDynamic.size(new NtDynamic("\"str\"")).has());
    Assert.assertEquals(3, (int) LibDynamic.size(new NtMaybe<>(new NtDynamic("[1,2,3]"))).get());
    Assert.assertFalse(LibDynamic.size(new NtMaybe<NtDynamic>()).has());
  }

  @Test
  public void coverage_type_checks() {
    Assert.assertTrue(LibDynamic.is_array(new NtDynamic("[1,2]")));
    Assert.assertFalse(LibDynamic.is_array(new NtDynamic("{}")));
    Assert.assertTrue(LibDynamic.is_object(new NtDynamic("{}")));
    Assert.assertFalse(LibDynamic.is_object(new NtDynamic("[1]")));
    Assert.assertTrue(LibDynamic.is_string(new NtDynamic("\"hello\"")));
    Assert.assertFalse(LibDynamic.is_string(new NtDynamic("42")));
    Assert.assertTrue(LibDynamic.is_number(new NtDynamic("42")));
    Assert.assertTrue(LibDynamic.is_number(new NtDynamic("3.14")));
    Assert.assertFalse(LibDynamic.is_number(new NtDynamic("\"str\"")));
    Assert.assertTrue(LibDynamic.is_bool(new NtDynamic("true")));
    Assert.assertFalse(LibDynamic.is_bool(new NtDynamic("42")));
  }

  @Test
  public void coverage_has() {
    Assert.assertTrue(LibDynamic.has(new NtDynamic("{\"x\":1}"), "x"));
    Assert.assertFalse(LibDynamic.has(new NtDynamic("{\"x\":1}"), "y"));
    Assert.assertFalse(LibDynamic.has(new NtDynamic("[1,2]"), "x"));
  }

  @Test
  public void coverage_keys() {
    NtList<String> keys = LibDynamic.keys(new NtDynamic("{\"a\":1,\"b\":2}"));
    Assert.assertEquals(2, keys.size());
    NtList<String> emptyKeys = LibDynamic.keys(new NtDynamic("[1,2]"));
    Assert.assertEquals(0, emptyKeys.size());
  }

  @Test
  public void coverage_at() {
    Assert.assertEquals("2", LibDynamic.atIndex(new NtDynamic("[1,2,3]"), 1).json);
    Assert.assertEquals("null", LibDynamic.atIndex(new NtDynamic("[1,2,3]"), 5).json);
    Assert.assertEquals("null", LibDynamic.atIndex(new NtDynamic("42"), 0).json);
    Assert.assertEquals("1", LibDynamic.atField(new NtDynamic("{\"x\":1}"), "x").json);
    Assert.assertEquals("null", LibDynamic.atField(new NtDynamic("{\"x\":1}"), "y").json);
    Assert.assertEquals("null", LibDynamic.atField(new NtDynamic("[1,2]"), "x").json);
  }

  @Test
  public void coverage_merge() {
    NtDynamic target = new NtDynamic("{\"a\":1,\"b\":2}");
    NtDynamic patch = new NtDynamic("{\"b\":3,\"c\":4}");
    NtDynamic merged = LibDynamic.merge(target, patch);
    // merged should have a=1, b=3, c=4
    Assert.assertEquals(1, (int) LibDynamic.i(merged, "a").get());
    Assert.assertEquals(3, (int) LibDynamic.i(merged, "b").get());
    Assert.assertEquals(4, (int) LibDynamic.i(merged, "c").get());

    // merge with non-object patch returns patch
    NtDynamic patchScalar = new NtDynamic("42");
    Assert.assertEquals("42", LibDynamic.merge(target, patchScalar).json);

    // merge null removal
    NtDynamic removePatch = new NtDynamic("{\"a\":null}");
    NtDynamic afterRemove = LibDynamic.merge(target, removePatch);
    Assert.assertFalse(LibDynamic.i(afterRemove, "a").has());
    Assert.assertEquals(2, (int) LibDynamic.i(afterRemove, "b").get());
  }

  @Test
  public void coverage_arr() {
    ArrayList<NtDynamic> items = new ArrayList<>();
    items.add(new NtDynamic("1"));
    items.add(new NtDynamic("2"));
    items.add(new NtDynamic("3"));
    NtDynamic result = LibDynamic.arr(new ArrayNtList<>(items));
    Assert.assertTrue(LibDynamic.is_array(result));
    Assert.assertEquals(3, (int) LibDynamic.size(result).get());
  }

  @Test
  public void ntjson_type_checks() {
    NtJson arr = new NtJson(new ArrayList<>());
    Assert.assertTrue(arr.is_array());
    Assert.assertFalse(arr.is_object());
    Assert.assertFalse(arr.is_string());
    Assert.assertFalse(arr.is_number());
    Assert.assertFalse(arr.is_bool());
    Assert.assertFalse(arr.is_null());

    NtJson nullJson = new NtJson(null);
    Assert.assertTrue(nullJson.is_null());

    NtDynamic dyn = new NtDynamic("{\"a\":[1,2,3]}");
    NtJson json = dyn.to_json();
    Assert.assertTrue(json.is_object());
    Assert.assertEquals(1, (int) json.size().get());
    NtList<String> keys = json.keys();
    Assert.assertEquals(1, keys.size());
  }

  @Test
  public void ntjson_to_list() {
    NtDynamic dyn = new NtDynamic("[1,2,3]");
    NtJson json = dyn.to_json();
    NtList<NtJson> list = json.to_list();
    Assert.assertEquals(3, list.size());

    // non-array returns empty list
    NtJson obj = new NtDynamic("{\"a\":1}").to_json();
    Assert.assertEquals(0, obj.to_list().size());
  }
}
