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
package ape.runtime;

import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class JsonConstructionTests {

  @Test
  public void listMaybeStrSetup() throws Exception {
    new RealDocumentSetup("record S { maybe<string> item; } table<S> s; public formula j = (iterate s).item.join(\", \");");
  }

  @Test
  public void sendMessageBasic() throws Exception {
    final var setup =
        new RealDocumentSetup(
            "@connected { return true; } message M { int x; double y; string z; bool b; } int xx; double yy; string zz; bool bb; channel foo(M m) { xx = m.x; yy = m.y; zz = m.z; bb = m.b; }");
    setup.document.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), new RealDocumentSetup.AssertInt(2));
    setup.document.send(
        ContextSupport.WRAP(NtPrincipal.NO_ONE),
        null,
        "marker",
        "foo",
        "{\"x\":123,\"y\":3.14,\"z\":\"w00t\",\"b\":true}",
        new RealDocumentSetup.AssertInt(4));
    Assert.assertEquals(
        "{\"__snapshot\":\"space/0\",\"xx\":123,\"yy\":3.14,\"zz\":\"w00t\",\"bb\":true,\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":4,\"__entropy\":\"-4186135525725789677\",\"__auto_future_id\":0,\"__connection_id\":1,\"__message_id\":0,\"__time\":\"0\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__dedupe\":{\"?/?/marker\":\"0\"},\"__clients\":{\"0\":{\"agent\":\"?\",\"authority\":\"?\"}},\"__webqueue\":{},\"__replication\":{}}",
        setup.document.json());
  }

  @Test
  public void sendMessageFixInvalid() throws Exception {
    final var setup =
        new RealDocumentSetup(
            "@connected { return true; } message M { int x; double y; string z; bool b; } int xx; double yy; string zz; bool bb; channel foo(M m) { xx = m.x; yy = m.y; zz = m.z; bb = m.b; }");
    setup.document.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), new RealDocumentSetup.AssertInt(2));
    setup.document.send(
        ContextSupport.WRAP(NtPrincipal.NO_ONE),
        null,
        "marker1",
        "foo",
        "{\"x\":\"\",\"y\":null,\"z\":\"w00t\",\"b\":true}",
        new RealDocumentSetup.AssertInt(4));
  }

  @Test
  public void sendMessageInvalidCrash() throws Exception {
    final var setup =
        new RealDocumentSetup(
            "@connected { return true; } message M { int x; double y; string z; bool b; } int xx; double yy; string zz; bool bb; channel foo(M m) { xx = m.x; yy = m.y; zz = m.z; bb = m.b; }");
    setup.document.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), new RealDocumentSetup.AssertInt(2));
    setup.document.send(
        ContextSupport.WRAP(NtPrincipal.NO_ONE),
        null,
        "marker1",
        "foo",
        "{\"x\":true,\"y\":null,\"z\":\"w00t\",\"b\":true}",
        new RealDocumentSetup.AssertFailure(145627));
  }
}
