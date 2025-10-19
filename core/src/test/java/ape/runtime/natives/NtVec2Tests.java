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

public class NtVec2Tests {

    @Test
    public void testConstructorAndFields() {
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Assert.assertEquals(1.0, vec.x, 0.001);
        Assert.assertEquals(2.0, vec.y, 0.001);
    }

    @Test
    public void testConstructorWithZeroValues() {
        NtVec2 vec = new NtVec2(0.0, 0.0);
        Assert.assertEquals(0.0, vec.x, 0.001);
        Assert.assertEquals(0.0, vec.y, 0.001);
    }

    @Test
    public void testConstructorWithNegativeValues() {
        NtVec2 vec = new NtVec2(-1.5, -2.5);
        Assert.assertEquals(-1.5, vec.x, 0.001);
        Assert.assertEquals(-2.5, vec.y, 0.001);
    }

    @Test
    public void testConstructorWithSpecialValues() {
        NtVec2 vec = new NtVec2(Double.NaN, Double.POSITIVE_INFINITY);
        Assert.assertTrue(Double.isNaN(vec.x));
        Assert.assertEquals(Double.POSITIVE_INFINITY, vec.y, 0.001);
    }

    @Test
    public void testEqualsSameObject() {
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Assert.assertEquals(vec, vec);
    }

    @Test
    public void testEqualsEqualObjects() {
        NtVec2 vec1 = new NtVec2(1.0, 2.0);
        NtVec2 vec2 = new NtVec2(1.0, 2.0);
        Assert.assertEquals(vec1, vec2);
        Assert.assertEquals(vec2, vec1);
    }

    @Test
    public void testEqualsDifferentObjects() {
        NtVec2 vec1 = new NtVec2(1.0, 2.0);
        NtVec2 vec2 = new NtVec2(2.0, 1.0);
        Assert.assertNotEquals(vec1, vec2);
    }

    @Test
    public void testEqualsNull() {
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Assert.assertNotEquals(null, vec);
    }

    @Test
    public void testEqualsDifferentType() {
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Object other = new Object();
        Assert.assertNotEquals(vec, other);
    }

    @Test
    public void testEqualsSpecialValues() {
        NtVec2 vec1 = new NtVec2(Double.NaN, Double.POSITIVE_INFINITY);
        NtVec2 vec2 = new NtVec2(Double.NaN, Double.POSITIVE_INFINITY);
        Assert.assertEquals(vec1, vec2);
    }

    @Test
    public void testHashCodeConsistency() {
        NtVec2 vec1 = new NtVec2(1.0, 2.0);
        NtVec2 vec2 = new NtVec2(1.0, 2.0);
        Assert.assertEquals(vec1.hashCode(), vec2.hashCode());
    }

    @Test
    public void testHashCodeDifferentVectors() {
        NtVec2 vec1 = new NtVec2(1.0, 2.0);
        NtVec2 vec2 = new NtVec2(2.0, 1.0);
        Assert.assertNotSame(vec1.hashCode(), vec2.hashCode());
    }

    @Test
    public void testToString() {
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Assert.assertEquals("vec2{x=1.0, y=2.0}", vec.toString());
    }

    @Test
    public void testToStringSpecialValues() {
        NtVec2 vec = new NtVec2(Double.NaN, Double.POSITIVE_INFINITY);
        Assert.assertEquals("vec2{x=NaN, y=Infinity}", vec.toString());
    }

    @Test
    public void testMemory() {
        NtVec2 vec = new NtVec2(1.0, 2.0);
        Assert.assertEquals(32, vec.memory());
    }
}
