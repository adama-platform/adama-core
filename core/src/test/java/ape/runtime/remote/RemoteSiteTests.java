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
package ape.runtime.remote;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class RemoteSiteTests {
  @Test
  public void flow() {
    RemoteSite site = new RemoteSite(42, new RemoteInvocation("service", "method", NtPrincipal.NO_ONE, "{\"new\":\"hope\"}"));
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      site.dump(writer);
      Assert.assertEquals("{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"new\":\"hope\"}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}", writer.toString());
      RemoteSite copy = new RemoteSite(42, new JsonStreamReader(writer.toString()));
      Assert.assertEquals(site.invocation(), copy.invocation());
      Assert.assertEquals(site.invocation(), copy.invocation());
    }
    site.deliver(new RemoteResult("{\"success\":true}", null, null));
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      site.dump(writer);
      Assert.assertEquals("{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"new\":\"hope\"}},\"result\":{\"result\":{\"success\":true},\"failure\":null,\"failure_code\":null}}", writer.toString());
      RemoteSite copy = new RemoteSite(42, new JsonStreamReader(writer.toString()));
      Assert.assertEquals(site.invocation(), copy.invocation());
    }
    site.revert();
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      site.dump(writer);
      Assert.assertEquals("{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"new\":\"hope\"}},\"result\":{\"result\":null,\"failure\":null,\"failure_code\":null}}", writer.toString());
      RemoteSite copy = new RemoteSite(42, new JsonStreamReader(writer.toString()));
      Assert.assertEquals(site.invocation(), copy.invocation());
      Assert.assertEquals(site.invocation(), copy.invocation());
    }
    site.deliver(new RemoteResult("{\"success\":true}", null, null));
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      site.dump(writer);
      Assert.assertEquals("{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"new\":\"hope\"}},\"result\":{\"result\":{\"success\":true},\"failure\":null,\"failure_code\":null}}", writer.toString());
      RemoteSite copy = new RemoteSite(42, new JsonStreamReader(writer.toString()));
      Assert.assertEquals(site.invocation(), copy.invocation());
    }
    site.commit();
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      site.dump(writer);
      Assert.assertEquals("{\"invoke\":{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"parameter\":{\"new\":\"hope\"}},\"result\":{\"result\":{\"success\":true},\"failure\":null,\"failure_code\":null}}", writer.toString());
      RemoteSite copy = new RemoteSite(42, new JsonStreamReader(writer.toString()));
      Assert.assertEquals(site.invocation(), copy.invocation());
      Assert.assertEquals(site, copy);
      Assert.assertEquals(site.hashCode(), copy.hashCode());
      Assert.assertFalse(site.equals(null));
      Assert.assertFalse(site.equals("X"));
    }
  }

  @Test
  public void patch_junk() {
    RemoteSite site = new RemoteSite(42, new RemoteInvocation("service", "method", NtPrincipal.NO_ONE, "{\"new\":\"hope\"}"));
    site.patch(new JsonStreamReader("{\"junk\":123}"));
    site.patch(new JsonStreamReader("123"));
  }
}
