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
package ape.caravan.index;

import ape.caravan.entries.DelKey;
import ape.caravan.entries.MapKey;
import ape.runtime.data.Key;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class KeyMapTests {
  @Test
  public void flow() {
    KeyMap km = new KeyMap();
    {
      Key k1 = new Key("space", "key");
      MapKey mk1 = km.inventAndApply(k1);
      Assert.assertEquals(1, mk1.id);
      Assert.assertEquals(1, (int) km.get(k1));
      Assert.assertNull(km.inventAndApply(k1));
    }
    km.apply(new DelKey(new Key("space", "del")));
    km.apply(new DelKey(new Key("space", "key")));
    km.apply(new MapKey(new Key("s", "k"), 42));
    {
      Key k1 = new Key("space", "key");
      MapKey mk1 = km.inventAndApply(k1);
      Assert.assertEquals(43, mk1.id);
      Assert.assertEquals(43, (int) km.get(k1));
    }
    Assert.assertEquals(42, (int) km.get(new Key("s", "k")));
  }

  @Test
  public void hack() throws Exception {
    KeyMap km = new KeyMap();
    {
      MapKey mkInvent = km.inventAndApply(new Key("space", "key_invent1"));
      Assert.assertEquals(1, mkInvent.id);
    }

    Field f = km.getClass().getDeclaredField("idgen");
    f.setAccessible(true);
    f.set(km, 0);

    {
      MapKey mkInvent = km.inventAndApply(new Key("space", "key_invent2"));
      Assert.assertEquals(2, mkInvent.id);
    }
  }
}
