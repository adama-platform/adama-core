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

import ape.runtime.json.JsonStreamReader;

/**
 * Interface for operational transformation (OT) operands in collaborative text editing.
 * An operand represents text that may have pending operations. Operations compose
 * lazily without immediate string manipulation, allowing efficient batching of
 * multiple edits. The apply() method transforms an operand by parsing JSON-encoded
 * changes (insert/retain sequences) and building a new composite operand.
 */
public interface Operand {
  /** the core algorithm of applying a OT (json encoded changes) to an operand returning another operand */
  static Operand apply(Operand start, String changes) {
    Operand result = start;
    JsonStreamReader reader = new JsonStreamReader(changes);
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        if ("changes".equals(reader.fieldName())) {
          if (reader.startArray()) {
            Join next = new Join();
            int at = 0;
            while (reader.notEndOfArray()) {
              if (reader.startArray()) {
                at += reader.readInteger();
                boolean notFirst = false;
                while (reader.notEndOfArray()) {
                  if (notFirst) {
                    next.children.add(new Raw("\n"));
                  }
                  next.children.add(new Raw(reader.readString()));
                  notFirst = true;
                }
              } else {
                int copy = reader.readInteger();
                result.transposeRangeIntoJoin(at, copy, next);
                at += copy;
              }
            }
            result = next;
          }
        } else {
          reader.skipValue();
        }
      }
    } else {
      reader.skipValue();
    }
    return result;
  }

  /** transpose range through this operation into another join */
  void transposeRangeIntoJoin(int at, int length, Join join);

  /** get the string value */
  String get();

  /** how long is the string */
  int length();
}
