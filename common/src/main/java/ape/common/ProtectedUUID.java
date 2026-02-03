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
import java.util.UUID;

/**
 * Generates URL-safe opaque identifiers from UUIDs.
 * Encodes UUIDs using a 35-character alphanumeric alphabet with a
 * checksum suffix for basic integrity verification. Produces compact,
 * human-readable identifiers suitable for external exposure.
 */
public class ProtectedUUID {
  private static final char[] UUID_CODEC_BASE = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y', 'Z'};

  public static String generate() {
    return encode(UUID.randomUUID());
  }

  public static String encode(UUID id) {
    try {
      StringBuilder sb = new StringBuilder();
      long v = id.getLeastSignificantBits();
      long trailer = 1;
      if (v < 0) {
        v = -v;
        trailer *= 2 + 1;
      }
      int m = UUID_CODEC_BASE.length;
      while (v > 0) {
        sb.append(UUID_CODEC_BASE[(int) (v % m)]);
        v /= m;
      }
      v = id.getMostSignificantBits();
      if (v < 0) {
        v = -v;
        trailer *= 2 + 1;
      }
      while (v > 0) {
        sb.append(UUID_CODEC_BASE[(int) (v % m)]);
        v /= m;
      }
      while (trailer > 0) {
        sb.append(UUID_CODEC_BASE[(int) (trailer % m)]);
        trailer /= m;
      }
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
      sb.append('X');
      v = Math.abs(digest[0] + digest[1] * 256 + digest[2] * 256 * 256);
      int signbytes = 2;
      while (v > 0 && signbytes > 0) {
        sb.append(UUID_CODEC_BASE[(int) (v % m)]);
        v /= m;
        signbytes--;
      }
      return sb.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
