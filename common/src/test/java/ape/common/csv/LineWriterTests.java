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

public class LineWriterTests {
  @Test
  public void simple() {
    LineWriter lw = new LineWriter();
    lw.write(123);
    lw.write(3.14);
    lw.write("xyz");
    Assert.assertEquals("123,3.14,xyz", lw.toString());
  }

  @Test
  public void quote1() {
    LineWriter lw = new LineWriter();
    lw.write("Comma,Header");
    lw.write("Header2");
    lw.write("Header3");
    Assert.assertEquals("\"Comma,Header\",Header2,Header3", lw.toString());
  }

  @Test
  public void quote2() {
    LineWriter lw = new LineWriter();
    lw.write("Value with comma(,) and double quote(\")");
    lw.write("Value2");
    lw.write("Value3");
    Assert.assertEquals("\"Value with comma(,) and double quote(\"\")\",Value2,Value3", lw.toString());
  }

  @Test
  public void quote3() {
    LineWriter lw = new LineWriter();
    lw.write("xy\"abc\"\"");
    lw.write("x");
    lw.write("y");
    Assert.assertEquals("\"xy\"\"abc\"\"\"\"\",x,y", lw.toString());
  }

  @Test
  public void quote4() {
    LineWriter lw = new LineWriter();
    lw.write("new\nline");
    lw.write("a");
    lw.write("b");
    Assert.assertEquals("\"new\nline\",a,b", lw.toString());
  }
}
