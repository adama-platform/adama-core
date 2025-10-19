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

/** a 2x2 matrix */
public class NtMatrix2 implements NtProxyString {
    public final double v_0_0;
    public final double v_0_1;
    public final double v_1_0;
    public final double v_1_1;

    public NtMatrix2(double v_0_0, double v_0_1, double v_1_0, double v_1_1) {
        this.v_0_0 = v_0_0;
        this.v_0_1 = v_0_1;
        this.v_1_0 = v_1_0;
        this.v_1_1 = v_1_1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NtMatrix2 ntMatrix2 = (NtMatrix2) o;
        return Double.compare(v_0_0, ntMatrix2.v_0_0) == 0 && Double.compare(v_0_1, ntMatrix2.v_0_1) == 0 && Double.compare(v_1_0, ntMatrix2.v_1_0) == 0 && Double.compare(v_1_1, ntMatrix2.v_1_1) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(v_0_0, v_0_1, v_1_0, v_1_1);
    }

    @Override
    public String toString() {
        return "matrix2{" +
                "v_0_0=" + v_0_0 +
                ", v_0_1=" + v_0_1 +
                ", v_1_0=" + v_1_0 +
                ", v_1_1=" + v_1_1 +
                '}';
    }

    @Override
    public String pack() {
        return v_0_0 + "|" + v_0_1 + "|" + v_1_0 + "|" + v_1_1;
    }

    public static NtMatrix2 unpack(String s) {
        double[] values = LibPacking.doubleArrayUnpack(s, 4);
        return new NtMatrix2(values[0], values[1], values[2], values[3]);
    }

    @Override
    public long memory() {
        return 64;
    }
}
