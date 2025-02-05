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

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class URLTests {
  @Test
  public void spaces_20() throws Exception {
    Assert.assertEquals("hello%20world", URL.encode("hello world", false));
  }

  @Test
  public void path() throws Exception {
    Assert.assertEquals("/ok/now/we/go", URL.encode("/ok/now/we/go", true));
  }

  @Test
  public void keeper() throws Exception {
    Assert.assertEquals("abcdef._~blah/", URL.encode("abcdef._~blah/", true));
  }

  @Test
  public void slasher() throws Exception {
    Assert.assertEquals("//", URL.encode("//", true));
    Assert.assertEquals("%2F%2F", URL.encode("//", false));
  }

  @Test
  public void unicode() throws Exception {
    Assert.assertEquals("%E7%8C%BF%E3%82%82%E6%9C%A8%E3%81%8B%E3%82%89%E8%90%BD%E3%81%A1%E3%82%8B", URL.encode("猿も木から落ちる", false));
  }

  @Test
  public void surrogate() throws Exception {
    Assert.assertEquals("%F0%90%80%80", URL.encode("\ud800\udc00", false));
  }

  @Test
  public void plain() throws Exception {
    Assert.assertTrue(URL.plain('c', false));
    Assert.assertTrue(URL.plain('4', false));
    Assert.assertTrue(URL.plain('C', false));
    Assert.assertTrue(URL.plain('_', false));
    Assert.assertTrue(URL.plain('/', true));
    Assert.assertFalse(URL.plain('/', false));
    Assert.assertTrue(URL.plain('~', false));
    Assert.assertTrue(URL.plain('.', false));
    Assert.assertFalse(URL.plain('@', false));
  }

  @Test
  public void params() {
    Assert.assertEquals("", URL.parameters(null));
    Assert.assertEquals("", URL.parameters(new HashMap<>()));
    Assert.assertEquals("?xyz=abc", URL.parameters(Collections.singletonMap("xyz", "abc")));
    TreeMap<String, String> multi = new TreeMap<>();
    multi.put("a", "b");
    multi.put("c", "d");
    multi.put("e", "f");
    Assert.assertEquals("?a=b&c=d&e=f", URL.parameters(multi));
  }
}
