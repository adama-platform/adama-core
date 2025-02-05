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
package ape.common.net;

import org.junit.Assert;
import org.junit.Test;

public class ChannelCommonTests {

  @Test
  public void edges() {
    DemoChannel channel = new DemoChannel();
    Assert.assertEquals(1, channel.makeId());
    Assert.assertEquals(5, channel.makeId());
    Assert.assertEquals(7, channel.makeId());
    for (int k = 0; k < 2097152 - 7; k++) {
      channel.makeId();
    }
    Assert.assertEquals(4194299, channel.makeId());
    Assert.assertEquals(4194301, channel.makeId());
    Assert.assertEquals(4194303, channel.makeId());
    Assert.assertEquals(1, channel.makeId());
    Assert.assertEquals(5, channel.makeId());
  }

  public static class DemoChannel extends ChannelCommon {
    public DemoChannel() {
      super(1, null);
      streams.put(3, null);
    }
  }
}
