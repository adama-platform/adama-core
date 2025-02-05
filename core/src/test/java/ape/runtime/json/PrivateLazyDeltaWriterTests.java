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
package ape.runtime.json;

import ape.runtime.natives.NtComplex;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class PrivateLazyDeltaWriterTests {

  @Test
  public void bunchAdoAboutNothing() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    lazy.planObject().planField("x").planArray();
    Assert.assertEquals("", writer.toString());
  }

  @Test
  public void manifestAbove() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    lazy.planObject().planField("x").planArray().manifest();
    Assert.assertEquals("{\"x\":[", writer.toString());
  }

  @Test
  public void manifestX() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    lazy.planObject().planField("x").planArray().writeFastString("x");
    Assert.assertEquals("{\"x\":[\"x\"", writer.toString());
  }

  @Test
  public void donothing() {
    PrivateLazyDeltaWriter.DO_NOTHING.run();
  }

  @Test
  public void complex() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    PrivateLazyDeltaWriter obj = lazy.planObject();
    obj.planField("x").writeNtComplex(new NtComplex(1, 2));
    obj.end();
    Assert.assertEquals("{\"x\":{\"r\":1.0,\"i\":2.0}}", writer.toString());
  }

  @Test
  public void force() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    PrivateLazyDeltaWriter obj = lazy.planObject();
    obj.planField("x").force();
    obj.end();
    Assert.assertEquals("{\"x\":}", writer.toString());
  }

  @Test
  public void inject() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    PrivateLazyDeltaWriter obj = lazy.planObject();
    obj.planField("x").injectJson(">INJECT<");
    obj.end();
    Assert.assertEquals("{\"x\":>INJECT<}", writer.toString());
  }

  @Test
  public void simpleObject() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    PrivateLazyDeltaWriter obj = lazy.planObject();
    obj.planField("x").writeInt(123);
    obj.planField("y").writeNull();
    obj.planField(42).writeString("hi");
    obj.planField(13).writeDouble(13.271);
    obj.planField("z").writeBool(true);
    obj.end();
    Assert.assertEquals(
        "{\"x\":123,\"y\":null,\"42\":\"hi\",\"13\":13.271,\"z\":true}", writer.toString());
  }

  @Test
  public void simpleArray() {
    JsonStreamWriter writer = new JsonStreamWriter();
    PrivateLazyDeltaWriter lazy = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, writer, null, 0);
    PrivateLazyDeltaWriter obj = lazy.planArray();
    obj.writeInt(123);
    obj.writeNull();
    obj.writeString("hi");
    obj.writeDouble(13.271);
    obj.writeBool(true);
    obj.end();
    Assert.assertEquals("[123,null,\"hi\",13.271,true]", writer.toString());
  }
}
