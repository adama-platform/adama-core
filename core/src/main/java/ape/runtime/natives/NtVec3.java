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

/** a 3D vector of doubles */
public class NtVec3 implements NtProxyString, Comparable<NtVec3> {
    public final double x;
    public final double y;
    public final double z;

    public NtVec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NtVec3 ntVec3 = (NtVec3) o;
        return Double.compare(x, ntVec3.x) == 0 && Double.compare(y, ntVec3.y) == 0 && Double.compare(z, ntVec3.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "vec3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public long memory() {
        return 40;
    }

    @Override
    public String pack() {
        return x + "|" + y + "|" + z;
    }

    public static NtVec3 unpack(String s) {
        double[] values = LibPacking.doubleArrayUnpack(s, 3);
        return new NtVec3(values[0], values[1], values[2]);
    }

    @Override
    public int compareTo(NtVec3 o) {
        int d = Double.compare(x, o.x);
        if (d == 0) {
            d = Double.compare(y, o.y);
            if (d == 0) {
                d = Double.compare(z, o.z);
            }
        }
        return d;
    }
}
