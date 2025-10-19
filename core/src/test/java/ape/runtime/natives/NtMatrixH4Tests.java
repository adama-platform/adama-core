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

import org.junit.Assert;
import org.junit.Test;

public class NtMatrixH4Tests {

    @Test
    public void testConstructorAndFields() {
        NtMatrixH4 matrix = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        Assert.assertEquals(1.0, matrix.v_0_0, 0.001);
        Assert.assertEquals(2.0, matrix.v_0_1, 0.001);
        Assert.assertEquals(3.0, matrix.v_0_2, 0.001);
        Assert.assertEquals(4.0, matrix.v_0_3, 0.001);
        Assert.assertEquals(5.0, matrix.v_1_0, 0.001);
        Assert.assertEquals(6.0, matrix.v_1_1, 0.001);
        Assert.assertEquals(7.0, matrix.v_1_2, 0.001);
        Assert.assertEquals(8.0, matrix.v_1_3, 0.001);
        Assert.assertEquals(9.0, matrix.v_2_0, 0.001);
        Assert.assertEquals(10.0, matrix.v_2_1, 0.001);
        Assert.assertEquals(11.0, matrix.v_2_2, 0.001);
        Assert.assertEquals(12.0, matrix.v_2_3, 0.001);
    }

    @Test
    public void testConstructorWithZeroValues() {
        NtMatrixH4 matrix = new NtMatrixH4(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        Assert.assertEquals(0.0, matrix.v_0_0, 0.001);
        Assert.assertEquals(0.0, matrix.v_0_1, 0.001);
        Assert.assertEquals(0.0, matrix.v_0_2, 0.001);
        Assert.assertEquals(0.0, matrix.v_0_3, 0.001);
        Assert.assertEquals(0.0, matrix.v_1_0, 0.001);
        Assert.assertEquals(0.0, matrix.v_1_1, 0.001);
        Assert.assertEquals(0.0, matrix.v_1_2, 0.001);
        Assert.assertEquals(0.0, matrix.v_1_3, 0.001);
        Assert.assertEquals(0.0, matrix.v_2_0, 0.001);
        Assert.assertEquals(0.0, matrix.v_2_1, 0.001);
        Assert.assertEquals(0.0, matrix.v_2_2, 0.001);
        Assert.assertEquals(0.0, matrix.v_2_3, 0.001);
    }

    @Test
    public void testConstructorWithNegativeValues() {
        NtMatrixH4 matrix = new NtMatrixH4(-1.0, -2.0, -3.0, -4.0, -5.0, -6.0, -7.0, -8.0, -9.0, -10.0, -11.0, -12.0);
        Assert.assertEquals(-1.0, matrix.v_0_0, 0.001);
        Assert.assertEquals(-2.0, matrix.v_0_1, 0.001);
        Assert.assertEquals(-3.0, matrix.v_0_2, 0.001);
        Assert.assertEquals(-4.0, matrix.v_0_3, 0.001);
        Assert.assertEquals(-5.0, matrix.v_1_0, 0.001);
        Assert.assertEquals(-6.0, matrix.v_1_1, 0.001);
        Assert.assertEquals(-7.0, matrix.v_1_2, 0.001);
        Assert.assertEquals(-8.0, matrix.v_1_3, 0.001);
        Assert.assertEquals(-9.0, matrix.v_2_0, 0.001);
        Assert.assertEquals(-10.0, matrix.v_2_1, 0.001);
        Assert.assertEquals(-11.0, matrix.v_2_2, 0.001);
        Assert.assertEquals(-12.0, matrix.v_2_3, 0.001);
    }

    @Test
    public void testConstructorWithSpecialValues() {
        NtMatrixH4 matrix = new NtMatrixH4(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0,
                2.0, 3.0, 4.0, 5.0,
                6.0, 7.0, 8.0, 9.0);
        Assert.assertTrue(Double.isNaN(matrix.v_0_0));
        Assert.assertEquals(Double.POSITIVE_INFINITY, matrix.v_0_1, 0.001);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, matrix.v_0_2, 0.001);
        Assert.assertEquals(1.0, matrix.v_0_3, 0.001);
        Assert.assertEquals(2.0, matrix.v_1_0, 0.001);
        Assert.assertEquals(3.0, matrix.v_1_1, 0.001);
        Assert.assertEquals(4.0, matrix.v_1_2, 0.001);
        Assert.assertEquals(5.0, matrix.v_1_3, 0.001);
        Assert.assertEquals(6.0, matrix.v_2_0, 0.001);
        Assert.assertEquals(7.0, matrix.v_2_1, 0.001);
        Assert.assertEquals(8.0, matrix.v_2_2, 0.001);
        Assert.assertEquals(9.0, matrix.v_2_3, 0.001);
    }

