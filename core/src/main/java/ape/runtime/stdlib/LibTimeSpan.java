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

import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtTimeSpan;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;

/** timespan */
public class LibTimeSpan {
  @Extension
  public static NtTimeSpan add(NtTimeSpan a, NtTimeSpan b) {
    return new NtTimeSpan(a.seconds + b.seconds);
  }

  @Extension
  public static NtTimeSpan multiply(NtTimeSpan y, double x) {
    return new NtTimeSpan(x * y.seconds);
  }

  @Extension
  public static NtTimeSpan makeFromSeconds(double x) {
    return new NtTimeSpan(x);
  }

  @Extension
  public static NtTimeSpan makeFromSeconds(int x) {
    return new NtTimeSpan(x);
  }

  @Extension
  public static NtTimeSpan makeFromMinutes(double x) {
    return new NtTimeSpan(x * 60);
  }

  @Extension
  public static NtTimeSpan makeFromMinutes(int x) {
    return new NtTimeSpan(x * 60);
  }

  @Extension
  public static double seconds(NtTimeSpan x) {
    return x.seconds;
  }

  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> seconds(@HiddenType(clazz = NtTimeSpan.class) NtMaybe<NtTimeSpan> mt) {
    if (mt.has()) {
      return new NtMaybe<>(mt.get().seconds);
    }
    return new NtMaybe<>();
  }

  @Extension
  public static double minutes(NtTimeSpan x) {
    return x.seconds / 60.0;
  }

  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> minutes(@HiddenType(clazz = NtTimeSpan.class) NtMaybe<NtTimeSpan> mt) {
    if (mt.has()) {
      return new NtMaybe<>(mt.get().seconds / 60.0);
    }
    return new NtMaybe<>();
  }

  @Extension
  public static double hours(NtTimeSpan x) {
    return x.seconds / 3600.0;
  }

  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> hours(@HiddenType(clazz = NtTimeSpan.class) NtMaybe<NtTimeSpan> mt) {
    if (mt.has()) {
      return new NtMaybe<>(mt.get().seconds / 3600.0);
    }
    return new NtMaybe<>();
  }
}
