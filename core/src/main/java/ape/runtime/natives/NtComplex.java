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

import ape.runtime.stdlib.LibMath;

import java.util.Objects;

/** a complex number */
public class NtComplex implements Comparable<NtComplex> {
  public final double real;
  public final double imaginary;

  public NtComplex(double real, double imaginary) {
    this.real = real;
    this.imaginary = imaginary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NtComplex ntComplex = (NtComplex) o;
    return Double.compare(ntComplex.real, real) == 0 && Double.compare(ntComplex.imaginary, imaginary) == 0;
  }

  @Override
  public String toString() {
    return real + " " + imaginary + "i";
  }

  @Override
  public int hashCode() {
    return Objects.hash(real, imaginary);
  }

  @Override
  public int compareTo(NtComplex o) {
    int delta = Double.compare(real, o.real);
    if (delta == 0) {
      return Double.compare(imaginary, o.imaginary);
    }
    return delta;
  }

  public boolean zero() {
    return LibMath.near(real, 0) && LibMath.near(imaginary, 0);
  }

  public NtComplex recip() {
    double len2 = real * real + imaginary * imaginary;
    return new NtComplex(real / len2, -imaginary / len2);
  }

  public long memory() {
    return 16;
  }
}
