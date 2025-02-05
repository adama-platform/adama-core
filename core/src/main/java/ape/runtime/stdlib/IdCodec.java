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
package ape.runtime.stdlib;

/**
 * this encodes an ID in a pseudo-cryptic way that is pretty; it is purely cosmetic, but it does
 * encode in a way that brings the most change forward so it is appropriate to use in a prefix based
 * system like S3
 */
public class IdCodec {
  private static final char[] TABLE_INT_TO_CH = new char[]{'A', 'J', '8', 'N', 'F', 'W', 'S', 'X', '7', 'D', 'Q', 'M', 'R', 'P', 'Y', 'E', 'I', 'O', '3', '5', 'C', 'V', '6', 'B', 'H', 'T', '2', 'U', 'K', 'L', '9', '4', 'G'};
  private static final int[] TABLE_CH_TO_INT = buildDecoderTable(TABLE_INT_TO_CH);

  private static int[] buildDecoderTable(char[] table) {
    int[] decoder = new int[40];
    for (int k = 0; k < decoder.length; k++) {
      decoder[k] = 0;
    }
    for (int k = 0; k < table.length; k++) {
      decoder[(table[k] - '2')] = k;
    }
    return decoder;
  }

  public static String encode(long value) {
    StringBuilder sb = new StringBuilder();
    long v = value;
    int len = 0;
    do {
      int low = (int) (v % 33);
      sb.append(TABLE_INT_TO_CH[low]);
      v -= low;
      v /= 33;
      len++;
    } while (v > 0 || len < 7);
    return sb.toString();
  }

  public static long decode(String str) {
    long value = 0;
    for (int k = str.length() - 1; k >= 0; k--) {
      value *= 33;
      value += TABLE_CH_TO_INT[str.charAt(k) - '2'];
    }
    return value;
  }
}
