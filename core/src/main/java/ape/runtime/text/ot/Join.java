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
package ape.runtime.text.ot;

import java.util.ArrayList;

/** many operands combined into one via concatenation */
public class Join implements Operand {
  public final ArrayList<Operand> children;

  public Join() {
    this.children = new ArrayList<>();
  }

  @Override
  public void transposeRangeIntoJoin(int at, int length, Join join) {
    int curAt = 0;
    for (Operand child : children) {
      int curLen = child.length();
      int iA = Math.max(curAt, at);
      int iB = Math.min(curAt + curLen, at + length);
      if (iA < iB) {
        child.transposeRangeIntoJoin(iA - curAt, iB - iA, join);
      }
      curAt += curLen;
    }
  }

  @Override
  public String get() {
    StringBuilder sb = new StringBuilder();
    for (Operand op : children) {
      sb.append(op.get());
    }
    return sb.toString();
  }

  @Override
  public int length() {
    int _length = 0;
    for (Operand op : children) {
      _length += op.length();
    }
    return _length;
  }
}
