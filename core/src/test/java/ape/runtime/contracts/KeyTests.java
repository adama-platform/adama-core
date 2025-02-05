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
package ape.runtime.contracts;

import ape.runtime.data.Key;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class KeyTests {
  @Test
  public void coverageHash() {
    Key key = new Key("space", "key");
    Assert.assertTrue(key.hashCode() >= 0);
    Random rng = new Random();
    for (int k = 0; k < 1000; k++) {
      Key t = new Key("space" + System.nanoTime(), "key" + rng.nextDouble() + "/" + rng.nextLong());
    }
  }

  @Test
  public void coverageEquals() {
    Key key = new Key("space", "key");
    Assert.assertTrue(key.equals(new Key("space", "key")));
    Assert.assertTrue(key.equals(key));
    Assert.assertFalse(key.equals(null));
    Assert.assertFalse(key.equals("nope"));
    Assert.assertFalse(key.equals(new Key("spacex", "keyx")));
    Assert.assertFalse(key.equals(new Key("spacex", "key")));
    Assert.assertFalse(key.equals(new Key("space", "keyx")));
  }

  @Test
  public void coverageCompare() {
    Key key = new Key("space", "key");
    Assert.assertEquals(0, key.compareTo(key));
    Assert.assertEquals(0, key.compareTo(new Key("space", "key")));
    Assert.assertEquals(-1, key.compareTo(new Key("space1", "key")));
    Assert.assertEquals(-1, key.compareTo(new Key("space", "key1")));
    Assert.assertEquals(-1, key.compareTo(new Key("space1", "key1")));
    Assert.assertEquals(18, key.compareTo(new Key("a", "key")));
    Assert.assertEquals(18, key.compareTo(new Key("a", "key1")));
    Assert.assertEquals(18, key.compareTo(new Key("a", "key1")));
  }
}
