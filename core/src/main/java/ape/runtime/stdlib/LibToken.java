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

import ape.runtime.natives.NtList;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;

import java.util.ArrayList;

/**
 * Standard library for token normalization and set intersection in Adama documents.
 * Provides sorting, deduplication, and intersection of integer and string token arrays.
 * Used to support search indexing and matching operations.
 */
public class LibToken {
  /** Sort a list of integers and remove duplicates, returning a primitive int array for use as a search token set. */
  @Extension
  public static final int[] sortAndUniqueAsIntTokens(@HiddenType(clazz = Integer.class) NtList<Integer> vals) {
    ArrayList<Integer> sorted = new ArrayList<>();
    for (Integer v : vals) {
      sorted.add(v);
    }
    sorted.sort(Integer::compareTo);
    ArrayList<Integer> unique = new ArrayList<>(sorted.size());
    int last = Integer.MIN_VALUE;
    for (Integer v : sorted) {
      if (v > last) {
        unique.add(v);
      }
      last = v;
    }
    int[] result = new int[unique.size()];
    for (int k = 0; k < unique.size(); k++) {
      result[k] = unique.get(k);
    }
    return result;
  }

  /** Normalize (trim + lowercase), sort, and deduplicate a list of strings into a string array for use as a search token set. */
  @Extension
  public static final String[] normalizeSortAndUniqueAsStringTokens(@HiddenType(clazz = String.class) NtList<String> vals) {
    ArrayList<String> sorted = new ArrayList<>();
    for (String v : vals) {
      sorted.add(v.trim().toLowerCase());
    }
    sorted.sort(String::compareTo);
    ArrayList<String> unique = new ArrayList<>(sorted.size());
    String last = "";
    for (String v : sorted) {
      if (v.compareTo(last) > 0) {
        unique.add(v);
      }
      last = v;
    }
    String[] result = new String[unique.size()];
    for (int k = 0; k < unique.size(); k++) {
      result[k] = unique.get(k);
    }
    return result;
  }

  /** Compute the intersection of two sorted integer token arrays using a merge-join. */
  @Extension
  public static final int[] intersect(int[] a, int[] b) {
    ArrayList<Integer> result = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < a.length && j < b.length) {
      if (a[i] < b[j]) {
        i++;
      } else if (a[i] > b[j]) {
        j++;
      } else { // equals
        result.add(a[i]);
        i++;
        j++;
      }
    }
    int[] f = new int[result.size()];
    for (int k = 0; k < f.length; k++) {
      f[k] = result.get(k);
    }
    return f;
  }

  /** Compute the intersection of two sorted string token arrays using a merge-join. */
  @Extension
  public static final String[] intersect(String[] a, String[] b) {
    ArrayList<String> result = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < a.length && j < b.length) {
      int delta = a[i].compareTo(b[j]);
      if (delta < 0) {
        i++;
      } else if (delta > 0) {
        j++;
      } else { // equals
        result.add(a[i]);
        i++;
        j++;
      }
    }
    String[] f = new String[result.size()];
    for (int k = 0; k < f.length; k++) {
      f[k] = result.get(k);
    }
    return f;
  }
}
