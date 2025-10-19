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

import ape.runtime.stdlib.LibPacking;

import java.util.Objects;

/** a 2D vector of doubles */
public class NtVec2 implements NtProxyString, Comparable<NtVec2> {
    public final double x;
    public final double y;

    public NtVec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NtVec2 ntVec2 = (NtVec2) o;
        return Double.compare(x, ntVec2.x) == 0 && Double.compare(y, ntVec2.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "vec2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public long memory() {
        return 32;
    }

    @Override
    public String pack() {
        return x + "|" + y;
    }

    public static NtVec2 unpack(String s) {
        double[] values = LibPacking.doubleArrayUnpack(s, 2);
        return new NtVec2(values[0], values[1]);
    }

    @Override
    public int compareTo(NtVec2 o) {
        int d = Double.compare(x, o.x);
        if (d == 0) {
            d = Double.compare(y, o.y);
        }
        return d;
    }
}
