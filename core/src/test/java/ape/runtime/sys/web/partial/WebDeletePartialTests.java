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
package ape.runtime.sys.web.partial;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.web.WebContext;
import ape.runtime.sys.web.WebDelete;
import org.junit.Assert;
import org.junit.Test;

public class WebDeletePartialTests {
  @Test
  public void nulls() {
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{}")).convert(context));
    Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{\"junk\":[]}")).convert(context));
        Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":{}}")).convert(context));
    Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":\"cake\",\"parameters\":{}}")).convert(context));
    Assert.assertNotNull(WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":{},\"parameters\":{}}")).convert(context));
  }

  @Test
  public void happy() {
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    WebDelete put = (WebDelete) WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":{\"x\":\"y\"},\"parameters\":{},\"bodyJson\":\"body\"}")).convert(context);
    Assert.assertNotNull(put);
    Assert.assertEquals("uri", put.uri);
    Assert.assertEquals("y", put.headers.get("x"));
    Assert.assertEquals("{}", put.parameters.json);
  }

}
