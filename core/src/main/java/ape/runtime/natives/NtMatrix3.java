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

/** a 3x3 matrix */
public class NtMatrix3 implements NtProxyString {
    public final double v_0_0;
    public final double v_0_1;
    public final double v_0_2;
    public final double v_1_0;
    public final double v_1_1;
    public final double v_1_2;
    public final double v_2_0;
    public final double v_2_1;
    public final double v_2_2;

    public NtMatrix3(double v_0_0, double v_0_1, double v_0_2,
                     double v_1_0, double v_1_1, double v_1_2,
                     double v_2_0, double v_2_1, double v_2_2) {
        this.v_0_0 = v_0_0;
        this.v_0_1 = v_0_1;
        this.v_0_2 = v_0_2;
        this.v_1_0 = v_1_0;
        this.v_1_1 = v_1_1;
        this.v_1_2 = v_1_2;
        this.v_2_0 = v_2_0;
        this.v_2_1 = v_2_1;
        this.v_2_2 = v_2_2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NtMatrix3 ntMatrix3 = (NtMatrix3) o;
        return Double.compare(v_0_0, ntMatrix3.v_0_0) == 0 && Double.compare(v_0_1, ntMatrix3.v_0_1) == 0 && Double.compare(v_0_2, ntMatrix3.v_0_2) == 0 && Double.compare(v_1_0, ntMatrix3.v_1_0) == 0 && Double.compare(v_1_1, ntMatrix3.v_1_1) == 0 && Double.compare(v_1_2, ntMatrix3.v_1_2) == 0 && Double.compare(v_2_0, ntMatrix3.v_2_0) == 0 && Double.compare(v_2_1, ntMatrix3.v_2_1) == 0 && Double.compare(v_2_2, ntMatrix3.v_2_2) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(v_0_0, v_0_1, v_0_2, v_1_0, v_1_1, v_1_2, v_2_0, v_2_1, v_2_2);
    }

    @Override
    public String toString() {
        return "matrix3{" +
                "v_0_0=" + v_0_0 +
                ", v_0_1=" + v_0_1 +
                ", v_0_2=" + v_0_2 +
                ", v_1_0=" + v_1_0 +
                ", v_1_1=" + v_1_1 +
                ", v_1_2=" + v_1_2 +
                ", v_2_0=" + v_2_0 +
                ", v_2_1=" + v_2_1 +
                ", v_2_2=" + v_2_2 +
                '}';
    }

    @Override
    public String pack() {
        return v_0_0 + "|" + v_0_1 + "|" + v_0_2 + "|" +
                v_1_0 + "|" + v_1_1 + "|" + v_1_2 + "|" +
                v_2_0 + "|" + v_2_1 + "|" + v_2_2;
    }

    public static NtMatrix3 unpack(String s) {
        double[] values = LibPacking.doubleArrayUnpack(s, 9);
        return new NtMatrix3(values[0], values[1], values[2],
                values[3], values[4], values[5],
                values[6], values[7], values[8]);
    }

    @Override
    public long memory() {
        return 144;
    }
}
