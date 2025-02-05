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
package ape.runtime.deploy;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class CachedByteCodeTests {
  @Test
  public void empty() throws Exception {
    final byte[] p;
    {
      CachedByteCode cbc = new CachedByteCode("space", "class", "reflect", new HashMap<>());
      p = cbc.pack();
    }
    {
      CachedByteCode u = CachedByteCode.unpack(p);
      Assert.assertEquals("space", u.spaceName);
      Assert.assertEquals("class", u.className);
      Assert.assertEquals("reflect", u.reflection);
      Assert.assertEquals(0, u.classBytes.size());
    }
  }

  @Test
  public void solo() throws Exception {
    final byte[] p;
    {
      HashMap<String, byte[]> m = new HashMap<>();
      m.put("xyz", "ABC".getBytes());
      CachedByteCode cbc = new CachedByteCode("space", "class", "reflect", m);
      p = cbc.pack();
    }
    {
      CachedByteCode u = CachedByteCode.unpack(p);
      Assert.assertEquals("space", u.spaceName);
      Assert.assertEquals("class", u.className);
      Assert.assertEquals("reflect", u.reflection);
      Assert.assertEquals(1, u.classBytes.size());
      Assert.assertEquals("ABC", new String(u.classBytes.get("xyz")));
    }
  }

  @Test
  public void two() throws Exception {
    final byte[] p;
    {
      HashMap<String, byte[]> m = new HashMap<>();
      m.put("xyz", "ABC".getBytes());
      m.put("x", "DEF".getBytes());
      CachedByteCode cbc = new CachedByteCode("space", "class", "reflect", m);
      p = cbc.pack();
    }
    {
      CachedByteCode u = CachedByteCode.unpack(p);
      Assert.assertEquals("space", u.spaceName);
      Assert.assertEquals("class", u.className);
      Assert.assertEquals("reflect", u.reflection);
      Assert.assertEquals(2, u.classBytes.size());
      Assert.assertEquals("ABC", new String(u.classBytes.get("xyz")));
      Assert.assertEquals("DEF", new String(u.classBytes.get("x")));
    }
  }

  @Test
  public void bad() throws Exception {
    Assert.assertNull(CachedByteCode.unpack(new byte[] { 0, 0, 0, 0x19 }));
  }
}
