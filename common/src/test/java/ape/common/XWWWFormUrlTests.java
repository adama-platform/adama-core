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

public class XWWWFormUrlTests {
  @Test
  public void simple() {
    String result = XWWWFormUrl.encode(Json.parseJsonObject("{}"));
    Assert.assertEquals("", result);
  }

  @Test
  public void one() {
    String result = XWWWFormUrl.encode(Json.parseJsonObject("{\"x\":123}"));
    Assert.assertEquals("x=123", result);
  }

  @Test
  public void two() {
    String result = XWWWFormUrl.encode(Json.parseJsonObject("{\"x\":123,\"y\":\"xyz\"}"));
    Assert.assertEquals("x=123&y=xyz", result);
  }

  @Test
  public void three() {
    String result = XWWWFormUrl.encode(Json.parseJsonObject("{\"x\":4.2,\"y\":\"xyz\",\"z\":true}"));
    Assert.assertEquals("x=4.2&y=xyz&z=true", result);
  }

  @Test
  public void compound() {
    String result = XWWWFormUrl.encode(Json.parseJsonObject("{\"o\":{\"x\":4.2,\"y\":\"xyz\",\"z\":true}}"));
    Assert.assertEquals("o.x=4.2&o.y=xyz&o.z=true", result);
  }
}
