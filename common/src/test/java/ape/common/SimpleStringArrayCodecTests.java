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
package ape.common;

import org.junit.Assert;
import org.junit.Test;

public class SimpleStringArrayCodecTests {
  @Test
  public void simple_and_primary_usecase() {
    Assert.assertEquals("123:456", SimpleStringArrayCodec.pack("123", "456"));
    String[] unpack = SimpleStringArrayCodec.unpack("123:456");
    Assert.assertEquals("123", unpack[0]);
    Assert.assertEquals("456", unpack[1]);
  }

  @Test
  public void escaping_only() {
    Assert.assertEquals("##:#1", SimpleStringArrayCodec.pack("#", ":"));
    String[] unpack = SimpleStringArrayCodec.unpack("##:#1");
    Assert.assertEquals("#", unpack[0]);
    Assert.assertEquals(":", unpack[1]);
  }

  @Test
  public void escaping_bigger() {
    Assert.assertEquals("x##y:1#13:#1##", SimpleStringArrayCodec.pack("x#y", "1:3", ":#"));
    String[] unpack = SimpleStringArrayCodec.unpack("x##y:1#13:#1##");
    Assert.assertEquals("x#y", unpack[0]);
    Assert.assertEquals("1:3", unpack[1]);
    Assert.assertEquals(":#", unpack[2]);
  }
}
