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
package ape.runtime.reactives;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockMessage;
import ape.runtime.mocks.MockRxParent;
import org.junit.Assert;
import org.junit.Test;

public class RxHolderTests {
  @Test
  public void flow() {
    RxHolder<MockMessage> holder = new RxHolder<>(null, () -> new MockMessage());
    JsonStreamWriter writer = new JsonStreamWriter();
    holder.__commit("h", writer, writer);
    holder.__revert();
    holder.__dump(writer);
    holder.__insert(new JsonStreamReader("{}"));
    Assert.assertEquals("{\"x\":42,\"y\":13}", writer.toString());

    MockMessage next = new MockMessage();
    next.x = 100;
    next.y = 200;

    writer = new JsonStreamWriter();
    holder.set(next);
    holder.__commit("h", writer, writer);
    holder.__dump(writer);
    Assert.assertEquals("\"h\":\"h\":{\"x\":100,\"y\":200},{\"x\":42,\"y\":13},{\"x\":100,\"y\":200}", writer.toString());
    holder.__patch(new JsonStreamReader("{\"x\":123}"));
    writer = new JsonStreamWriter();
    holder.__commit("h2", writer, writer);
    Assert.assertEquals("\"h2\":\"h2\":{\"x\":123,\"y\":200},{\"x\":100,\"y\":200}", writer.toString());
  }
}
