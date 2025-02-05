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
package ape.common.csv;

import org.junit.Assert;
import org.junit.Test;

public class LineReaderTests {
  @Test
  public void simple() {
    String[] line = LineReader.parse("a,b,c");
    Assert.assertEquals(3, line.length);
    Assert.assertEquals("a", line[0]);
    Assert.assertEquals("b", line[1]);
    Assert.assertEquals("c", line[2]);
  }
  @Test
  public void quoted1() {
    String[] line = LineReader.parse("\"Comma,Header\",Header2,Header3");
    Assert.assertEquals(3, line.length);
    Assert.assertEquals("Comma,Header", line[0]);
    Assert.assertEquals("Header2", line[1]);
    Assert.assertEquals("Header3", line[2]);
  }

  @Test
  public void quoted2() {
    String[] line = LineReader.parse("Xyz,\"1,2,3\",\"\"\"Hi\"\"\"");
    Assert.assertEquals(3, line.length);
    Assert.assertEquals("Xyz", line[0]);
    Assert.assertEquals("1,2,3", line[1]);
    Assert.assertEquals("\"Hi\"", line[2]);
  }

  @Test
  public void quoted3() {
    String[] line = LineReader.parse("\"xy\"\"abc\"\"\"\"\",x,y");
    Assert.assertEquals(3, line.length);
    Assert.assertEquals("xy\"abc\"\"", line[0]);
    Assert.assertEquals("x", line[1]);
    Assert.assertEquals("y", line[2]);
  }

  @Test
  public void bad_quote() {
    String[] line = LineReader.parse("\"x\"1\",y");
    Assert.assertEquals(2, line.length);
    Assert.assertEquals("x", line[0]);
    Assert.assertEquals(",y", line[1]);
  }

  @Test
  public void solo() {
    String[] line = LineReader.parse("x");
    Assert.assertEquals(1, line.length);
    Assert.assertEquals("x", line[0]);
  }
}
