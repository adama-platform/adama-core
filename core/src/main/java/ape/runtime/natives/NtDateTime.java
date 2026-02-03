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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Immutable timestamp with timezone using Java ZonedDateTime.
 * Provides full date/time/zone semantics for scheduling, logging, and
 * temporal queries. The toInt() encoding converts to minutes since epoch
 * for efficient indexing (fits in 32-bit int for ~4000 years). Parsing
 * uses ISO-8601 format with system timezone as fallback default.
 */
public class NtDateTime implements Comparable<NtDateTime> {
  public final ZonedDateTime dateTime;

  public NtDateTime(ZonedDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NtDateTime that = (NtDateTime) o;
    return Objects.equals(dateTime, that.dateTime);
  }

  @Override
  public String toString() {
    return dateTime.toString();
  }

  public long memory() {
    return 64;
  }

  @Override
  public int compareTo(NtDateTime o) {
    return dateTime.compareTo(o.dateTime);
  }

  public int toInt() {
    long val = dateTime.toInstant().toEpochMilli();
    // convert to minutes since epoch as this fits within int
    return (int) (val / 60000L);
  }

  public static NtDateTime parse(String val) {
    try {
      return new NtDateTime(ZonedDateTime.parse(val));
    } catch (Exception ex) {
      return new NtDateTime(ZonedDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()));
    }
  }
}
