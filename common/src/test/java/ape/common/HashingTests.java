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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashingTests {
  @Test
  public void okMD5() {
    MessageDigest digest = Hashing.md5();
    digest.update("X".getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals("AhKbuGEGHRoFLFkuLcazgw==", Hashing.finishAndEncode(digest));
  }

  @Test
  public void okSHA256() {
    MessageDigest digest = Hashing.sha256();
    digest.update("X".getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals("S2irOEf+2n1sYsH7y+6/o16rc1HtXnj03a3qXfZLgBU=", Hashing.finishAndEncode(digest));
  }

  @Test
  public void okSHA256EmptyHex() {
    Assert.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Hex.of(Hashing.sha256().digest()));
  }

  @Test
  public void okSHA384() {
    MessageDigest digest = Hashing.sha384();
    digest.update("X".getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals("dU/pvqqRu3rpi+5VFo4Wx7HzxapUzPg8KNszhGM8rOSGOb7ujNAF4+u2uV3UPJW3", Hashing.finishAndEncode(digest));
  }

  @Test
  public void fail() {
    try {
      Hashing.forKnownAlgorithm("SHA9000!");
      Assert.fail();
    } catch (Exception ex) {
      Assert.assertEquals("java.security.NoSuchAlgorithmException: SHA9000! MessageDigest not available", ex.getMessage());
    }
  }
}
