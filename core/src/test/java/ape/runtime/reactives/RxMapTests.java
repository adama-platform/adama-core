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

import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockRxChild;
import ape.runtime.mocks.MockRxParent;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class RxMapTests {
  @Test
  public void memory() {
    final var m = map();
    Assert.assertEquals(1192, m.__memory());
    m.getOrCreate(42).set(52);
    Assert.assertEquals(1368, m.__memory());
    m.getOrCreate(123).set(52);
    Assert.assertEquals(1544, m.__memory());
  }

  @Test
  public void memoryStr() {
    final var m = new RxMap<String, RxInt32>(
        new MockRxParent(),
        new RxMap.StringCodec<RxInt32>() {
          @Override
          public RxInt32 make(RxParent maker) {
            return new RxInt32(maker, 40);
          }
        });
    Assert.assertEquals(1192, m.__memory());
    m.getOrCreate("42").set(52);
    Assert.assertEquals(1372, m.__memory());
    m.getOrCreate("50").set(52);
    Assert.assertEquals(1552, m.__memory());
  }

  @Test
  public void memoryPrincipal() {
    final var m = new RxMap<NtPrincipal, RxPrincipal>(
        new MockRxParent(),
        new RxMap.PrincipalCodec<RxPrincipal>() {
          @Override
          public RxPrincipal make(RxParent maker) {
            return new RxPrincipal(maker, new NtPrincipal("a", "a"));
          }
        });
    Assert.assertEquals(1192, m.__memory());
    m.getOrCreate(new NtPrincipal("a", "b")).set(new NtPrincipal("b", "c"));
    Assert.assertEquals(1384, m.__memory());
    JsonStreamWriter forward = new JsonStreamWriter();
    JsonStreamWriter reverse = new JsonStreamWriter();
    m.__commit("name", forward, reverse);
    Assert.assertEquals("\"name\":{\"a/b\":{\"agent\":\"b\",\"authority\":\"c\"}}", forward.toString());
    Assert.assertEquals("\"name\":{\"a/b\":null}", reverse.toString());
  }

  private RxMap<Integer, RxInt32> map() {
    return map(new MockRxParent());
  }

  private RxMap<Integer, RxInt32> map(RxParent parent) {
    return new RxMap<Integer, RxInt32>(
        parent,
        new RxMap.IntegerCodec<RxInt32>() {
          @Override
          public RxInt32 make(RxParent maker) {
            return new RxInt32(maker, 0);
          }
        });
  }

  @Test
  public void cost_report() {
    MockRxParent parent = new MockRxParent();
    map(parent).__cost(423);
    Assert.assertEquals(423, parent.cost);
  }

  @Test
  public void dump_empty() {
    final var m = map();
    JsonStreamWriter writer = new JsonStreamWriter();
    m.__dump(writer);
    Assert.assertEquals("{}", writer.toString());
  }

  @Test
  public void alive_with_parent() {
    MockRxParent parent = new MockRxParent();
    final var m = map(parent);
    Assert.assertTrue(m.__isAlive());
    parent.alive = false;
    Assert.assertFalse(m.__isAlive());
  }

  @Test
  public void alive_without_parent() {
    final var m = map(null);
    Assert.assertTrue(m.__isAlive());
  }

  @Test
  public void killable_proxy() {
    final var m = new RxMap<Integer, RxMap<Integer, RxInt32>>(new MockRxParent(), new RxMap.IntegerCodec<RxMap<Integer, RxInt32>>() {
          @Override
          public RxMap<Integer, RxInt32> make(RxParent maker) {
            return map(maker);
          }
        });
    m.getOrCreate(42).getOrCreate(100).set(10000);
    {
      JsonStreamWriter redo = new JsonStreamWriter();
      JsonStreamWriter undo = new JsonStreamWriter();
      m.__commit("x", redo, undo);
      Assert.assertEquals("\"x\":{\"42\":{\"100\":10000}}", redo.toString());
    }
    m.__kill();
    m.remove(42);
    {
      JsonStreamWriter redo = new JsonStreamWriter();
      JsonStreamWriter undo = new JsonStreamWriter();
      m.__commit("x", redo, undo);
      Assert.assertEquals("\"x\":{\"42\":null}", redo.toString());
    }
  }

  @Test
  public void dump_singular() {
    final var m = map();
    m.getOrCreate(42).set(52);
    JsonStreamWriter writer = new JsonStreamWriter();
    m.__dump(writer);
    Assert.assertEquals("{\"42\":52}", writer.toString());
  }

  @Test
  public void dump_after_revert() {
    final var m = map();
    m.getOrCreate(42).set(52);
    m.__revert();
    JsonStreamWriter writer = new JsonStreamWriter();
    m.__dump(writer);
    Assert.assertEquals("{}", writer.toString());
  }

  @Test
  public void dump_after_create_then_remove() {
    final var m = map();
    m.getOrCreate(42).set(52);
    m.remove(42);
    JsonStreamWriter writer = new JsonStreamWriter();
    m.__dump(writer);
    Assert.assertEquals("{}", writer.toString());
  }

  @Test
  public void map_min() {
    final var m = map();
    m.getOrCreate(1).set(10);
    m.getOrCreate(2).set(15);
    m.getOrCreate(3).set(20);
    m.getOrCreate(4).set(30);
    Assert.assertEquals(1, (int) m.min().get().key);
    Assert.assertEquals(10, (int) m.min().get().value.get());
  }

  @Test
  public void map_max() {
    final var m = map();
    m.getOrCreate(1).set(10);
    m.getOrCreate(2).set(15);
    m.getOrCreate(3).set(20);
    m.getOrCreate(4).set(30);
    Assert.assertEquals(4, (int) m.max().get().key);
    Assert.assertEquals(30, (int) m.max().get().value.get());
  }

  @Test
  public void commit_seq() {
    final var m = map();
    m.getOrCreate(42).set(52);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"42\":52}", forward.toString());
      Assert.assertEquals("\"map\":{\"42\":null}", reverse.toString());
    }
    m.getOrCreate(50).set(100);
    m.remove(42);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"42\":null,\"50\":100}", forward.toString());
      Assert.assertEquals("\"map\":{\"42\":52,\"50\":null}", reverse.toString());
    }
    m.getOrCreate(50).set(17);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"50\":17}", forward.toString());
      Assert.assertEquals("\"map\":{\"50\":100}", reverse.toString());
    }
  }

  @Test
  public void revert_seq() {
    final var m = map();
    JsonStreamReader reader = new JsonStreamReader("{\"42\":123,\"50\":100,\"100\":null}");
    m.__insert(reader);
    { // revert insertion
      Assert.assertEquals(2, m.size());
      m.getOrCreate(1000).set(24);
      Assert.assertEquals(3, m.size());
      Assert.assertTrue(m.lookup(1000).has());
      m.__revert();
      Assert.assertFalse(m.lookup(1000).has());
    }
    { // revert change
      m.getOrCreate(42).set(24);
      Assert.assertEquals(24, (int) m.lookup(42).get().get());
      m.__revert();
      Assert.assertEquals(123, (int) m.lookup(42).get().get());
    }
    { // revert delete
      Assert.assertEquals(2, m.size());
      m.remove(42);
      Assert.assertEquals(1, m.size());
      Assert.assertFalse(m.lookup(42).has());
      m.__revert();
      Assert.assertTrue(m.lookup(42).has());
      Assert.assertEquals(2, m.size());
    }
  }

  @Test
  public void seq() {
    final var m = map();
    MockRxChild child = new MockRxChild();
    m.__subscribe(child);
    m.getOrCreate(42).set(52);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"42\":52}", forward.toString());
      Assert.assertEquals("\"map\":{\"42\":null}", reverse.toString());
    }
    child.assertInvalidateCount(1);
  }

  @Test
  public void insert() {
    final var m = map();
    m.getOrCreate(100).set(50);
    JsonStreamReader reader = new JsonStreamReader("{\"42\":123,\"50\":100,\"100\":null}");
    m.__insert(reader);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{}", forward.toString());
      Assert.assertEquals("\"map\":{}", reverse.toString());
    }
    JsonStreamWriter dump = new JsonStreamWriter();
    m.__dump(dump);
    Assert.assertEquals("{\"42\":123,\"50\":100}", dump.toString());
    Assert.assertTrue(m.lookup(42).has());
    Assert.assertFalse(m.lookup(1000).has());
  }

  @Test
  public void insert_bad_key_skips() {
    final var m = map();
    m.getOrCreate(100).set(50);
    JsonStreamReader reader = new JsonStreamReader("{\"x\":123,\"50\":100,\"100\":null}");
    m.__insert(reader);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{}", forward.toString());
      Assert.assertEquals("\"map\":{}", reverse.toString());
    }
    JsonStreamWriter dump = new JsonStreamWriter();
    m.__dump(dump);
    Assert.assertEquals("{\"50\":100}", dump.toString());
    Assert.assertTrue(m.lookup(50).has());
    Assert.assertFalse(m.lookup(42).has());
  }

  @Test
  public void patch_bad_key_skips() {
    final var m = map();
    m.getOrCreate(100).set(50);
    JsonStreamReader reader = new JsonStreamReader("{\"x\":123,\"50\":100,\"100\":null}");
    m.__patch(reader);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"50\":100}", forward.toString());
      Assert.assertEquals("\"map\":{\"50\":null}", reverse.toString());
    }
    JsonStreamWriter dump = new JsonStreamWriter();
    m.__dump(dump);
    Assert.assertEquals("{\"50\":100}", dump.toString());
    Assert.assertTrue(m.lookup(50).has());
    Assert.assertFalse(m.lookup(42).has());
  }

  @Test
  public void insert_skip() {
    final var m = map();
    m.getOrCreate(100).set(50);
    JsonStreamReader reader = new JsonStreamReader("123,1000");
    m.__insert(reader);
    Assert.assertEquals(1000, reader.readInteger());
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"100\":50}", forward.toString());
      Assert.assertEquals("\"map\":{\"100\":null}", reverse.toString());
    }
    JsonStreamWriter dump = new JsonStreamWriter();
    m.__dump(dump);
    Assert.assertEquals("{\"100\":50}", dump.toString());
    Assert.assertFalse(m.lookup(42).has());
    Assert.assertFalse(m.lookup(1000).has());
  }

  @Test
  public void patch_skip() {
    final var m = map();
    m.getOrCreate(100).set(50);
    JsonStreamReader reader = new JsonStreamReader("123,1000");
    m.__patch(reader);
    Assert.assertEquals(1000, reader.readInteger());
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"100\":50}", forward.toString());
      Assert.assertEquals("\"map\":{\"100\":null}", reverse.toString());
    }
    JsonStreamWriter dump = new JsonStreamWriter();
    m.__dump(dump);
    Assert.assertEquals("{\"100\":50}", dump.toString());
    Assert.assertFalse(m.lookup(42).has());
    Assert.assertFalse(m.lookup(1000).has());
    Assert.assertTrue(m.lookup(100).has());
  }

  @Test
  public void patch() {
    final var m = map();
    m.getOrCreate(100).set(50);
    JsonStreamReader reader = new JsonStreamReader("{\"42\":123,\"50\":100,\"100\":null}");
    m.__patch(reader);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"42\":123,\"50\":100}", forward.toString());
      Assert.assertEquals("\"map\":{\"42\":null,\"50\":null}", reverse.toString());
    }
    JsonStreamWriter dump = new JsonStreamWriter();
    m.__dump(dump);
    Assert.assertEquals("{\"42\":123,\"50\":100}", dump.toString());
    Assert.assertTrue(m.lookup(42).has());
    Assert.assertFalse(m.lookup(1000).has());
  }

  @Test
  public void lookup() {
    final var m = map();
    m.getOrCreate(100).set(50);
    JsonStreamReader reader = new JsonStreamReader("{\"42\":123,\"50\":100,\"100\":null}");
    m.__insert(reader);
    Assert.assertTrue(m.lookup(42).has());
    Assert.assertTrue(m.lookup(50).has());
    Assert.assertFalse(m.lookup(1000).has());
    Assert.assertFalse(m.lookup(100).has());
    m.iterator();
  }

  @Test
  public void resurrect() {
    final var m = map();
    MockRxChild child = new MockRxChild();
    m.__subscribe(child);
    m.getOrCreate(42).set(52);
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{\"42\":52}", forward.toString());
      Assert.assertEquals("\"map\":{\"42\":null}", reverse.toString());
    }
    m.remove(42);
    Assert.assertEquals(52, (int) m.getOrCreate(42).get());
    {
      JsonStreamWriter forward = new JsonStreamWriter();
      JsonStreamWriter reverse = new JsonStreamWriter();
      m.__commit("map", forward, reverse);
      Assert.assertEquals("\"map\":{}", forward.toString());
      Assert.assertEquals("\"map\":{}", reverse.toString());
    }
    child.assertInvalidateCount(2);
    m.iterator();
  }

  @Test
  public void codec() {
    Assert.assertEquals(
        123,
        (int)
            new RxMap.IntegerCodec<RxInt32>() {
              @Override
              public RxInt32 make(RxParent maker) {
                return null;
              }
            }.fromStr("123"));
    Assert.assertEquals(
        "123",
        new RxMap.IntegerCodec<RxInt32>() {
          @Override
          public RxInt32 make(RxParent maker) {
            return null;
          }
        }.toStr(123));
    Assert.assertEquals(
        123L,
        (long)
            new RxMap.LongCodec<RxInt32>() {
              @Override
              public RxInt32 make(RxParent maker) {
                return null;
              }
            }.fromStr("123"));
    Assert.assertEquals(
        "123",
        new RxMap.LongCodec<RxInt32>() {
          @Override
          public RxInt32 make(RxParent maker) {
            return null;
          }
        }.toStr(123L));
    Assert.assertEquals(
        "123",
        new RxMap.StringCodec<RxInt32>() {
          @Override
          public RxInt32 make(RxParent maker) {
            return null;
          }
        }.fromStr("123"));
    Assert.assertEquals(
        "123",
        new RxMap.StringCodec<RxInt32>() {
          @Override
          public RxInt32 make(RxParent maker) {
            return null;
          }
        }.toStr("123"));
  }
}
