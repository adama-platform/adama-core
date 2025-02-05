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
package ape.runtime.delta;

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.json.PrivateLazyDeltaWriter;
import ape.runtime.mocks.MockRxParent;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.reactives.RxInt64;
import ape.runtime.remote.replication.RxReplicationStatus;
import org.junit.Assert;
import org.junit.Test;

public class DReplicationStatusTests {
  @Test
  public void coverage() {
    DReplicationStatus delta = new DReplicationStatus();
    RxReplicationStatus status = new RxReplicationStatus(new MockRxParent(), new RxInt64(null, 1000), "service", "method");
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      delta.show(status, writer);
      Assert.assertEquals(78, delta.__memory());
      delta.hide(writer);
      delta.hide(writer);
      delta.hide(writer);
      Assert.assertEquals(40, delta.__memory());
      delta.show(status, writer);
      delta.show(status, writer);
      delta.show(status, writer);
      delta.show(status, writer);
      delta.clear();
      Assert.assertEquals(40, delta.__memory());
      delta.show(status, writer);
      Assert.assertEquals(78, delta.__memory());
      Assert.assertEquals("\"Nothing;null;null;0\"null\"Nothing;null;null;0\"\"Nothing;null;null;0\"", stream.toString());
    }
  }
}
