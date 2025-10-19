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
package ape.runtime.stdlib;

import org.junit.Test;
import org.junit.Assert;

import ape.runtime.natives.*;

public class LibMatrixTests {

    private static final double DELTA = 1e-10;

    // transform(NtMatrix2, NtVec2)
    @Test
    public void testTransformMatrix2Vec2() {
        NtMatrix2 matrix = new NtMatrix2(1.0, 0.0, 0.0, 1.0); // Identity
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Assert.assertEquals(new NtVec2(1.0, 2.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix2Vec2Rotation() {
        NtMatrix2 matrix = LibMatrix.to_rotation(Math.PI / 2); // 90 degrees
        NtVec2 vec = new NtVec2(1.0, 0.0);
        NtVec2 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(0.0, result.x, DELTA);
        Assert.assertEquals(1.0, result.y, DELTA);
    }

    @Test
    public void testTransformMatrix2Vec2Zero() {
        NtMatrix2 matrix = new NtMatrix2(0.0, 0.0, 0.0, 0.0);
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Assert.assertEquals(new NtVec2(0.0, 0.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix2Vec2Negative() {
        NtMatrix2 matrix = new NtMatrix2(-1.0, -2.0, -3.0, -4.0);
        NtVec2 vec = new NtVec2(1.0, 1.0);
        Assert.assertEquals(new NtVec2(-3.0, -7.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix2Vec2SpecialNaN() {
        NtMatrix2 matrix = new NtMatrix2(Double.NaN, 1.0, 1.0, 1.0);
        NtVec2 vec = new NtVec2(1.0, 1.0);
        NtVec2 result = LibMatrix.transform(matrix, vec);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(2.0, result.y, 0.001);
    }

    @Test
    public void testTransformMatrix2Vec2SpecialInfinity() {
        NtMatrix2 matrix = new NtMatrix2(Double.POSITIVE_INFINITY, 0.0, 0.0, 1.0);
        NtVec2 vec = new NtVec2(1.0, 1.0);
        NtVec2 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(1.0, result.y, 0.001);
    }

    // transform(NtMatrix3, NtVec3)
    @Test
    public void testTransformMatrix3Vec3() {
        NtMatrix3 matrix = new NtMatrix3(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0); // Identity
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(1.0, 2.0, 3.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix3Vec3RotationZ() {
        NtMatrix3 matrix = LibMatrix.to_rotation_z(Math.PI / 2); // 90 degrees around Z
        NtVec3 vec = new NtVec3(1.0, 0.0, 0.0);
        NtVec3 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(0.0, result.x, DELTA);
        Assert.assertEquals(1.0, result.y, DELTA);
        Assert.assertEquals(0.0, result.z, DELTA);
    }

    @Test
    public void testTransformMatrix3Vec3Zero() {
        NtMatrix3 matrix = new NtMatrix3(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(0.0, 0.0, 0.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix3Vec3Negative() {
        NtMatrix3 matrix = new NtMatrix3(-1.0, -2.0, -3.0, -4.0, -5.0, -6.0, -7.0, -8.0, -9.0);
        NtVec3 vec = new NtVec3(1.0, 1.0, 1.0);
        Assert.assertEquals(new NtVec3(-6.0, -15.0, -24.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix3Vec3SpecialNaN() {
        NtMatrix3 matrix = new NtMatrix3(Double.NaN, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        NtVec3 vec = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibMatrix.transform(matrix, vec);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(3.0, result.y, 0.001);
        Assert.assertEquals(3.0, result.z, 0.001);
    }

    @Test
    public void testTransformMatrix3Vec3SpecialInfinity() {
        NtMatrix3 matrix = new NtMatrix3(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
        NtVec3 vec = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(1.0, result.y, 0.001);
        Assert.assertEquals(1.0, result.z, 0.001);
    }

    // transform(NtMatrix4, NtVec4)
    @Test
    public void testTransformMatrix4Vec4() {
        NtMatrix4 matrix = new NtMatrix4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0); // Identity
        NtVec4 vec = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(1.0, 2.0, 3.0, 4.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix4Vec4Translation() {
        NtMatrix4 matrix = new NtMatrix4(1.0, 0.0, 0.0, 5.0, 0.0, 1.0, 0.0, 6.0, 0.0, 0.0, 1.0, 7.0, 0.0, 0.0, 0.0, 1.0);
        NtVec4 vec = new NtVec4(1.0, 2.0, 3.0, 1.0);
        Assert.assertEquals(new NtVec4(6.0, 8.0, 10.0, 1.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix4Vec4Zero() {
        NtMatrix4 matrix = new NtMatrix4(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        NtVec4 vec = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(0.0, 0.0, 0.0, 0.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix4Vec4Negative() {
        NtMatrix4 matrix = new NtMatrix4(-1.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, -1.0);
        NtVec4 vec = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(-1.0, -2.0, -3.0, -4.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrix4Vec4SpecialNaN() {
        NtMatrix4 matrix = new NtMatrix4(Double.NaN, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        NtVec4 vec = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibMatrix.transform(matrix, vec);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(1.0, result.y, 0.001);
        Assert.assertEquals(1.0, result.z, 0.001);
        Assert.assertEquals(1.0, result.w, 0.001);
    }

    @Test
    public void testTransformMatrix4Vec4SpecialInfinity() {
        NtMatrix4 matrix = new NtMatrix4(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        NtVec4 vec = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(1.0, result.y, 0.001);
        Assert.assertEquals(1.0, result.z, 0.001);
        Assert.assertEquals(1.0, result.w, 0.001);
    }

    // transform(NtMatrixH4, NtVec3)
    @Test
    public void testTransformMatrixH4Vec3() {
        NtMatrixH4 matrix = new NtMatrixH4(1.0, 0.0, 0.0, 4.0, 0.0, 1.0, 0.0, 5.0, 0.0, 0.0, 1.0, 6.0); // Identity with translation
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(5.0, 7.0, 9.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrixH4Vec3Zero() {
        NtMatrixH4 matrix = new NtMatrixH4(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(0.0, 0.0, 0.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrixH4Vec3Negative() {
        NtMatrixH4 matrix = new NtMatrixH4(-1.0, 0.0, 0.0, -4.0, 0.0, -1.0, 0.0, -5.0, 0.0, 0.0, -1.0, -6.0);
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(-5.0, -7.0, -9.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrixH4Vec3SpecialNaN() {
        NtMatrixH4 matrix = new NtMatrixH4(Double.NaN, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        NtVec3 vec = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibMatrix.transform(matrix, vec);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(1.0, result.y, 0.001);
        Assert.assertEquals(1.0, result.z, 0.001);
    }

    @Test
    public void testTransformMatrixH4Vec3SpecialInfinity() {
        NtMatrixH4 matrix = new NtMatrixH4(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        NtVec3 vec = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(1.0, result.y, 0.001);
        Assert.assertEquals(1.0, result.z, 0.001);
    }

    // transform(NtMatrixH4, NtVec4)
    @Test
    public void testTransformMatrixH4Vec4() {
        NtMatrixH4 matrix = new NtMatrixH4(1.0, 0.0, 0.0, 4.0, 0.0, 1.0, 0.0, 5.0, 0.0, 0.0, 1.0, 6.0);
        NtVec4 vec = new NtVec4(1.0, 2.0, 3.0, 1.0);
        Assert.assertEquals(new NtVec4(5.0, 7.0, 9.0, 1.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrixH4Vec4Zero() {
        NtMatrixH4 matrix = new NtMatrixH4(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        NtVec4 vec = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(0.0, 0.0, 0.0, 4.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrixH4Vec4Negative() {
        NtMatrixH4 matrix = new NtMatrixH4(-1.0, 0.0, 0.0, -4.0, 0.0, -1.0, 0.0, -5.0, 0.0, 0.0, -1.0, -6.0);
        NtVec4 vec = new NtVec4(1.0, 2.0, 3.0, 1.0);
        Assert.assertEquals(new NtVec4(-5.0, -7.0, -9.0, 1.0), LibMatrix.transform(matrix, vec));
    }

    @Test
    public void testTransformMatrixH4Vec4SpecialNaN() {
        NtMatrixH4 matrix = new NtMatrixH4(Double.NaN, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        NtVec4 vec = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibMatrix.transform(matrix, vec);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(1.0, result.y, 0.001);
        Assert.assertEquals(1.0, result.z, 0.001);
        Assert.assertEquals(1.0, result.w, 0.001);
    }

    @Test
    public void testTransformMatrixH4Vec4SpecialInfinity() {
        NtMatrixH4 matrix = new NtMatrixH4(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        NtVec4 vec = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(1.0, result.y, 0.001);
        Assert.assertEquals(1.0, result.z, 0.001);
        Assert.assertEquals(1.0, result.w, 0.001);
    }

    // multiply(NtMatrix2, NtMatrix2)
    @Test
    public void testMultiplyMatrix2() {
        NtMatrix2 a = new NtMatrix2(1.0, 2.0, 3.0, 4.0);
        NtMatrix2 b = new NtMatrix2(5.0, 6.0, 7.0, 8.0);
        NtMatrix2 expected = new NtMatrix2(19.0, 22.0, 43.0, 50.0);
        Assert.assertEquals(expected, LibMatrix.multiply(a, b));
    }

    @Test
    public void testMultiplyMatrix2Identity() {
        NtMatrix2 a = new NtMatrix2(1.0, 0.0, 0.0, 1.0);
        NtMatrix2 b = new NtMatrix2(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(b, LibMatrix.multiply(a, b));
    }

    @Test
    public void testMultiplyMatrix2Zero() {
        NtMatrix2 a = new NtMatrix2(0.0, 0.0, 0.0, 0.0);
        NtMatrix2 b = new NtMatrix2(1.0, 2.0, 3.0, 4.0);
        NtMatrix2 expected = new NtMatrix2(0.0, 0.0, 0.0, 0.0);
        Assert.assertEquals(expected, LibMatrix.multiply(a, b));
    }

    @Test
    public void testMultiplyMatrix2SpecialNaN() {
        NtMatrix2 a = new NtMatrix2(Double.NaN, 1.0, 1.0, 1.0);
        NtMatrix2 b = new NtMatrix2(1.0, 1.0, 1.0, 1.0);
        NtMatrix2 result = LibMatrix.multiply(a, b);
        Assert.assertTrue(Double.isNaN(result.v_0_0));
        Assert.assertTrue(Double.isNaN(result.v_0_1));
        Assert.assertEquals(2, result.v_1_0, 0.001);
        Assert.assertEquals(2, result.v_1_1, 0.001);
    }

    @Test
    public void testMultiplyMatrix2SpecialInfinity() {
        NtMatrix2 a = new NtMatrix2(Double.POSITIVE_INFINITY, 0.0, 0.0, 1.0);
        NtMatrix2 b = new NtMatrix2(1.0, 0.0, 0.0, 1.0);
        NtMatrix2 result = LibMatrix.multiply(a, b);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.v_0_0, 0.001);
        // 0 * Infinity = NaN
        Assert.assertTrue(Double.isNaN(result.v_0_1));
        Assert.assertEquals(0.0, result.v_1_0, 0.001);
        Assert.assertEquals(1.0, result.v_1_1, 0.001);
    }

    // Similar tests for other multiply overloads: NtMatrix3, NtMatrix4, NtMatrixH4 combinations

    @Test
    public void testMultiplyMatrix3() {
        NtMatrix3 a = new NtMatrix3(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        NtMatrix3 b = new NtMatrix3(9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0);
        NtMatrix3 expected = new NtMatrix3(30.0, 24.0, 18.0, 84.0, 69.0, 54.0, 138.0, 114.0, 90.0);
        Assert.assertEquals(expected, LibMatrix.multiply(a, b));
    }

    @Test
    public void testMultiplyMatrix4() {
        NtMatrix4 a = new NtMatrix4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        NtMatrix4 b = new NtMatrix4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0);
        Assert.assertEquals(b, LibMatrix.multiply(a, b));
    }

    @Test
    public void testMultiplyMatrixH4H4() {
        NtMatrixH4 a = new NtMatrixH4(1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 2.0, 0.0, 0.0, 1.0, 3.0);
        NtMatrixH4 b = new NtMatrixH4(1.0, 0.0, 0.0, 4.0, 0.0, 1.0, 0.0, 5.0, 0.0, 0.0, 1.0, 6.0);
        NtMatrixH4 expected = new NtMatrixH4(1.0, 0.0, 0.0, 5.0, 0.0, 1.0, 0.0, 7.0, 0.0, 0.0, 1.0, 9.0);
        Assert.assertEquals(expected, LibMatrix.multiply(a, b));
    }

    @Test
    public void testMultiplyMatrixH4Matrix4() {
        NtMatrixH4 a = new NtMatrixH4(1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 2.0, 0.0, 0.0, 1.0, 3.0);
        NtMatrix4 b = new NtMatrix4(1.0, 0.0, 0.0, 4.0, 0.0, 1.0, 0.0, 5.0, 0.0, 0.0, 1.0, 6.0, 0.0, 0.0, 0.0, 1.0);
        NtMatrix4 expected = new NtMatrix4(1.0, 0.0, 0.0, 5.0, 0.0, 1.0, 0.0, 7.0, 0.0, 0.0, 1.0, 9.0, 0.0, 0.0, 0.0, 1.0);
        Assert.assertEquals(expected, LibMatrix.multiply(a, b));
    }

    @Test
    public void testMultiplyMatrix4MatrixH4() {
        NtMatrix4 a = new NtMatrix4(1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 2.0, 0.0, 0.0, 1.0, 3.0, 0.0, 0.0, 0.0, 1.0);
        NtMatrixH4 b = new NtMatrixH4(1.0, 0.0, 0.0, 4.0, 0.0, 1.0, 0.0, 5.0, 0.0, 0.0, 1.0, 6.0);
        NtMatrix4 expected = new NtMatrix4(1.0, 0.0, 0.0, 5.0, 0.0, 1.0, 0.0, 7.0, 0.0, 0.0, 1.0, 9.0, 0.0, 0.0, 0.0, 1.0);
        Assert.assertEquals(expected, LibMatrix.multiply(a, b));
    }

    // to_3(NtMatrix2)
    @Test
    public void testTo3Matrix2() {
        NtMatrix2 m = new NtMatrix2(1.0, 2.0, 3.0, 4.0);
        NtMatrix3 expected = new NtMatrix3(1.0, 2.0, 0.0, 3.0, 4.0, 0.0, 0.0, 0.0, 1.0);
        Assert.assertEquals(expected, LibMatrix.to_3(m));
    }

    // to_4(NtMatrix3)
    @Test
    public void testTo4Matrix3() {
        NtMatrix3 m = new NtMatrix3(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        NtMatrix4 expected = new NtMatrix4(1.0, 2.0, 3.0, 0.0, 4.0, 5.0, 6.0, 0.0, 7.0, 8.0, 9.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        Assert.assertEquals(expected, LibMatrix.to_4(m));
    }

    // to_4(NtMatrix2)
    @Test
    public void testTo4Matrix2() {
        NtMatrix2 m = new NtMatrix2(1.0, 2.0, 3.0, 4.0);
        NtMatrix4 expected = new NtMatrix4(1.0, 2.0, 0.0, 0.0, 3.0, 4.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        Assert.assertEquals(expected, LibMatrix.to_4(m));
    }

    // to_4(NtMatrixH4)
    @Test
    public void testTo4MatrixH4() {
        NtMatrixH4 m = new NtMatrixH4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        NtMatrix4 expected = new NtMatrix4(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 0.0, 0.0, 0.0, 1.0);
        Assert.assertEquals(expected, LibMatrix.to_4(m));
    }

    // combine(NtMatrix3, NtVec3)
    @Test
    public void testCombine() {
        NtMatrix3 rotation = new NtMatrix3(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
        NtVec3 translation = new NtVec3(1.0, 2.0, 3.0);
        NtMatrixH4 expected = new NtMatrixH4(1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 2.0, 0.0, 0.0, 1.0, 3.0);
        Assert.assertEquals(expected, LibMatrix.combine(rotation, translation));
    }

    // toRotation(double)
    @Test
    public void testTo2drotation() {
        NtMatrix2 matrix = LibMatrix.to_rotation(0);
        Assert.assertEquals(1, matrix.v_0_0, 0.001);
        Assert.assertEquals(0, matrix.v_0_1, 0.001);
        Assert.assertEquals(0, matrix.v_1_0, 0.001);
        Assert.assertEquals(1, matrix.v_1_1, 0.001);
    }

    @Test
    public void testTo2drotation90() {
        NtMatrix2 matrix = LibMatrix.to_rotation(Math.PI / 2);
        Assert.assertEquals(0.0, matrix.v_0_0, DELTA);
        Assert.assertEquals(-1.0, matrix.v_0_1, DELTA);
        Assert.assertEquals(1.0, matrix.v_1_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_1, DELTA);
    }

    // RotateX(double)
    @Test
    public void testTorotationx() {
        NtMatrix3 matrix = LibMatrix.to_rotation_x(0.0);
        Assert.assertEquals(1.0, matrix.v_0_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_0_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_0_2, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_0, DELTA);
        Assert.assertEquals(1.0, matrix.v_1_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_2, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_1, DELTA);
        Assert.assertEquals(1.0, matrix.v_2_2, DELTA);
    }

    @Test
    public void testTorotationx90() {
        NtMatrix3 matrix = LibMatrix.to_rotation_x(Math.PI / 2);
        Assert.assertEquals(1.0, matrix.v_0_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_0_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_0_2, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_1, DELTA);
        Assert.assertEquals(-1.0, matrix.v_1_2, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_0, DELTA);
        Assert.assertEquals(1.0, matrix.v_2_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_2, DELTA);
    }

    // Similar for RotateY, RotateZ

    @Test
    public void testTorotationy90() {
        NtMatrix3 matrix = LibMatrix.to_rotation_y(Math.PI / 2);
        Assert.assertEquals(0.0, matrix.v_0_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_0_1, DELTA);
        Assert.assertEquals(1.0, matrix.v_0_2, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_0, DELTA);
        Assert.assertEquals(1.0, matrix.v_1_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_2, DELTA);
        Assert.assertEquals(-1.0, matrix.v_2_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_2, DELTA);
    }

    @Test
    public void testTorotationz90() {
        NtMatrix3 matrix = LibMatrix.to_rotation_z(Math.PI / 2);
        Assert.assertEquals(0.0, matrix.v_0_0, DELTA);
        Assert.assertEquals(-1.0, matrix.v_0_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_0_2, DELTA);
        Assert.assertEquals(1.0, matrix.v_1_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_1, DELTA);
        Assert.assertEquals(0.0, matrix.v_1_2, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_0, DELTA);
        Assert.assertEquals(0.0, matrix.v_2_1, DELTA);
        Assert.assertEquals(1.0, matrix.v_2_2, DELTA);
    }

    // RotateAround(double, NtVec3)
    @Test
    public void testTorotationZ90() {
        NtMatrix3 matrix = LibMatrix.to_rotation_around(Math.PI / 2, new NtVec3(0.0, 0.0, 1.0));
        NtVec3 vec = new NtVec3(1.0, 0.0, 0.0);
        NtVec3 result = LibMatrix.transform(matrix, vec);
        Assert.assertEquals(0.0, result.x, DELTA);
        Assert.assertEquals(1.0, result.y, DELTA);
        Assert.assertEquals(0.0, result.z, DELTA);
    }

    @Test
    public void testTorotationZeroAxis() {
        // Note: axis is normalized, but if len=0, /0, but test assumes non-zero
        NtMatrix3 matrix = LibMatrix.to_rotation_around(0.0, new NtVec3(1.0, 0.0, 0.0));
        Assert.assertEquals(new NtMatrix3(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0), matrix);
    }

    @Test
    public void testTorotationSpecialNaN() {
        NtVec3 axis = new NtVec3(Double.NaN, 0.0, 0.0);
        // Will cause len NaN, ux NaN, etc., result all NaN
        NtMatrix3 matrix = LibMatrix.to_rotation_around(Math.PI / 2, axis);
        Assert.assertTrue(Double.isNaN(matrix.v_0_0));
        // etc for all
    }
}
