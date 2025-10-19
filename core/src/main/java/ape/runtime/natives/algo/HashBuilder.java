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

import ape.common.Hashing;
import ape.runtime.natives.*;
import ape.runtime.natives.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** a streaming hasher of rich types */
public class HashBuilder {
  private static final byte[] BYTES_TRUE = new byte[]{'T'};
  private static final byte[] BYTES_FALSE = new byte[]{'T'};
  public final MessageDigest digest;

  public HashBuilder() {
    this.digest = Hashing.sha384();
  }

  public void hashNtDynamic(NtDynamic value) {
    digest.update(value.json.getBytes(StandardCharsets.UTF_8));
  }

  public void hashBoolean(final boolean b) {
    if (b) {
      digest.update(BYTES_TRUE);
    } else {
      digest.update(BYTES_FALSE);
    }
  }

  public void hashNtComplex(final NtComplex c) {
    hashDouble(c.real);
    hashDouble(c.imaginary);
  }

  public void hashDouble(final double d) {
    digest.update(("" + d).getBytes(StandardCharsets.UTF_8));
  }

  public void hashInteger(final int i) {
    digest.update(("" + i).getBytes(StandardCharsets.UTF_8));
  }

  public void hashNtAsset(final NtAsset a) {
    digest.update(a.id.getBytes(StandardCharsets.UTF_8));
    digest.update(a.md5.getBytes(StandardCharsets.UTF_8));
    digest.update(a.sha384.getBytes(StandardCharsets.UTF_8));
  }

  public void hashString(final String s) {
    digest.update(s.getBytes(StandardCharsets.UTF_8));
  }

  public void hashLong(final long l) {
    digest.update(("" + l).getBytes(StandardCharsets.UTF_8));
  }

  public void hashNtPrincipal(final NtPrincipal c) {
    digest.update(c.agent.getBytes(StandardCharsets.UTF_8));
    digest.update(c.authority.getBytes(StandardCharsets.UTF_8));
  }

  public void hashNtDate(NtDate d) {
    digest.update(d.toString().getBytes(StandardCharsets.UTF_8));
  }

  public void hashNtDateTime(NtDateTime d) {
    digest.update(d.toString().getBytes(StandardCharsets.UTF_8));
  }

  public void hashNtTime(NtTime d) {
    digest.update(d.toString().getBytes(StandardCharsets.UTF_8));
  }

  public void hashNtTimeSpan(NtTimeSpan d) {
    digest.update(d.toString().getBytes(StandardCharsets.UTF_8));
  }

  public void hashNtVec2(NtVec2 d) {
    digest.update(d.pack().getBytes(StandardCharsets.UTF_8));
  }
  public void hashNtVec3(NtVec3 d) {
    digest.update(d.pack().getBytes(StandardCharsets.UTF_8));
  }
  public void hashNtVec4(NtVec4 d) {
    digest.update(d.pack().getBytes(StandardCharsets.UTF_8));
  }
  public void hashNtMatrix2(NtMatrix2 d) {
    digest.update(d.pack().getBytes(StandardCharsets.UTF_8));
  }
  public void hashNtMatrix3(NtMatrix3 d) {
    digest.update(d.pack().getBytes(StandardCharsets.UTF_8));
  }
  public void hashNtMatrixH4(NtMatrixH4 d) {
    digest.update(d.pack().getBytes(StandardCharsets.UTF_8));
  }
  public void hashNtMatrix4(NtMatrix4 d) {
    digest.update(d.pack().getBytes(StandardCharsets.UTF_8));
  }

  public String finish() {
    return Hashing.finishAndEncode(digest);
  }
}
