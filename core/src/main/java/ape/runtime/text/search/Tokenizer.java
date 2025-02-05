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
package ape.runtime.text.search;

import java.util.PrimitiveIterator;
import java.util.TreeSet;

/** a very simple tokenizer */
public class Tokenizer {
  public static TreeSet<String> of(String sentence) {
    TreeSet<String> result = new TreeSet<>();
    StringBuilder word = new StringBuilder();
    PrimitiveIterator.OfInt it = sentence.codePoints().iterator();
    while (it.hasNext()) {
      int cp = it.nextInt();
      if (Character.isAlphabetic(cp)) {
        word.append(Character.toString(Character.toLowerCase(cp)));
      } else {
        if (word.length() > 0 && Character.isWhitespace(cp)) {
          result.add(word.toString());
          word.setLength(0);
        }
      }
    }
    if (word.length() > 0) {
      result.add(word.toString());
    }
    return result;
  }
}
