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
import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.lists.ArrayNtList;
import ape.runtime.natives.lists.JoinNtList;
import ape.translator.reflect.Skip;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Standard library for list operations in Adama documents.
 * Provides flatten (nested to single), manifest (extract maybes), reverse,
 * skip, drop, join, indexing helpers, and other functional transformations.
 * Methods marked @Skip are internal and not exposed to Adama language.
 */
public class LibLists {
  @Skip
  public static <T> NtList<T> flatten(NtList<NtList<T>> list) {
    ArrayList<T> result = new ArrayList<>();
    for (NtList<T> sub : list) {
      for (T item : sub) {
        result.add(item);
      }
    }
    return new ArrayNtList<>(result);
  }

  @Skip
  public static <T> NtList<T> manifest(NtList<NtMaybe<T>> list) {
    ArrayList<T> result = new ArrayList<>();
    for (NtMaybe<T> m : list) {
      if (m.has()) {
        result.add(m.get());
      }
    }
    return new ArrayNtList<>(result);
  }

  @Skip
  public static <T> NtList<T> reverse(NtList<T> list) {
    Stack<T> stack = new Stack<>();
    ArrayList<T> result = new ArrayList<>();
    for (T item : list) {
      stack.push(item);
    }
    while (!stack.empty()) {
      result.add(stack.pop());
    }
    return new ArrayNtList<>(result);
  }

  @Skip
  public static <T> NtList<T> skip(NtList<T> list, int count) {
    ArrayList<T> result = new ArrayList<>();
    int skip = count;
    for (T item : list) {
      if (skip == 0) {
        result.add(item);
      } else {
        skip--;
      }
    }
    return new ArrayNtList<>(result);
  }

  @Skip
  public static <T> NtList<T> drop(NtList<T> list, int count) {
    ArrayList<T> result = new ArrayList<>();
    int keep = list.size() - count;
    for (T item : list) {
      if (keep > 0) {
        result.add(item);
      }
      keep--;
    }
    return new ArrayNtList<>(result);
  }

  @Skip
  public static <T> NtList<T> join(NtList<T> a, NtList<T> b) {
    return new JoinNtList<>(a, b);
  }
}
