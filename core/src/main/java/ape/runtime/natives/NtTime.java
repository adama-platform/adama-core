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
package ape.runtime.natives;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable time-of-day (hour:minute) at minute precision.
 * Represents wall-clock time without date or timezone context. The toInt()
 * encoding computes hour*60 + minute for efficient indexing (0-1439 range).
 * Parsing accepts "HH:MM" format with default minute of 0 if omitted.
 * Used for scheduling and time-based filtering in Adama documents.
 */
public class NtTime implements Comparable<NtTime> {
  public final int hour;
  public final int minute;

  public NtTime(int hour, int minute) {
    this.hour = hour;
    this.minute = minute;
  }

  @Override
  public int hashCode() {
    return Objects.hash(hour, minute);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NtTime ntTime = (NtTime) o;
    return hour == ntTime.hour && minute == ntTime.minute;
  }

  @Override
  public String toString() {
    return (hour < 10 ? ("0" + hour) : hour) + (minute < 10 ? ":0" : ":") + minute;
  }

  public long memory() {
    return 24;
  }

  public int toInt() {
    return hour * 60 + minute;
  }

  @Override
  public int compareTo(NtTime o) {
    if (hour < o.hour) {
      return -1;
    } else if (hour > o.hour) {
      return 1;
    }
    return Integer.compare(minute, o.minute);
  }

  public static NtTime parse(String val) {
    try {
      String[] parts = val.split(Pattern.quote(":"));
      return new NtTime(Integer.parseInt(parts[0]), parts.length > 1 ? Integer.parseInt(parts[1]) : 0);
    } catch (Exception nfe) {
      return new NtTime(0, 0);
    }
  }
}
