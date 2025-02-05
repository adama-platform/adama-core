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
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class DMapTests {
  @Test
  @SuppressWarnings("unchecked")
  public void flow() {
    final var map = new DMap<Integer, DBoolean>();
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      final DMap<Integer, DBoolean>.Walk walk = map.begin();
      walk.next(42, DBoolean::new).show(true, delta.planField("" + 42));
      walk.next(1, DBoolean::new).show(false, delta.planField("" + 1));
      walk.end(delta);
      delta.end();
      Assert.assertEquals("{\"42\":true,\"1\":false}", stream.toString());
      Assert.assertEquals(200, map.__memory());
    }
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      delta.manifest();
      final DMap<Integer, DBoolean>.Walk walk = map.begin();
      walk.next(42, DBoolean::new).show(true, delta.planField("" + 42));
      walk.next(1, DBoolean::new).show(false, delta.planField("" + 1));
      walk.end(delta);
      delta.end();
      Assert.assertEquals("{}", stream.toString());
      Assert.assertEquals(200, map.__memory());
    }
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      delta.manifest();
      final DMap<Integer, DBoolean>.Walk walk = map.begin();
      walk.next(42, DBoolean::new).show(false, delta.planField("" + 42));
      walk.end(delta);
      delta.end();
      Assert.assertEquals("{\"42\":false,\"1\":null}", stream.toString());
      Assert.assertEquals(120, map.__memory());
    }
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      map.hide(writer);
      map.hide(writer);
      Assert.assertEquals("null", stream.toString());
      Assert.assertEquals(40, map.__memory());
    }
    map.clear();
  }
}
