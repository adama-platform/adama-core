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

import ape.runtime.json.JsonStreamReader;
import org.junit.Assert;
import org.junit.Test;

public class DeployedVersionTests {
  @Test
  public void upgrade() {
    DeployedVersion justString = new DeployedVersion(new JsonStreamReader("\"main\""));
    Assert.assertEquals("main", justString.main);
    justString.hashCode();
  }

  @Test
  public void flow1() {
    DeployedVersion v = new DeployedVersion(new JsonStreamReader("{\"main\":\"xyz\",\"junk\":true,\"includes\":{\"x\":\"y\"}}"));
    Assert.assertEquals("xyz", v.main);
    Assert.assertTrue(v.includes.containsKey("x"));
    Assert.assertEquals("y", v.includes.get("x"));
    v.hashCode();
  }

  @Test
  public void flow2() {
    DeployedVersion v = new DeployedVersion(new JsonStreamReader("{\"main\":\"xyz\",\"junk\":true,\"includes\":{\"x\":\"y\"}}"));
    Assert.assertEquals("xyz", v.main);
    Assert.assertTrue(v.includes.containsKey("x"));
    Assert.assertEquals("y", v.includes.get("x"));
    v.hashCode();
  }
}
