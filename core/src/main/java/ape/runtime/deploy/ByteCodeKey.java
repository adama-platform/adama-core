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
package ape.runtime.deploy;

import java.util.Objects;

/** a simple key for the required inputs for byte code compiler */
public class ByteCodeKey implements Comparable<ByteCodeKey> {
  public final String spaceName;
  public final String className;
  public final String javaSource;
  public final String reflection;

  public ByteCodeKey(final String spaceName, final String className, final String javaSource, String reflection) {
    this.spaceName = spaceName;
    this.className = className;
    this.javaSource = javaSource;
    this.reflection = reflection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ByteCodeKey that = (ByteCodeKey) o;
    return Objects.equals(spaceName, that.spaceName) && Objects.equals(className, that.className) && Objects.equals(javaSource, that.javaSource) && Objects.equals(reflection, that.reflection);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spaceName, className, javaSource, reflection);
  }

  @Override
  public int compareTo(ByteCodeKey o) {
    int delta = spaceName.compareTo(o.spaceName);
    if (delta == 0) {
      delta = className.compareTo(o.className);
    }
    if (delta == 0) {
      delta = javaSource.compareTo(o.javaSource);
    }
    if (delta == 0) {
      delta = reflection.compareTo(o.reflection);
    }
    return delta;
  }
}
