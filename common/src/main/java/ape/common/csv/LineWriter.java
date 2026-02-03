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
package ape.common.csv;

import java.util.PrimitiveIterator;

/**
 * RFC 4180 compliant CSV line writer.
 * Generates properly escaped CSV records with automatic quoting
 * for fields containing commas, quotes, or newlines. Supports
 * primitive types and strings.
 */
public class LineWriter {
  private StringBuilder sb = new StringBuilder();
  private boolean notFirst;

  public LineWriter() {
    this.notFirst = false;
  }

  private void writeCommaIfNotFirst() {
    if (notFirst) {
      sb.append(",");
    }
    notFirst = true;
  }

  public void write(boolean b) {
    write(b ? "true" : "false");
  }

  public void write(int x) {
    writeCommaIfNotFirst();
    sb.append(x);
  }

  public void write(double x) {
    writeCommaIfNotFirst();
    sb.append(x);
  }

  public void write(long x) {
    writeCommaIfNotFirst();
    sb.append(x);
  }

  public void write(String s) {
    writeCommaIfNotFirst();
    if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
      sb.append("\"");
      PrimitiveIterator.OfInt it = s.codePoints().iterator();
      while (it.hasNext()) {
        int cp = it.nextInt();
        switch (cp) {
          case '"':
            sb.append("\"");
          default:
            sb.append(Character.toString(cp));
        }
      }
      sb.append("\"");
    } else {
      sb.append(s);
    }
  }

  @Override
  public String toString() {
    return sb.toString();
  }
}
