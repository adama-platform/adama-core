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
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class WebContextTests {
  @Test
  public void save() {
    WebContext context = new WebContext(new NtPrincipal("agent", "auth"), "origin", "ip");
    JsonStreamWriter writer = new JsonStreamWriter();
    context.writeAsObject(writer);
    Assert.assertEquals("{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"origin\":\"origin\",\"ip\":\"ip\"}", writer.toString());
  }

  @Test
  public void load() {
    WebContext wc = WebContext.readFromObject(new JsonStreamReader("{\"who\":{\"agent\":\"agent\",\"authority\":\"auth\"},\"origin\":\"origin\",\"ip\":\"ip\"}"));
    Assert.assertEquals("agent", wc.who.agent);
    Assert.assertEquals("auth", wc.who.authority);
    Assert.assertEquals("origin", wc.origin);
    Assert.assertEquals("ip", wc.ip);
  }
}
