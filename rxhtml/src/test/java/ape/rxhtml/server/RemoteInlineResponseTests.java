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

public class RemoteInlineResponseTests {
  @Test
  public void trivial_body() {
    RemoteInlineResponse response = RemoteInlineResponse.body(200, "body", "title", "identity");
    Assert.assertEquals(200, response.status);
    Assert.assertEquals("body", response.body);
    Assert.assertEquals("title", response.title);
    Assert.assertEquals("identity", response.identity);
    Assert.assertNull(response.contentType);
    Assert.assertNull(response.redirect);
  }

  @Test
  public void trivial_json() {
    RemoteInlineResponse response = RemoteInlineResponse.json(403, "body", "identity");
    Assert.assertEquals(403, response.status);
    Assert.assertEquals("body", response.body);
    Assert.assertNull(response.title);
    Assert.assertEquals("identity", response.identity);
    Assert.assertEquals("application/json", response.contentType);
    Assert.assertNull(response.redirect);
  }

  @Test
  public void trivial_xml() {
    RemoteInlineResponse response = RemoteInlineResponse.xml(200, "body", "identity");
    Assert.assertEquals("body", response.body);
    Assert.assertNull(response.title);
    Assert.assertEquals("identity", response.identity);
    Assert.assertEquals("text/xml", response.contentType);
    Assert.assertNull(response.redirect);
  }

  @Test
  public void trivial_redirect() {
    RemoteInlineResponse response = RemoteInlineResponse.redirect("location", "identity");
    Assert.assertNull(response.body);
    Assert.assertNull(response.title);
    Assert.assertEquals("identity", response.identity);
    Assert.assertNull(response.contentType);
    Assert.assertEquals("location", response.redirect);
  }
}
