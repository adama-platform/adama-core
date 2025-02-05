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

import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtMaybe;

import java.util.ArrayList;
import java.util.function.Function;

/** some runtime exposed to the living document */
public class Utility {

  /** convert the maybe of one type into the maybe of another */
  public static <TyIn, TyOut> NtMaybe<TyOut> convertMaybe(final NtMaybe<TyIn> in, final Function<TyIn, TyOut> conv) {
    if (in.has()) {
      return new NtMaybe<>(conv.apply(in.get()));
    } else {
      return new NtMaybe<>();
    }
  }

  /** convert a list of Integers into an array of ints */
  public static int[] convertIntegerArrayList(ArrayList<Integer> in) {
    int[] output = new int[in.size()];
    int at = 0;
    for (Integer v : in) {
      output[at] = v;
      at++;
    }
    return output;
  }

  public static <TyIn, TyOut> TyOut[] convertMultiple(final Iterable<TyIn> source, final Function<Integer, TyOut[]> makeArray, final Function<TyIn, TyOut> conv) {
    final var out = new ArrayList<TyOut>();
    for (final TyIn item : source) {
      out.add(conv.apply(item));
    }
    return out.toArray(makeArray.apply(out.size()));
  }

  public static <TyIn, TyOut> TyOut[] convertMultiple(final TyIn[] source, final Function<Integer, TyOut[]> makeArray, final Function<TyIn, TyOut> conv) {
    final var result = makeArray.apply(source.length);
    for (var k = 0; k < source.length; k++) {
      result[k] = conv.apply(source[k]);
    }
    return result;
  }

  public static <TyIn, TyOut> TyOut convertSingle(final TyIn in, final Function<TyIn, TyOut> conv) {
    return conv.apply(in);
  }

  public static <T> T identity(final T value) {
    return value;
  }

  public static <T> NtMaybe<T> lookup(final T[] arr, final NtMaybe<Integer> k) {
    if (k.has()) {
      return lookup(arr, k.get());
    }
    return new NtMaybe<>();
  }

  public static <T> NtMaybe<T> lookup(final T[] arr, final int k) {
    final var maybe = new NtMaybe<T>();
    if (0 <= k && k < arr.length) {
      maybe.set(arr[k]);
    }
    return maybe;
  }

  public static <T> T[] readArray(JsonStreamReader reader, Function<JsonStreamReader, T> transform, Function<Integer, T[]> makeArray) {
    ArrayList<T> items = new ArrayList<T>();
    if (reader.startArray()) {
      while (reader.notEndOfArray()) {
        items.add(transform.apply(reader));
      }
    }
    return items.toArray(makeArray.apply(items.size()));
  }
}
