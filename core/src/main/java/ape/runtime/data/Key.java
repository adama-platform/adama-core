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
package ape.runtime.data;

import java.util.Objects;

/**
 * Composite identifier for a document within the Adama platform.
 * A key consists of a space name and document key, uniquely identifying
 * a single living document instance. Keys are immutable with cached hash
 * codes for efficient use in maps and sets. Comparison is lexicographic
 * by space first, then by key.
 */
public class Key implements Comparable<Key> {
  public final String space;
  public final String key;
  private final int cachedHashCode;

  public Key(String space, String key) {
    this.space = space;
    this.key = key;
    this.cachedHashCode = Math.abs(Objects.hash(space, key));
  }

  @Override
  public int hashCode() {
    return cachedHashCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Key key1 = (Key) o;
    return Objects.equals(space, key1.space) && Objects.equals(key, key1.key);
  }

  @Override
  public int compareTo(Key o) {
    if (this == o) return 0;
    int delta = space.compareTo(o.space);
    if (delta == 0) {
      delta = key.compareTo(o.key);
    }
    return delta;
  }
}
