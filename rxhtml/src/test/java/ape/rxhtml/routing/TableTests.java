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
package ape.rxhtml.routing;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.rxhtml.server.ServerSideTarget;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

public class TableTests {
  @Test
  public void flow() {
    Table table = new Table();
    table.add(Instructions.parse("/xyz/$n:number/$t:text/$z*"), new Target(100, null, null, null));
    TreeMap<String, String> captures = new TreeMap<>();
    Target target = (Target) table.route("/xyz/123/hi/there/joe", captures);
    Assert.assertEquals(100, target.status);
    Assert.assertEquals("123", captures.get("n"));
    Assert.assertEquals("hi", captures.get("t"));
    Assert.assertEquals("there/joe", captures.get("z"));
    Assert.assertEquals(340, table.measure());
  }

  @Test
  public void serversideFlow() {
    Table table = new Table();
    table.add(Instructions.parse("/xyz/$n:number"), new MockServerTarget());
    TreeMap<String, String> captures = new TreeMap<>();
    ServerSideTarget target = (ServerSideTarget) table.route("/xyz/123", captures);
    Assert.assertEquals("123", captures.get("n"));
    target.get(null, "agent", "authority", "space", "/xyz/123", new TreeMap<>(), new Callback<Target>() {
      @Override
      public void success(Target value) {
        Assert.assertEquals(200, value.status);
      }
      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
  }
}
