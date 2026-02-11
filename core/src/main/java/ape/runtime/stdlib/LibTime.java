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
import ape.runtime.natives.NtTime;
import ape.runtime.natives.NtTimeSpan;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;

/**
 * Standard library for time-of-day operations in Adama documents.
 * Provides time construction, extension with timespan (clamped to day),
 * cyclic addition (wraps at midnight), overlap detection for time ranges,
 * and conversion to integer encoding. Works with NtTime at minute precision.
 */
public class LibTime {
  /** Convert a time-of-day to its integer encoding (hour * 60 + minute). */
  @Extension
  public static int toInt(NtTime t) {
    return t.toInt();
  }

  /** Extend a time by a span, clamping the result to stay within the same day (00:00..23:59). */
  @Extension
  public static NtTime extendWithinDay(NtTime t, NtTimeSpan s) {
    int end = ((int) (t.toInt() * 60 + s.seconds)) / 60;
    if (end >= 1440) end = 1439;
    if (end < 0) end = 0;
    return new NtTime(end / 60, end % 60);
  }

  /** Add a time span to a time-of-day with wrap-around at midnight (cyclic addition). */
  @Extension
  public static NtTime cyclicAdd(NtTime t, NtTimeSpan s) {
    int next = ((int) (t.toInt() * 60 + s.seconds)) / 60;
    next %= 1400;
    if (next < 0) {
      next += 1400;
    }
    return new NtTime(next / 60, next % 60);
  }

  /** Construct a time-of-day from hour (0-23) and minute (0-59). Returns empty for invalid values. */
  public static @HiddenType(clazz = NtTime.class) NtMaybe<NtTime> make(int hr, int min) {
    if (0 <= hr && hr <= 23 && 0 <= min && min <= 59) {
      return new NtMaybe<>(new NtTime(hr, min));
    } else {
      return new NtMaybe<>();
    }
  }

  /** Return true if the time range [aStart, aEnd] overlaps with [bStart, bEnd]. */
  public static boolean overlaps(NtTime aStart, NtTime aEnd, NtTime bStart, NtTime bEnd) {
    return LibMath.intersects(aStart.toInt(), aEnd.toInt(), bStart.toInt(), bEnd.toInt());
  }
}
