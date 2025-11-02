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

/** a 4D vector of doubles */
public class NtVec4 implements NtProxyString, Comparable<NtVec4> {
    public final double x;
    public final double y;
    public final double z;
    public final double w;

    public NtVec4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NtVec4 ntVec4 = (NtVec4) o;
        return Double.compare(x, ntVec4.x) == 0 && Double.compare(y, ntVec4.y) == 0 && Double.compare(z, ntVec4.z) == 0 && Double.compare(w, ntVec4.w) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }

    @Override
    public String toString() {
        return "vec4{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }

    @Override
    public long memory() {
        return 48;
    }

    @Override
    public String pack() {
        return x + "|" + y + "|" + z + "|" + w;
    }

    public static NtVec4 unpack(String s) {
        double[] values = LibPacking.doubleArrayUnpack(s, 4);
        return new NtVec4(values[0], values[1], values[2], values[3]);
    }

    @Override
    public int compareTo(NtVec4 o) {
        int d = Double.compare(x, o.x);
        if (d == 0) {
            d = Double.compare(y, o.y);
            if (d == 0) {
                d = Double.compare(z, o.z);
                if (d == 0) {
                    d = Double.compare(w, o.w);
                }
            }
        }
        return d;
    }

    public NtVec4 negate() {
        return new NtVec4(-x, -y, -z, -w);
    }
}
