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
package ape.runtime.remote;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import org.junit.Assert;
import org.junit.Test;

public class RemoteResultTests {
  @Test
  public void nothing() {
    RemoteResult result = new RemoteResult(null, null, null);
    JsonStreamWriter writer = new JsonStreamWriter();
    result.write(writer);
    Assert.assertEquals("{\"result\":null,\"failure\":null,\"failure_code\":null}", writer.toString());
    RemoteResult copy = new RemoteResult(new JsonStreamReader(writer.toString()));
    Assert.assertEquals(result.result, copy.result);
    Assert.assertEquals(result.failure, copy.failure);
    Assert.assertEquals(result.failureCode, copy.failureCode);
    Assert.assertEquals(result, copy);
    Assert.assertEquals(result.hashCode(), copy.hashCode());
    Assert.assertFalse(result.equals(null));
    Assert.assertFalse(result.equals("XYZ"));
  }

  @Test
  public void success() {
    RemoteResult result = new RemoteResult("{}", null, null);
    JsonStreamWriter writer = new JsonStreamWriter();
    result.write(writer);
    Assert.assertEquals("{\"result\":{},\"failure\":null,\"failure_code\":null}", writer.toString());
    RemoteResult copy = new RemoteResult(new JsonStreamReader(writer.toString()));
    Assert.assertEquals(result.result, copy.result);
    Assert.assertEquals(result.failure, copy.failure);
    Assert.assertEquals(result.failureCode, copy.failureCode);
    Assert.assertEquals(result, copy);
    Assert.assertEquals(result.hashCode(), copy.hashCode());
    Assert.assertFalse(result.equals(null));
    Assert.assertFalse(result.equals("XYZ"));
  }

  @Test
  public void failure() {
    RemoteResult result = new RemoteResult(null, "No", 1000);
    JsonStreamWriter writer = new JsonStreamWriter();
    result.write(writer);
    Assert.assertEquals("{\"result\":null,\"failure\":\"No\",\"failure_code\":1000}", writer.toString());
    RemoteResult copy = new RemoteResult(new JsonStreamReader(writer.toString()));
    Assert.assertEquals(result.result, copy.result);
    Assert.assertEquals(result.failure, copy.failure);
    Assert.assertEquals(result.failureCode, copy.failureCode);
    Assert.assertEquals(result, copy);
    Assert.assertEquals(result.hashCode(), copy.hashCode());
    Assert.assertFalse(result.equals(null));
    Assert.assertFalse(result.equals("XYZ"));
  }

  @Test
  public void allthings() {
    RemoteResult result = new RemoteResult("{}", "Nope", 82);
    JsonStreamWriter writer = new JsonStreamWriter();
    result.write(writer);
    Assert.assertEquals("{\"result\":{},\"failure\":\"Nope\",\"failure_code\":82}", writer.toString());
    RemoteResult copy = new RemoteResult(new JsonStreamReader(writer.toString()));
    Assert.assertEquals(result.result, copy.result);
    Assert.assertEquals(result.failure, copy.failure);
    Assert.assertEquals(result.failureCode, copy.failureCode);
    Assert.assertEquals(result, copy);
    Assert.assertEquals(result.hashCode(), copy.hashCode());
    Assert.assertFalse(result.equals(null));
    Assert.assertFalse(result.equals("XYZ"));
  }
}
