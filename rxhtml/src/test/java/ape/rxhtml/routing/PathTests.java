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
package ape.rxhtml.routing;

import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

public class PathTests {

  private Target mock(int v) {
    return new Target(v, null, null, null);
  }

  @Test
  public void root() {
    Path root = new Path("root");
    root.set(mock(123));
    Assert.assertEquals(123, ((Target) root.route(0, new String[] {}, new TreeMap<>())).status);
    Assert.assertEquals(112, root.memory());
  }

  @Test
  public void root_as_empty() {
    Path root = new Path("root");
    root.diveFixed("").set(mock(123));
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/"), new TreeMap<>())).status);
    Assert.assertEquals(216, root.memory());
  }

  @Test
  public void fixed() {
    Path root = new Path("root");
    Assert.assertTrue(root.diveFixed("xyz").set(mock(123)));
    Assert.assertTrue(root.diveFixed("a").set(mock(100)));
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz"), new TreeMap<>())).status);
    Assert.assertEquals(100, ((Target) root.route(0, Path.parsePath("/a"), new TreeMap<>())).status);
    Assert.assertEquals(392, root.memory());
  }

  @Test
  public void var_number_1() {
    Path root = new Path("root");
    Assert.assertTrue(root.diveFixed("xyz").newNumber("x").set(mock(123)));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz/42"), captured)).status);
    Assert.assertEquals("42", captured.get("x"));
    Assert.assertEquals(264, root.memory());
  }


  @Test
  public void var_number_1_fail() {
    Path root = new Path("root");
    root.diveFixed("xyz").newNumber("x").set(mock(123));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertNull(root.route(0, Path.parsePath("/xyz/joe"), captured));
    Assert.assertNull(captured.get("x"));
  }

  @Test
  public void var_number_2() {
    Path root = new Path("root");
    root.diveFixed("xyz").newNumber("x").newNumber("y").set(mock(123));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz/42/13"), captured)).status);
    Assert.assertEquals("42", captured.get("x"));
    Assert.assertEquals("13", captured.get("y"));
    Assert.assertEquals(306, root.memory());
  }

  @Test
  public void var_text() {
    Path root = new Path("root");
    Assert.assertTrue(root.diveFixed("xyz").newText("x").set(mock(123)));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz/joe"), captured)).status);
    Assert.assertEquals("joe", captured.get("x"));
  }

  @Test
  public void no_leading_slash() {
    Path root = new Path("root");
    Assert.assertTrue(root.diveFixed("xyz").newText("x").set(mock(123)));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("xyz/joe"), captured)).status);
    Assert.assertEquals("joe", captured.get("x"));
  }

  @Test
  public void eat_trailing_slash() {
    Path root = new Path("root");
    Assert.assertTrue(root.diveFixed("xyz").newText("x").set(mock(123)));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz/joe///"), captured)).status);
    Assert.assertEquals("joe", captured.get("x"));
  }

  @Test
  public void dupes() {
    Path root = new Path("root");
    Assert.assertTrue(root.diveFixed("xyz").set(mock(123)));
    Assert.assertFalse(root.diveFixed("xyz").set(mock(123)));
  }

  @Test
  public void var_backtracking_text() {
    Path root = new Path("root");
    root.diveFixed("xyz").newText("x").diveFixed("edit").set(mock(123));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertNull(root.route(0, Path.parsePath("/xyz/joe/nope"), captured));
    Assert.assertNull(captured.get("x"));
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz/joe/edit"), captured)).status);
    Assert.assertEquals("joe", captured.get("x"));
  }

  @Test
  public void var_backtracking_num() {
    Path root = new Path("root");
    root.diveFixed("xyz").newNumber("x").diveFixed("edit").set(mock(123));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertNull(root.route(0, Path.parsePath("/xyz/42/nope"), captured));
    Assert.assertNull(captured.get("x"));
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz/42/edit"), captured)).status);
    Assert.assertEquals("42", captured.get("x"));
  }

  @Test
  public void suffix() {
    Path root = new Path("root");
    root.diveFixed("xyz").setSuffix("v").set(mock(123));
    TreeMap<String, String> captured = new TreeMap<>();
    Assert.assertEquals(123, ((Target) root.route(0, Path.parsePath("/xyz/joe/edit"), captured)).status);
    Assert.assertEquals("joe/edit", captured.get("v"));
    Assert.assertEquals(264, root.memory());
  }
}
