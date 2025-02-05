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
package ape.web.client;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class SimpleHttpRequestBodyTests {
  @Test
  public void empty() throws Exception {
    Assert.assertEquals(0, SimpleHttpRequestBody.EMPTY.size());
    SimpleHttpRequestBody.EMPTY.read(null);
    SimpleHttpRequestBody.EMPTY.finished(true);
  }
  @Test
  public void bytearray() throws Exception {
    SimpleHttpRequestBody body = SimpleHttpRequestBody.WRAP("Hello World".getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals(11, body.size());
    byte[] chunk = new byte[4];
    Assert.assertEquals(4, body.read(chunk));
    Assert.assertEquals("Hell", new String(chunk, StandardCharsets.UTF_8));
    Assert.assertEquals(4, body.read(chunk));
    Assert.assertEquals("o Wo", new String(chunk, StandardCharsets.UTF_8));
    Assert.assertEquals(3, body.read(chunk));
    Assert.assertEquals("rld", new String(chunk, 0, 3, StandardCharsets.UTF_8));
    body.finished(true);
  }
}
