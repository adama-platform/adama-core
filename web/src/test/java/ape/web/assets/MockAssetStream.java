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
package ape.web.assets;

import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class MockAssetStream implements AssetStream {

  private ByteArrayOutputStream memory = new ByteArrayOutputStream();
  private long length = -1;
  private String type = "";
  private String md5 = null;
  private boolean done = false;
  private Integer failureCode = null;

  @Override
  public void headers(long length, String contentType, String md5) {
    this.length = length;
    this.type = contentType;
    this.md5 = md5;
  }

  public void assertHeaders(long exLength, String exType) {
    Assert.assertEquals(exLength, this.length);
    Assert.assertEquals(exType, this.type);
  }

  @Override
  public void body(byte[] chunk, int offset, int length, boolean last) {
    memory.write(chunk, offset, length);
    if (last) {
      Assert.assertFalse(done);
      done = true;
    }
  }

  public void assertNotDone() {
    Assert.assertFalse(done);
  }

  public void assertDone() {
    Assert.assertTrue(done);
  }

  public void assertBody(String expect) {
    Assert.assertEquals(expect, new String(memory.toByteArray(), StandardCharsets.UTF_8));
  }

  public void assertNoFailure() {
    Assert.assertNull(failureCode);
  }

  public void assertFailure(int code) {
    Assert.assertNotNull(failureCode);
    Assert.assertEquals(code, (int) failureCode);
  }

  @Override
  public void failure(int code) {
    this.failureCode = code;
  }
}
