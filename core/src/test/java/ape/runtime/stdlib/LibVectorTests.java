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

import ape.runtime.natives.NtVec2;
import ape.runtime.natives.NtVec3;
import ape.runtime.natives.NtVec4;

public class LibVectorTests {

    // Dot product tests for NtVec2
    @Test
    public void testDotVec2() {
        NtVec2 a = new NtVec2(1.0, 2.0);
        NtVec2 b = new NtVec2(3.0, 4.0);
        Assert.assertEquals(11.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec2Zero() {
        NtVec2 a = new NtVec2(0.0, 0.0);
        NtVec2 b = new NtVec2(0.0, 0.0);
        Assert.assertEquals(0.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec2Negative() {
        NtVec2 a = new NtVec2(-1.0, -2.0);
        NtVec2 b = new NtVec2(3.0, 4.0);
        Assert.assertEquals(-11.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec2SpecialNaN() {
        NtVec2 a = new NtVec2(Double.NaN, 1.0);
        NtVec2 b = new NtVec2(1.0, 1.0);
        Assert.assertTrue(Double.isNaN(LibVector.dot(a, b)));
    }

    @Test
    public void testDotVec2SpecialInfinity() {
        NtVec2 a = new NtVec2(Double.POSITIVE_INFINITY, 1.0);
        NtVec2 b = new NtVec2(1.0, 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec2SpecialIndeterminate() {
        NtVec2 a = new NtVec2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        NtVec2 b = new NtVec2(1.0, -1.0);
        Assert.assertTrue(Double.isNaN(LibVector.dot(a, b)));
    }

    // Dot product tests for NtVec3
    @Test
    public void testDotVec3() {
        NtVec3 a = new NtVec3(1.0, 2.0, 3.0);
        NtVec3 b = new NtVec3(4.0, 5.0, 6.0);
        Assert.assertEquals(32.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec3Zero() {
        NtVec3 a = new NtVec3(0.0, 0.0, 0.0);
        NtVec3 b = new NtVec3(0.0, 0.0, 0.0);
        Assert.assertEquals(0.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec3Negative() {
        NtVec3 a = new NtVec3(-1.0, -2.0, -3.0);
        NtVec3 b = new NtVec3(4.0, 5.0, 6.0);
        Assert.assertEquals(-32.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec3SpecialNaN() {
        NtVec3 a = new NtVec3(Double.NaN, 1.0, 1.0);
        NtVec3 b = new NtVec3(1.0, 1.0, 1.0);
        Assert.assertTrue(Double.isNaN(LibVector.dot(a, b)));
    }

    @Test
    public void testDotVec3SpecialInfinity() {
        NtVec3 a = new NtVec3(Double.POSITIVE_INFINITY, 1.0, 1.0);
        NtVec3 b = new NtVec3(1.0, 0.0, 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec3SpecialIndeterminate() {
        NtVec3 a = new NtVec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0);
        NtVec3 b = new NtVec3(1.0, -1.0, 0.0);
        Assert.assertTrue(Double.isNaN(LibVector.dot(a, b)));
    }

    // Dot product tests for NtVec4
    @Test
    public void testDotVec4() {
        NtVec4 a = new NtVec4(1.0, 2.0, 3.0, 4.0);
        NtVec4 b = new NtVec4(5.0, 6.0, 7.0, 8.0);
        Assert.assertEquals(70.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec4Zero() {
        NtVec4 a = new NtVec4(0.0, 0.0, 0.0, 0.0);
        NtVec4 b = new NtVec4(0.0, 0.0, 0.0, 0.0);
        Assert.assertEquals(0.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec4Negative() {
        NtVec4 a = new NtVec4(-1.0, -2.0, -3.0, -4.0);
        NtVec4 b = new NtVec4(5.0, 6.0, 7.0, 8.0);
        Assert.assertEquals(-70.0, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec4SpecialNaN() {
        NtVec4 a = new NtVec4(Double.NaN, 1.0, 1.0, 1.0);
        NtVec4 b = new NtVec4(1.0, 1.0, 1.0, 1.0);
        Assert.assertTrue(Double.isNaN(LibVector.dot(a, b)));
    }

    @Test
    public void testDotVec4SpecialInfinity() {
        NtVec4 a = new NtVec4(Double.POSITIVE_INFINITY, 1.0, 1.0, 1.0);
        NtVec4 b = new NtVec4(1.0, 0.0, 0.0, 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, LibVector.dot(a, b), 0.001);
    }

    @Test
    public void testDotVec4SpecialIndeterminate() {
        NtVec4 a = new NtVec4(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, 0.0);
        NtVec4 b = new NtVec4(1.0, -1.0, 0.0, 0.0);
        Assert.assertTrue(Double.isNaN(LibVector.dot(a, b)));
    }

    // Add tests for NtVec2
    @Test
    public void testAddVec2() {
        NtVec2 a = new NtVec2(1.0, 2.0);
        NtVec2 b = new NtVec2(3.0, 4.0);
        Assert.assertEquals(new NtVec2(4.0, 6.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec2Zero() {
        NtVec2 a = new NtVec2(0.0, 0.0);
        NtVec2 b = new NtVec2(0.0, 0.0);
        Assert.assertEquals(new NtVec2(0.0, 0.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec2Negative() {
        NtVec2 a = new NtVec2(-1.0, -2.0);
        NtVec2 b = new NtVec2(3.0, 4.0);
        Assert.assertEquals(new NtVec2(2.0, 2.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec2SpecialNaN() {
        NtVec2 a = new NtVec2(Double.NaN, 1.0);
        NtVec2 b = new NtVec2(1.0, 1.0);
        NtVec2 result = LibVector.add(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(2.0, result.y, 0.001);
    }

    @Test
    public void testAddVec2SpecialInfinity() {
        NtVec2 a = new NtVec2(Double.POSITIVE_INFINITY, 1.0);
        NtVec2 b = new NtVec2(1.0, 1.0);
        NtVec2 result = LibVector.add(a, b);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(2.0, result.y, 0.001);
    }

    @Test
    public void testAddVec2SpecialIndeterminate() {
        NtVec2 a = new NtVec2(Double.POSITIVE_INFINITY, 0.0);
        NtVec2 b = new NtVec2(Double.NEGATIVE_INFINITY, 0.0);
        NtVec2 result = LibVector.add(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
    }

    // Add tests for NtVec3
    @Test
    public void testAddVec3() {
        NtVec3 a = new NtVec3(1.0, 2.0, 3.0);
        NtVec3 b = new NtVec3(4.0, 5.0, 6.0);
        Assert.assertEquals(new NtVec3(5.0, 7.0, 9.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec3Zero() {
        NtVec3 a = new NtVec3(0.0, 0.0, 0.0);
        NtVec3 b = new NtVec3(0.0, 0.0, 0.0);
        Assert.assertEquals(new NtVec3(0.0, 0.0, 0.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec3Negative() {
        NtVec3 a = new NtVec3(-1.0, -2.0, -3.0);
        NtVec3 b = new NtVec3(4.0, 5.0, 6.0);
        Assert.assertEquals(new NtVec3(3.0, 3.0, 3.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec3SpecialNaN() {
        NtVec3 a = new NtVec3(Double.NaN, 1.0, 1.0);
        NtVec3 b = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibVector.add(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(2.0, result.y, 0.001);
        Assert.assertEquals(2.0, result.z, 0.001);
    }

    @Test
    public void testAddVec3SpecialInfinity() {
        NtVec3 a = new NtVec3(Double.POSITIVE_INFINITY, 1.0, 1.0);
        NtVec3 b = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibVector.add(a, b);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(2.0, result.y, 0.001);
        Assert.assertEquals(2.0, result.z, 0.001);
    }

    @Test
    public void testAddVec3SpecialIndeterminate() {
        NtVec3 a = new NtVec3(Double.POSITIVE_INFINITY, 0.0, 0.0);
        NtVec3 b = new NtVec3(Double.NEGATIVE_INFINITY, 0.0, 0.0);
        NtVec3 result = LibVector.add(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
    }

    // Add tests for NtVec4
    @Test
    public void testAddVec4() {
        NtVec4 a = new NtVec4(1.0, 2.0, 3.0, 4.0);
        NtVec4 b = new NtVec4(5.0, 6.0, 7.0, 8.0);
        Assert.assertEquals(new NtVec4(6.0, 8.0, 10.0, 12.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec4Zero() {
        NtVec4 a = new NtVec4(0.0, 0.0, 0.0, 0.0);
        NtVec4 b = new NtVec4(0.0, 0.0, 0.0, 0.0);
        Assert.assertEquals(new NtVec4(0.0, 0.0, 0.0, 0.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec4Negative() {
        NtVec4 a = new NtVec4(-1.0, -2.0, -3.0, -4.0);
        NtVec4 b = new NtVec4(5.0, 6.0, 7.0, 8.0);
        Assert.assertEquals(new NtVec4(4.0, 4.0, 4.0, 4.0), LibVector.add(a, b));
    }

    @Test
    public void testAddVec4SpecialNaN() {
        NtVec4 a = new NtVec4(Double.NaN, 1.0, 1.0, 1.0);
        NtVec4 b = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibVector.add(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(2.0, result.y, 0.001);
        Assert.assertEquals(2.0, result.z, 0.001);
        Assert.assertEquals(2.0, result.w, 0.001);
    }

    @Test
    public void testAddVec4SpecialInfinity() {
        NtVec4 a = new NtVec4(Double.POSITIVE_INFINITY, 1.0, 1.0, 1.0);
        NtVec4 b = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibVector.add(a, b);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(2.0, result.y, 0.001);
        Assert.assertEquals(2.0, result.z, 0.001);
        Assert.assertEquals(2.0, result.w, 0.001);
    }

    @Test
    public void testAddVec4SpecialIndeterminate() {
        NtVec4 a = new NtVec4(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0);
        NtVec4 b = new NtVec4(Double.NEGATIVE_INFINITY, 0.0, 0.0, 0.0);
        NtVec4 result = LibVector.add(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
        Assert.assertEquals(0.0, result.w, 0.001);
    }

    // Sub tests for NtVec2
    @Test
    public void testSubVec2() {
        NtVec2 a = new NtVec2(3.0, 4.0);
        NtVec2 b = new NtVec2(1.0, 2.0);
        Assert.assertEquals(new NtVec2(2.0, 2.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec2Zero() {
        NtVec2 a = new NtVec2(0.0, 0.0);
        NtVec2 b = new NtVec2(0.0, 0.0);
        Assert.assertEquals(new NtVec2(0.0, 0.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec2Negative() {
        NtVec2 a = new NtVec2(-1.0, -2.0);
        NtVec2 b = new NtVec2(3.0, 4.0);
        Assert.assertEquals(new NtVec2(-4.0, -6.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec2SpecialNaN() {
        NtVec2 a = new NtVec2(Double.NaN, 1.0);
        NtVec2 b = new NtVec2(1.0, 1.0);
        NtVec2 result = LibVector.sub(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
    }

    @Test
    public void testSubVec2SpecialInfinity() {
        NtVec2 a = new NtVec2(Double.POSITIVE_INFINITY, 1.0);
        NtVec2 b = new NtVec2(1.0, 1.0);
        NtVec2 result = LibVector.sub(a, b);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(0.0, result.y, 0.001);
    }

    @Test
    public void testSubVec2SpecialIndeterminate() {
        NtVec2 a = new NtVec2(Double.POSITIVE_INFINITY, 0.0);
        NtVec2 b = new NtVec2(Double.POSITIVE_INFINITY, 0.0);
        NtVec2 result = LibVector.sub(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
    }

    // Sub tests for NtVec3
    @Test
    public void testSubVec3() {
        NtVec3 a = new NtVec3(4.0, 5.0, 6.0);
        NtVec3 b = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(3.0, 3.0, 3.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec3Zero() {
        NtVec3 a = new NtVec3(0.0, 0.0, 0.0);
        NtVec3 b = new NtVec3(0.0, 0.0, 0.0);
        Assert.assertEquals(new NtVec3(0.0, 0.0, 0.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec3Negative() {
        NtVec3 a = new NtVec3(-1.0, -2.0, -3.0);
        NtVec3 b = new NtVec3(4.0, 5.0, 6.0);
        Assert.assertEquals(new NtVec3(-5.0, -7.0, -9.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec3SpecialNaN() {
        NtVec3 a = new NtVec3(Double.NaN, 1.0, 1.0);
        NtVec3 b = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibVector.sub(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
    }

    @Test
    public void testSubVec3SpecialInfinity() {
        NtVec3 a = new NtVec3(Double.POSITIVE_INFINITY, 1.0, 1.0);
        NtVec3 b = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibVector.sub(a, b);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
    }

    @Test
    public void testSubVec3SpecialIndeterminate() {
        NtVec3 a = new NtVec3(Double.POSITIVE_INFINITY, 0.0, 0.0);
        NtVec3 b = new NtVec3(Double.POSITIVE_INFINITY, 0.0, 0.0);
        NtVec3 result = LibVector.sub(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
    }

    // Sub tests for NtVec4
    @Test
    public void testSubVec4() {
        NtVec4 a = new NtVec4(5.0, 6.0, 7.0, 8.0);
        NtVec4 b = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(4.0, 4.0, 4.0, 4.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec4Zero() {
        NtVec4 a = new NtVec4(0.0, 0.0, 0.0, 0.0);
        NtVec4 b = new NtVec4(0.0, 0.0, 0.0, 0.0);
        Assert.assertEquals(new NtVec4(0.0, 0.0, 0.0, 0.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec4Negative() {
        NtVec4 a = new NtVec4(-1.0, -2.0, -3.0, -4.0);
        NtVec4 b = new NtVec4(5.0, 6.0, 7.0, 8.0);
        Assert.assertEquals(new NtVec4(-6.0, -8.0, -10.0, -12.0), LibVector.sub(a, b));
    }

    @Test
    public void testSubVec4SpecialNaN() {
        NtVec4 a = new NtVec4(Double.NaN, 1.0, 1.0, 1.0);
        NtVec4 b = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibVector.sub(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
        Assert.assertEquals(0.0, result.w, 0.001);
    }

    @Test
    public void testSubVec4SpecialInfinity() {
        NtVec4 a = new NtVec4(Double.POSITIVE_INFINITY, 1.0, 1.0, 1.0);
        NtVec4 b = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibVector.sub(a, b);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
        Assert.assertEquals(0.0, result.w, 0.001);
    }

    @Test
    public void testSubVec4SpecialIndeterminate() {
        NtVec4 a = new NtVec4(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0);
        NtVec4 b = new NtVec4(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0);
        NtVec4 result = LibVector.sub(a, b);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(0.0, result.y, 0.001);
        Assert.assertEquals(0.0, result.z, 0.001);
        Assert.assertEquals(0.0, result.w, 0.001);
    }

    // Scale tests for NtVec2
    @Test
    public void testScaleVec2() {
        double s = 2.0;
        NtVec2 v = new NtVec2(1.0, 2.0);
        Assert.assertEquals(new NtVec2(2.0, 4.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec2Zero() {
        double s = 0.0;
        NtVec2 v = new NtVec2(1.0, 2.0);
        Assert.assertEquals(new NtVec2(0.0, 0.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec2Negative() {
        double s = -1.0;
        NtVec2 v = new NtVec2(1.0, 2.0);
        Assert.assertEquals(new NtVec2(-1.0, -2.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec2SpecialNaN() {
        double s = Double.NaN;
        NtVec2 v = new NtVec2(1.0, 1.0);
        NtVec2 result = LibVector.scale(s, v);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertTrue(Double.isNaN(result.y));
    }

    @Test
    public void testScaleVec2SpecialInfinity() {
        double s = Double.POSITIVE_INFINITY;
        NtVec2 v = new NtVec2(1.0, 1.0);
        NtVec2 result = LibVector.scale(s, v);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.y, 0.001);
    }

    @Test
    public void testScaleVec2SpecialIndeterminate() {
        double s = Double.POSITIVE_INFINITY;
        NtVec2 v = new NtVec2(0.0, 1.0);
        NtVec2 result = LibVector.scale(s, v);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.y, 0.001);
    }

    // Scale tests for NtVec3
    @Test
    public void testScaleVec3() {
        double s = 2.0;
        NtVec3 v = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(2.0, 4.0, 6.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec3Zero() {
        double s = 0.0;
        NtVec3 v = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(0.0, 0.0, 0.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec3Negative() {
        double s = -1.0;
        NtVec3 v = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(new NtVec3(-1.0, -2.0, -3.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec3SpecialNaN() {
        double s = Double.NaN;
        NtVec3 v = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibVector.scale(s, v);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertTrue(Double.isNaN(result.y));
        Assert.assertTrue(Double.isNaN(result.z));
    }

    @Test
    public void testScaleVec3SpecialInfinity() {
        double s = Double.POSITIVE_INFINITY;
        NtVec3 v = new NtVec3(1.0, 1.0, 1.0);
        NtVec3 result = LibVector.scale(s, v);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.y, 0.001);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.z, 0.001);
    }

    @Test
    public void testScaleVec3SpecialIndeterminate() {
        double s = Double.POSITIVE_INFINITY;
        NtVec3 v = new NtVec3(0.0, 1.0, 0.0);
        NtVec3 result = LibVector.scale(s, v);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.y, 0.001);
        Assert.assertTrue(Double.isNaN(result.z));
    }

    // Scale tests for NtVec4
    @Test
    public void testScaleVec4() {
        double s = 2.0;
        NtVec4 v = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(2.0, 4.0, 6.0, 8.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec4Zero() {
        double s = 0.0;
        NtVec4 v = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(0.0, 0.0, 0.0, 0.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec4Negative() {
        double s = -1.0;
        NtVec4 v = new NtVec4(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(new NtVec4(-1.0, -2.0, -3.0, -4.0), LibVector.scale(s, v));
    }

    @Test
    public void testScaleVec4SpecialNaN() {
        double s = Double.NaN;
        NtVec4 v = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibVector.scale(s, v);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertTrue(Double.isNaN(result.y));
        Assert.assertTrue(Double.isNaN(result.z));
        Assert.assertTrue(Double.isNaN(result.w));
    }

    @Test
    public void testScaleVec4SpecialInfinity() {
        double s = Double.POSITIVE_INFINITY;
        NtVec4 v = new NtVec4(1.0, 1.0, 1.0, 1.0);
        NtVec4 result = LibVector.scale(s, v);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.x, 0.001);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.y, 0.001);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.z, 0.001);
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.w, 0.001);
    }

    @Test
    public void testScaleVec4SpecialIndeterminate() {
        double s = Double.POSITIVE_INFINITY;
        NtVec4 v = new NtVec4(0.0, 1.0, 0.0, 0.0);
        NtVec4 result = LibVector.scale(s, v);
        Assert.assertTrue(Double.isNaN(result.x));
        Assert.assertEquals(Double.POSITIVE_INFINITY, result.y, 0.001);
        Assert.assertTrue(Double.isNaN(result.z));
        Assert.assertTrue(Double.isNaN(result.w));
    }

    @Test
    public void testVec2Length() {
        double len = LibVector.length(new NtVec2(3, 4));
        Assert.assertEquals(5, len, 0.0001);
    }
    @Test
    public void testVec3Length() {
        double len = LibVector.length(new NtVec3(1, 2, 3));
        Assert.assertEquals(3.7416573867739413, len, 0.0001);
    }
    @Test
    public void testVec4Length() {
        double len = LibVector.length(new NtVec4(1, 2, 3, 4));
        Assert.assertEquals(5.477225575051661, len, 0.0001);
    }
}
