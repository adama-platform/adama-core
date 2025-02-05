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
package ape.runtime.ops;

import ape.runtime.json.JsonStreamReader;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class TestReportBuilderTests {
  @Test
  @SuppressWarnings("unchecked")
  public void flow() {
    final var trb = new TestReportBuilder();
    trb.begin("xyz");
    trb.annotate("x", (HashMap<String, Object>) new JsonStreamReader("{}").readJavaTree());
    trb.end(new AssertionStats(50, 0));
    Assert.assertEquals(0, trb.getFailures());
    trb.begin("t2");
    trb.end(new AssertionStats(50, 4));
    Assert.assertEquals(4, trb.getFailures());
    Assert.assertEquals(
        "TEST[xyz] = 100.0%\n" + "TEST[t2] = 92.0% (HAS FAILURES)\n", trb.toString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void flow2() {
    final var trb = new TestReportBuilder();
    trb.begin("xyz");
    trb.end(new AssertionStats(0, 0));
    trb.begin("zx");
    trb.annotate(
        "dump", (HashMap<String, Object>) new JsonStreamReader("{\"x\":true}").readJavaTree());
    trb.end(new AssertionStats(0, 0));
    Assert.assertEquals(
        "TEST[xyz] HAS NO ASSERTS\n" + "TEST[zx]...DUMP:{\"x\":true}\n" + " HAS NO ASSERTS\n",
        trb.toString());
  }

  @Test
  public void logging() {
    final var trb = new TestReportBuilder();
    trb.begin("suite");
    trb.log("hi", 1, 2, 3, 4);
    trb.end(new AssertionStats(1, 0));
    System.out.println(trb.toString());
  }
}
