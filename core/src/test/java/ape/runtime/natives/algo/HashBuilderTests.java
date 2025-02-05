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
package ape.runtime.natives.algo;

import ape.runtime.natives.*;
import ape.runtime.natives.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;

public class HashBuilderTests {
  @Test
  public void coverage() {
    HashBuilder hash = new HashBuilder();
    hash.hashNtDynamic(new NtDynamic("{}"));
    hash.hashBoolean(true);
    hash.hashBoolean(false);
    hash.hashDouble(3.14);
    hash.hashNtComplex(new NtComplex(2.71, 3.14));
    hash.hashInteger(42);
    hash.hashNtAsset(NtAsset.NOTHING);
    hash.hashString("xyz");
    hash.hashLong(123451234342L);
    hash.hashNtPrincipal(NtPrincipal.NO_ONE);
    Assert.assertEquals("UYoxfE+b51ZODGPUCfJwIne7mNacxQ7YqHWDkO0SLbB3sUqLRzz3S5JNDtAggHf/", hash.finish());
  }

  @Test
  public void datetime() {
    HashBuilder hash = new HashBuilder();
    hash.hashNtDate(new NtDate(2023, 4, 1));
    hash.hashNtTime(new NtTime(13, 14));
    hash.hashNtTimeSpan(new NtTimeSpan(100));
    hash.hashNtDateTime(new NtDateTime(ZonedDateTime.parse("2023-04-24T17:57:19.802528800-05:00[America/Chicago]")));
    Assert.assertEquals("vNvWxgbcpGdiCJxs0y5avo8q8FgEeSq+s58wqyBH1Ps79feA8rPpZypP9HE+/jxB", hash.finish());
  }

  @Test
  public void coverageReorder() {
    HashBuilder hash = new HashBuilder();
    hash.hashNtAsset(NtAsset.NOTHING);
    hash.hashLong(123451234342L);
    hash.hashNtPrincipal(NtPrincipal.NO_ONE);
    hash.hashDouble(3.14);
    hash.hashInteger(42);
    hash.hashString("xyz");
    hash.hashBoolean(false);
    hash.hashBoolean(true);
    hash.hashNtDynamic(new NtDynamic("{}"));
    hash.hashNtComplex(new NtComplex(2.71, 3.14));
    Assert.assertEquals("fwA2dTJTT6mFZKy7B9Nh2Ol1U6tbqQu0/5UCck6cHUp64vYyf6ZWNaT5LZlejRBg", hash.finish());
  }
}