    @Test
    public void testEqualsSameObject() {
        NtMatrixH4 matrix = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        Assert.assertEquals(matrix, matrix);
    }

    @Test
    public void testEqualsEqualObjects() {
        NtMatrixH4 matrix1 = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        NtMatrixH4 matrix2 = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        Assert.assertEquals(matrix1, matrix2);
        Assert.assertEquals(matrix2, matrix1);
    }

    @Test
    public void testEqualsDifferentObjects() {
        NtMatrixH4 matrix1 = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        NtMatrixH4 matrix2 = new NtMatrixH4(12.0, 11.0, 10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0);
        Assert.assertNotEquals(matrix1, matrix2);
    }

    @Test
    public void testEqualsNull() {
        NtMatrixH4 matrix = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        Assert.assertNotEquals(null, matrix);
    }

    @Test
    public void testEqualsDifferentType() {
        NtMatrixH4 matrix = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        Object other = new Object();
        Assert.assertNotEquals(matrix, other);
    }

    @Test
    public void testEqualsSpecialValues() {
        NtMatrixH4 matrix1 = new NtMatrixH4(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0,
                2.0, 3.0, 4.0, 5.0,
                6.0, 7.0, 8.0, 9.0);
        NtMatrixH4 matrix2 = new NtMatrixH4(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0,
                2.0, 3.0, 4.0, 5.0,
                6.0, 7.0, 8.0, 9.0);
        Assert.assertEquals(matrix1, matrix2);
    }

    @Test
    public void testHashCodeConsistency() {
        NtMatrixH4 matrix1 = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        NtMatrixH4 matrix2 = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        Assert.assertEquals(matrix1.hashCode(), matrix2.hashCode());
    }

    @Test
    public void testHashCodeDifferentMatrices() {
        NtMatrixH4 matrix1 = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        NtMatrixH4 matrix2 = new NtMatrixH4(12.0, 11.0, 10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0);
        Assert.assertNotSame(matrix1.hashCode(), matrix2.hashCode());
    }

    @Test
    public void testToString() {
        NtMatrixH4 matrix = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        Assert.assertEquals("matrixh4{v_0_0=1.0, v_0_1=2.0, v_0_2=3.0, v_0_3=4.0, v_1_0=5.0, v_1_1=6.0, v_1_2=7.0, v_1_3=8.0, v_2_0=9.0, v_2_1=10.0, v_2_2=11.0, v_2_3=12.0}", matrix.toString());
    }

    @Test
    public void testToStringSpecialValues() {
        NtMatrixH4 matrix = new NtMatrixH4(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0,
                2.0, 3.0, 4.0, 5.0,
                6.0, 7.0, 8.0, 9.0);
        Assert.assertEquals("matrixh4{v_0_0=NaN, v_0_1=Infinity, v_0_2=-Infinity, v_0_3=1.0, v_1_0=2.0, v_1_1=3.0, v_1_2=4.0, v_1_3=5.0, v_2_0=6.0, v_2_1=7.0, v_2_2=8.0, v_2_3=9.0}", matrix.toString());
    }
}
