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
package ape.runtime.async;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import org.junit.Assert;
import org.junit.Test;

public class TimeoutTests {
  @Test
  public void write() {
    Timeout x = new Timeout(123, 4.2);
    JsonStreamWriter writer = new JsonStreamWriter();
    x.write(writer);
    Assert.assertEquals("{\"timestamp\":\"123\",\"timeout\":4.2}", writer.toString());
  }

  @Test
  public void readFrom() {
    Timeout x = Timeout.readFrom(new JsonStreamReader("{\"timestamp\":\"123\",\"timeout\":4.2,\"junk\":true}"));
    Assert.assertEquals(123, x.timestamp);
    Assert.assertEquals(4.2, x.timeoutSeconds, 0.1);
  }

  @Test
  public void junk() {
    Timeout x = Timeout.readFrom(new JsonStreamReader("\"timeout\""));
    Assert.assertNull(x);
  }
}
