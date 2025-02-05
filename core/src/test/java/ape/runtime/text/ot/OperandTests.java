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
package ape.runtime.text.ot;

import org.junit.Assert;
import org.junit.Test;

public class OperandTests {
  @Test
  public void flow() {
    Operand flow = new Raw("/* adama */");
    flow = Operand.apply(flow, "{\"clientID\":\"dzg02a\",\"changes\":[11,[0,\"x\"]]}");
    Assert.assertEquals("/* adama */x", flow.get());
    flow = Operand.apply(flow, "{\"clientID\":\"dzg02a\",\"changes\":[[0,\"z\"],12]}");
    Assert.assertEquals("z/* adama */x", flow.get());
    flow = Operand.apply(flow, "{\"clientID\":\"dzg02a\",\"changes\":[9,[0,\" adama\"],4]}");
    Assert.assertEquals("z/* adama adama */x", flow.get());
    flow = Operand.apply(flow, "{\"clientID\":\"dzg02a\",\"changes\":[4,[11],4]}");
    Assert.assertEquals("z/*  */x", flow.get());
    flow = Operand.apply(flow, "null");
    Assert.assertEquals("z/*  */x", flow.get());
  }

  @Test
  public void flow2() {
    Operand flow = new Raw("hello world");
    flow = Operand.apply(flow, "{\"clientID\":\"9ajsif\",\"changes\":[6,[0,\"X\",\"Y\",\"Z\"],5]}");
    Assert.assertEquals("hello X\nY\nZworld", flow.get());
    Assert.assertEquals(16, flow.length());
    flow = Operand.apply(flow, "{\"clientID\":\"9ajsif\",\"changes\":[7,[0,\"\",\"\"],9]}");
    Assert.assertEquals("hello X\n\nY\nZworld", flow.get());
    Assert.assertEquals(17, flow.length());
    flow = Operand.apply(flow, "{\"clientID\":\"9ajsif\",\"changes\":[14,[0,\"\",\" \"],3]}");
    Assert.assertEquals("hello X\n\nY\nZwo\n rld", flow.get());
    Assert.assertEquals(19, flow.length());
  }
}
