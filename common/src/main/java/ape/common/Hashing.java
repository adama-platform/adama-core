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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Factory methods for creating MessageDigest instances and encoding results.
 * Wraps checked exceptions from MessageDigest.getInstance() for convenience.
 * Provides Base64 and hex encoding of digest results.
 */
public class Hashing {
  public static MessageDigest md5() {
    return forKnownAlgorithm("MD5");
  }

  public static MessageDigest forKnownAlgorithm(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static MessageDigest sha384() {
    return forKnownAlgorithm("SHA-384");
  }

  public static MessageDigest sha256() {
    return forKnownAlgorithm("SHA-256");
  }

  public static String finishAndEncode(MessageDigest digest) {
    return new String(Base64.getEncoder().encode(digest.digest()), StandardCharsets.UTF_8);
  }

  public static String finishAndEncodeHex(MessageDigest digest) {
    return Hex.of(digest.digest()).toLowerCase();
  }
}
