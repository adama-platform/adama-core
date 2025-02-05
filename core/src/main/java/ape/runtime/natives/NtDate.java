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

import java.time.LocalDate;
import java.util.Objects;

/** A single date in the typical gregorian calendar */
public class NtDate implements Comparable<NtDate> {
  public final int year;
  public final int month;
  public final int day;

  public NtDate(int year, int month, int day) {
    this.year = year;
    this.month = month;
    this.day = day;
  }

  @Override
  public int hashCode() {
    return Objects.hash(year, month, day);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NtDate ntDate = (NtDate) o;
    return year == ntDate.year && month == ntDate.month && day == ntDate.day;
  }

  @Override
  public String toString() {
    return year + (month < 10 ? "-0" : "-") + month + (day < 10 ? "-0" : "-") + day;
  }

  public long memory() {
    return 24;
  }

  @Override
  public int compareTo(NtDate o) {
    if (year < o.year) {
      return -1;
    } else if (year > o.year) {
      return 1;
    }
    if (month < o.month) {
      return -1;
    } else if (month > o.month) {
      return 1;
    }
    if (day < o.day) {
      return -1;
    } else if (day > o.day) {
      return 1;
    }
    return 0;
  }

  public int toInt() {
    return year * (366 * 32) + month * 32 + day;
  }

  public LocalDate toLocalDate() {
    return LocalDate.of(year, month, day);
  }

  public static NtDate parse(String val) {
    try {
      String[] parts = val.split("[/-]");
      return new NtDate(Integer.parseInt(parts[0]), parts.length > 1 ? Integer.parseInt(parts[1]) : 1, parts.length > 2 ? Integer.parseInt(parts[2]) : 1);
    } catch (Exception nfe) {
      return new NtDate(1, 1, 1);
    }
  }
}
