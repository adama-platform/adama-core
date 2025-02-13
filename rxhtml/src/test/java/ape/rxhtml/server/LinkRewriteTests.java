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
package ape.rxhtml.server;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class LinkRewriteTests {
  @Test
  public void test_simple() {
    LinkRewrite r = new LinkRewrite("/xyz");
    TreeMap<String, List<String>> map = new TreeMap<>();
    Assert.assertEquals(2, r.score(map));
    Assert.assertEquals("/xyz", r.eval(map));
  }

  @Test
  public void test_root() {
    LinkRewrite r = new LinkRewrite("/");
    TreeMap<String, List<String>> map = new TreeMap<>();
    Assert.assertEquals(2, r.score(map));
    Assert.assertEquals("/", r.eval(map));
  }

  @Test
  public void rewrite_happy() {
    LinkRewrite r = new LinkRewrite("/$x/$y");
    TreeMap<String, List<String>> map = new TreeMap<>();
    map.put("x", Collections.singletonList("123"));
    map.put("y", Collections.singletonList("42"));
    Assert.assertEquals(3, r.score(map));
    Assert.assertEquals("/123/42", r.eval(map));
  }

  @Test
  public void rewrite_incomplete() {
    LinkRewrite r = new LinkRewrite("/$x/$z");
    TreeMap<String, List<String>> map = new TreeMap<>();
    Assert.assertEquals(-19999, r.score(map));
  }
}
