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
package ape.web.client.pool;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class WebEndpointTests {
  @Test
  public void coverage() {
    WebEndpoint we = new WebEndpoint(URI.create("https://www.adama-platform.com"));
    Assert.assertTrue(we.secure);
    Assert.assertEquals(443, we.port);
    Assert.assertEquals("www.adama-platform.com", we.host);
    Assert.assertFalse(we.equals(null));
    Assert.assertFalse(we.equals("www"));
    Assert.assertTrue(we.equals(we));
    Assert.assertTrue(we.equals(new WebEndpoint(URI.create("https://www.adama-platform.com"))));
    Assert.assertFalse(we.equals(new WebEndpoint(URI.create("https://www.adama-platformx.com"))));
    Assert.assertFalse(we.equals(new WebEndpoint(URI.create("http://www.adama-platform.com"))));
    Assert.assertFalse(we.equals(new WebEndpoint(URI.create("https://www.adama-platform.com:444"))));
    we.hashCode();
  }

  @Test
  public void port_override() {
    WebEndpoint we = new WebEndpoint(URI.create("https://www.adama-platform.com:77"));
    Assert.assertEquals(77, we.port);
  }
}
