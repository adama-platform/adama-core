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
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.reactives.RxInt32;
import ape.runtime.text.RxText;
import org.junit.Assert;
import org.junit.Test;

public class DTextTests {
  @Test
  public void flow() {
    RxText text = new RxText(null, new RxInt32(null, 0));
    DText dt = new DText();
    final var stream = new JsonStreamWriter();
    final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
    dt.show(text, writer);
    text.set("xyz");
    dt.show(text, writer);
    text.set("/* adama */");
    dt.show(text, writer);
    text.append(0, new NtDynamic("{\"clientID\":\"dzg02a\",\"changes\":[11,[0,\"x\"]]}"));
    dt.show(text, writer);
    dt.hide(writer);
    dt.show(text, writer);
    Assert.assertEquals("{\"$g\":0,\"$i\":\"\",\"$s\":0}{\"$g\":1,\"$i\":\"xyz\",\"$s\":0}{\"$g\":2,\"$i\":\"/* adama */\",\"$s\":0}{\"0\":\"{\\\"clientID\\\":\\\"dzg02a\\\",\\\"changes\\\":[11,[0,\\\"x\\\"]]}\",\"$s\":1}null{\"$g\":2,\"$i\":\"/* adama */x\",\"$s\":1}", stream.toString());
    dt.clear();
    Assert.assertEquals(64, dt.__memory());
  }

  @Test
  public void changes() {
    RxText text = new RxText(null, new RxInt32(null, 0));
    DText dt = new DText();
    final var stream = new JsonStreamWriter();
    final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
    text.set("/* adama */");
    dt.show(text, writer);
    text.append(0, new NtDynamic("{\"clientID\":\"dzg02a\",\"changes\":[11,[0,\"x\"]]}"));
    {
      JsonStreamWriter redo = new JsonStreamWriter();
      JsonStreamWriter undo = new JsonStreamWriter();
      text.__commit("x", redo, undo);
    }
    dt.show(text, writer);
    Assert.assertEquals("{\"$g\":1,\"$i\":\"/* adama */\",\"$s\":0}{\"0\":\"{\\\"clientID\\\":\\\"dzg02a\\\",\\\"changes\\\":[11,[0,\\\"x\\\"]]}\",\"$s\":1}", stream.toString());
  }
}
