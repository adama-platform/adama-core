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
package ape.runtime.reactives;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;

public class RxDateTimeTests {
  //
  private static final NtDateTime A = new NtDateTime(ZonedDateTime.parse("2021-04-24T17:57:19.802528800-05:00[America/Chicago]"));
  private static final NtDateTime B = new NtDateTime(ZonedDateTime.parse("2023-04-24T17:57:19.802528800-05:00[America/Chicago]"));

  @Test
  public void memory() {
    Assert.assertEquals(64, A.memory());
    Assert.assertEquals(64, B.memory());
    RxDateTime a = new RxDateTime(null, A);
    RxDateTime b = new RxDateTime(null, B);
    Assert.assertEquals(184, a.__memory());
    Assert.assertEquals(184, b.__memory());
    a.set(A);
    Assert.assertEquals(A, a.get());
  }

  @Test
  public void idx_value() {
    RxDateTime a = new RxDateTime(null, A);
    Assert.assertEquals(26988417, a.getIndexValue());
  }

  @Test
  public void compare() {
    RxDateTime a = new RxDateTime(null, A);
    RxDateTime b = new RxDateTime(null, B);
    Assert.assertEquals(-1, a.compareTo(b));
    Assert.assertEquals(1, b.compareTo(a));
  }

  @Test
  public void commit_no_change() {
    JsonStreamWriter redo = new JsonStreamWriter();
    JsonStreamWriter undo = new JsonStreamWriter();

    RxDateTime a = new RxDateTime(null, A);
    a.__commit("x", redo, undo);
    Assert.assertEquals("", redo.toString());
    Assert.assertEquals("", undo.toString());
  }

  @Test
  public void commit_change() {
    JsonStreamWriter redo = new JsonStreamWriter();
    JsonStreamWriter undo = new JsonStreamWriter();

    RxDateTime a = new RxDateTime(null, A);
    a.set(B);
    a.__commit("x", redo, undo);
    Assert.assertEquals(
        "\"x\":\"2023-04-24T17:57:19.802528800-05:00[America/Chicago]\"",
        redo.toString());
    Assert.assertEquals(
        "\"x\":\"2021-04-24T17:57:19.802528800-05:00[America/Chicago]\"",
        undo.toString());
  }

  @Test
  public void revert() {
    JsonStreamWriter redo = new JsonStreamWriter();
    JsonStreamWriter undo = new JsonStreamWriter();
    RxDateTime a = new RxDateTime(null, A);
    a.set(B);
    a.__revert();
    a.__commit("x", redo, undo);
    Assert.assertEquals("", redo.toString());
    Assert.assertEquals("", undo.toString());
  }

  @Test
  public void dump() {
    JsonStreamWriter c = new JsonStreamWriter();
    RxDateTime a = new RxDateTime(null, A);
    a.__dump(c);
    Assert.assertEquals(
        "\"2021-04-24T17:57:19.802528800-05:00[America/Chicago]\"",
        c.toString());
  }

  @Test
  public void insert() {
    JsonStreamWriter c = new JsonStreamWriter();
    RxDateTime a = new RxDateTime(null, B);
    a.__dump(c);
    a.__insert(
        new JsonStreamReader(
            "\"2027-04-24T17:57:19.802528800-05:00[America/Chicago]\""));
    a.__dump(c);
    Assert.assertEquals(
        "\"2023-04-24T17:57:19.802528800-05:00[America/Chicago]\"\"2027-04-24T17:57:19.802528800-05:00[America/Chicago]\"",
        c.toString());
  }

  @Test
  public void patch() {
    JsonStreamWriter c = new JsonStreamWriter();
    RxDateTime a = new RxDateTime(null, B);
    a.__patch(
        new JsonStreamReader(
            "\"2027-04-24T17:57:19.802528800-05:00[America/Chicago]\""));
    a.__dump(c);
    Assert.assertEquals(
        "\"2027-04-24T17:57:19.802528800-05:00[America/Chicago]\"",
        c.toString());
  }
}
