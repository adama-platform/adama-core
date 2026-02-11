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

import ape.runtime.text.search.Tokenizer;
import ape.translator.reflect.Skip;

import java.util.Locale;
import java.util.TreeSet;

/**
 * Standard library for search operations in Adama documents.
 * Provides fuzzy search matching for the =? operator: matches if the needle
 * is empty, is a substring of the haystack, or shares tokenized words with it.
 */
public class LibSearch {
  /** operator for searching operator =?

   * X =? Y is true IF
   *   * X is empty string
   *   * X is a substring of Y
   *   * X has words that are zpart of the haystack
   * */
  @Skip
  public static boolean test(String needleRaw, String haystackRaw) {
    String needle = needleRaw.trim().toLowerCase(Locale.ENGLISH);
    // condition 1: needle is empty string
    if ("".equals(needle)) {
      return true;
    }

    // condition 2: the entire substring was found within the haystack
    String haystack = haystackRaw.trim().toLowerCase(Locale.ENGLISH);
    if (haystack.contains(needle.trim())) {
      return true;
    }

    // condition 3: we tokenize and check each word is within haystack
    TreeSet<String> a = Tokenizer.of(needle);
    for (String x : a) {
      if (haystack.contains(x)) {
        return true;
      }
    }
    return false;
  }
}
