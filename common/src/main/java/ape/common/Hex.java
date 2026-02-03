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

/**
 * Hexadecimal encoding and decoding utilities.
 * Converts between byte arrays and hex strings with support for
 * both lowercase and uppercase output formats.
 */
public class Hex {
  private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  private static final char[] HEX_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  public static String of(final byte[] bytes) {
    final int n = bytes.length;
    final char[] encoded = new char[n * 2];
    int j = 0;
    for (int i = 0; i < n; i++) {
      encoded[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
      encoded[j++] = HEX[0x0F & bytes[i]];
    }
    return new String(encoded);
  }

  public static String of(final byte b) {
    final char[] encoded = new char[2];
    encoded[0] = HEX[(0xF0 & b) >>> 4];
    encoded[1] = HEX[0x0F & b];
    return new String(encoded);
  }

  public static String of_upper(final byte[] bytes) {
    final int n = bytes.length;
    final char[] encoded = new char[n * 2];
    int j = 0;
    for (int i = 0; i < n; i++) {
      encoded[j++] = HEX_UPPER[(0xF0 & bytes[i]) >>> 4];
      encoded[j++] = HEX_UPPER[0x0F & bytes[i]];
    }
    return new String(encoded);
  }

  public static String of_upper(final byte b) {
    final char[] encoded = new char[2];
    encoded[0] = HEX_UPPER[(0xF0 & b) >>> 4];
    encoded[1] = HEX_UPPER[0x0F & b];
    return new String(encoded);
  }

  public static byte[] from(String hex) {
    byte[] result = new byte[hex.length() >> 1];
    int at = 0;
    for (int k = 0; k < hex.length(); k += 2) {
      result[at] = (byte) ((single(hex.charAt(k)) << 4) + single(hex.charAt(k + 1)));
      at++;
    }
    return result;
  }

  public static int single(char hex) {
    if ('0' <= hex && hex <= '9') {
      return hex - '0';
    }
    if ('a' <= hex && hex <= 'f') {
      return 10 + (hex - 'a');
    }
    if ('A' <= hex && hex <= 'F') {
      return 10 + (hex - 'A');
    }
    return 0;
  }
}
