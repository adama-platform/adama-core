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
package ape.runtime.sys.web;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.web.partial.WebPartial;
import ape.runtime.sys.web.partial.WebPutPartial;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

public class WebPutTests {
  @Test
  public void flow() {
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    TreeMap<String, String> headers = new TreeMap<>();
    headers.put("x", "abc");
    WebPut put = new WebPut(context, "/uri", headers, new NtDynamic("{\"p\":123}"), "{}");
    JsonStreamWriter writer = new JsonStreamWriter();
    put.injectWrite(writer);
    Assert.assertEquals("\"put\":{\"uri\":\"/uri\",\"headers\":{\"x\":\"abc\"},\"parameters\":{\"p\":123},\"bodyJson\":{}}", writer.toString());
    JsonStreamReader reader = new JsonStreamReader(writer.toString());
    Assert.assertEquals("put", reader.fieldName());
    WebPut clone = (WebPut) WebPutPartial.read(reader).convert(context);
    Assert.assertEquals("/uri", clone.uri);
    Assert.assertEquals("{\"p\":123}", clone.parameters.json);
    Assert.assertEquals("{}", clone.bodyJson);
    Assert.assertEquals("abc", clone.headers.storage.get("x"));
    Assert.assertNull(WebPutPartial.read(new JsonStreamReader("{}")).convert(context));
  }

  @Test
  public void route() {
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    JsonStreamReader reader = new JsonStreamReader("{\"put\":{\"uri\":\"/uri\",\"headers\":{\"x\":\"abc\"},\"parameters\":{\"p\":123},\"bodyJson\":{},\"junk\":\"rat\"}}");
    WebPut put = (WebPut) WebPartial.read(reader).convert(context);
    Assert.assertEquals("/uri", put.uri);
    Assert.assertEquals("{\"p\":123}", put.parameters.json);
    Assert.assertEquals("{}", put.bodyJson);
    Assert.assertEquals("abc", put.headers.storage.get("x"));
  }
}
