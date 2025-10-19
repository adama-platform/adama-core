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
package ape.runtime.json;

import ape.runtime.natives.*;
import org.junit.Assert;
import org.junit.Test;

public class JsonStreamRWComboTests {
    @Test
    public void testVec2() {
        NtVec2 input = new NtVec2(1, 2);
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeNtVec2(input);
        String output = writer.toString();
        JsonStreamReader reader = new JsonStreamReader(output);
        Assert.assertEquals(input, reader.readNtVec2());
    }

    @Test
    public void testVec3() {
        NtVec3 input = new NtVec3(1, 2, 3);
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeNtVec3(input);
        String output = writer.toString();
        JsonStreamReader reader = new JsonStreamReader(output);
        Assert.assertEquals(input, reader.readNtVec3());
    }

    @Test
    public void testVec4() {
        NtVec4 input = new NtVec4(1, 2, 3, 4);
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeNtVec4(input);
        String output = writer.toString();
        JsonStreamReader reader = new JsonStreamReader(output);
        Assert.assertEquals(input, reader.readNtVec4());
    }

    @Test
    public void testMatrix2() {
        NtMatrix2 input = new NtMatrix2(1, 2, 3, 4);
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeNtMatrix2(input);
        String output = writer.toString();
        JsonStreamReader reader = new JsonStreamReader(output);
        Assert.assertEquals(input, reader.readNtMatrix2());
    }

    @Test
    public void testMatrix3() {
        NtMatrix3 input = new NtMatrix3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeNtMatrix3(input);
        String output = writer.toString();
        JsonStreamReader reader = new JsonStreamReader(output);
        Assert.assertEquals(input, reader.readNtMatrix3());
    }

    @Test
    public void testMatrix4() {
        NtMatrix4 input = new NtMatrix4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeNtMatrix4(input);
        String output = writer.toString();
        JsonStreamReader reader = new JsonStreamReader(output);
        Assert.assertEquals(input, reader.readNtMatrix4());
    }

    @Test
    public void testMatrixH4() {
        NtMatrixH4 input = new NtMatrixH4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeNtMatrixH4(input);
        String output = writer.toString();
        JsonStreamReader reader = new JsonStreamReader(output);
        Assert.assertEquals(input, reader.readNtMatrixH4());
    }
}
