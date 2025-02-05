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

public class RemoteInvocationTests {
  @Test
  public void flow() {
    RemoteInvocation invocation = new RemoteInvocation("service", "method", new NtPrincipal("a", "b"), "{\"x\":1000}");
    JsonStreamWriter writer = new JsonStreamWriter();
    invocation.write(writer);
    Assert.assertEquals("{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"a\",\"authority\":\"b\"},\"parameter\":{\"x\":1000}}", writer.toString());
    RemoteInvocation copy = new RemoteInvocation(new JsonStreamReader(writer.toString()));
    Assert.assertEquals("service", copy.service);
    Assert.assertEquals("method", copy.method);
    Assert.assertEquals("a", copy.who.agent);
    Assert.assertEquals("b", copy.who.authority);
    Assert.assertEquals("{\"x\":1000}", copy.parameter);
    Assert.assertEquals(invocation.hashCode(), copy.hashCode());
    Assert.assertTrue(invocation.equals(copy));
    Assert.assertFalse(invocation.equals(null));
    Assert.assertFalse(invocation.equals("X"));
    Assert.assertEquals(0, invocation.compareTo(copy));

    Assert.assertEquals(10, invocation.compareTo(new RemoteInvocation("service", "method", new NtPrincipal("a", "b"), "")));
    Assert.assertEquals(1, invocation.compareTo(new RemoteInvocation("service", "method", new NtPrincipal("", ""), "")));
    Assert.assertEquals(6, invocation.compareTo(new RemoteInvocation("service", "", new NtPrincipal("", ""), "")));
    Assert.assertEquals(7, invocation.compareTo(new RemoteInvocation("", "", new NtPrincipal("", ""), "")));
  }

  @Test
  public void redundant() {
    RemoteInvocation copy = new RemoteInvocation(new JsonStreamReader("{\"service\":\"service\",\"method\":\"method\",\"who\":{\"agent\":\"a\",\"authority\":\"b\"},\"parameter\":{\"x\":1000},\"junk\":123}"));
    Assert.assertEquals("service", copy.service);
    Assert.assertEquals("method", copy.method);
    Assert.assertEquals("a", copy.who.agent);
    Assert.assertEquals("b", copy.who.authority);
    Assert.assertEquals("{\"x\":1000}", copy.parameter);
  }
}
