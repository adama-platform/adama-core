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

public class NtVec3Tests {

    @Test
    public void testConstructorAndFields() {
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(1.0, vec.x, 0.01);
        Assert.assertEquals(2.0, vec.y, 0.01);
        Assert.assertEquals(3.0, vec.z, 0.01);
    }

    @Test
    public void testConstructorWithZeroValues() {
        NtVec3 vec = new NtVec3(0.0, 0.0, 0.0);
        Assert.assertEquals(0.0, vec.x, 0.01);
        Assert.assertEquals(0.0, vec.y, 0.01);
        Assert.assertEquals(0.0, vec.z, 0.01);
    }

    @Test
    public void testConstructorWithNegativeValues() {
        NtVec3 vec = new NtVec3(-1.5, -2.5, -3.5);
        Assert.assertEquals(-1.5, vec.x, 0.01);
        Assert.assertEquals(-2.5, vec.y, 0.01);
        Assert.assertEquals(-3.5, vec.z, 0.01);
    }

    @Test
    public void testConstructorWithSpecialValues() {
        NtVec3 vec = new NtVec3(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(vec.x));
        Assert.assertEquals(Double.POSITIVE_INFINITY, vec.y, 0.01);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, vec.z, 0.01);
    }

    @Test
    public void testEqualsSameObject() {
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(vec, vec);
    }

    @Test
    public void testEqualsEqualObjects() {
        NtVec3 vec1 = new NtVec3(1.0, 2.0, 3.0);
        NtVec3 vec2 = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(vec1, vec2);
        Assert.assertEquals(vec2, vec1);
    }

    @Test
    public void testEqualsDifferentObjects() {
        NtVec3 vec1 = new NtVec3(1.0, 2.0, 3.0);
        NtVec3 vec2 = new NtVec3(2.0, 1.0, 4.0);
        Assert.assertNotEquals(vec1, vec2);
    }

    @Test
    public void testEqualsNull() {
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertNotEquals(null, vec);
    }

    @Test
    public void testEqualsDifferentType() {
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Object other = new Object();
        Assert.assertNotEquals(vec, other);
    }

    @Test
    public void testEqualsSpecialValues() {
        NtVec3 vec1 = new NtVec3(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        NtVec3 vec2 = new NtVec3(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        Assert.assertEquals(vec1, vec2);
    }

    @Test
    public void testHashCodeConsistency() {
        NtVec3 vec1 = new NtVec3(1.0, 2.0, 3.0);
        NtVec3 vec2 = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(vec1.hashCode(), vec2.hashCode());
    }

    @Test
    public void testHashCodeDifferentVectors() {
        NtVec3 vec1 = new NtVec3(1.0, 2.0, 3.0);
        NtVec3 vec2 = new NtVec3(2.0, 1.0, 4.0);
        Assert.assertNotSame(vec1.hashCode(), vec2.hashCode());
    }

    @Test
    public void testToString() {
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals("vec3{x=1.0, y=2.0, z=3.0}", vec.toString());
    }

    @Test
    public void testToStringSpecialValues() {
        NtVec3 vec = new NtVec3(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        Assert.assertEquals("vec3{x=NaN, y=Infinity, z=-Infinity}", vec.toString());
    }

    @Test
    public void testMemory() {
        NtVec3 vec = new NtVec3(1.0, 2.0, 3.0);
        Assert.assertEquals(40, vec.memory());
    }
}
