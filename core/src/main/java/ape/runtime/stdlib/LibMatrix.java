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

import ape.runtime.natives.*;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;
import ape.translator.reflect.Skip;
import ape.translator.reflect.UseName;

/** matrix math library */
public class LibMatrix {

    @Skip
    public static NtVec2 transform(NtMatrix2 matrix, NtVec2 vec) {
        // Assuming column vector post-multiplication: vec' = matrix * vec
        double newX = matrix.v_0_0 * vec.x + matrix.v_0_1 * vec.y;
        double newY = matrix.v_1_0 * vec.x + matrix.v_1_1 * vec.y;
        return new NtVec2(newX, newY);
    }

    @Skip
    public static NtVec3 transform(NtMatrix3 matrix, NtVec3 vec) {
        // Assuming column vector post-multiplication: vec' = matrix * vec
        double newX = matrix.v_0_0 * vec.x + matrix.v_0_1 * vec.y + matrix.v_0_2 * vec.z;
        double newY = matrix.v_1_0 * vec.x + matrix.v_1_1 * vec.y + matrix.v_1_2 * vec.z;
        double newZ = matrix.v_2_0 * vec.x + matrix.v_2_1 * vec.y + matrix.v_2_2 * vec.z;
        return new NtVec3(newX, newY, newZ);
    }

    @Skip
    public static NtVec4 transform(NtMatrix4 matrix, NtVec4 vec) {
        // Assuming column vector post-multiplication: vec' = matrix * vec
        double newX = matrix.v_0_0 * vec.x + matrix.v_0_1 * vec.y + matrix.v_0_2 * vec.z + matrix.v_0_3 * vec.w;
        double newY = matrix.v_1_0 * vec.x + matrix.v_1_1 * vec.y + matrix.v_1_2 * vec.z + matrix.v_1_3 * vec.w;
        double newZ = matrix.v_2_0 * vec.x + matrix.v_2_1 * vec.y + matrix.v_2_2 * vec.z + matrix.v_2_3 * vec.w;
        double newW = matrix.v_3_0 * vec.x + matrix.v_3_1 * vec.y + matrix.v_3_2 * vec.z + matrix.v_3_3 * vec.w;
        return new NtVec4(newX, newY, newZ, newW);
    }

    @Skip
    public static NtVec3 transform(NtMatrixH4 matrix, NtVec3 vec) {
        // Treat vec as (x, y, z, 1)
        double newX = matrix.v_0_0 * vec.x + matrix.v_0_1 * vec.y + matrix.v_0_2 * vec.z + matrix.v_0_3 * 1;
        double newY = matrix.v_1_0 * vec.x + matrix.v_1_1 * vec.y + matrix.v_1_2 * vec.z + matrix.v_1_3 * 1;
        double newZ = matrix.v_2_0 * vec.x + matrix.v_2_1 * vec.y + matrix.v_2_2 * vec.z + matrix.v_2_3 * 1;
        // newW = 1, so no need for perspective divide
        return new NtVec3(newX, newY, newZ);
    }

    @Skip
    public static NtVec4 transform(NtMatrixH4 matrix, NtVec4 vec) {
        double newX = matrix.v_0_0 * vec.x + matrix.v_0_1 * vec.y + matrix.v_0_2 * vec.z + matrix.v_0_3 * vec.w;
        double newY = matrix.v_1_0 * vec.x + matrix.v_1_1 * vec.y + matrix.v_1_2 * vec.z + matrix.v_1_3 * vec.w;
        double newZ = matrix.v_2_0 * vec.x + matrix.v_2_1 * vec.y + matrix.v_2_2 * vec.z + matrix.v_2_3 * vec.w;
        double newW = 0 * vec.x + 0 * vec.y + 0 * vec.z + 1 * vec.w; // = vec.w
        return new NtVec4(newX, newY, newZ, newW);
    }

    @Skip
    public static NtMatrix2 multiply(NtMatrix2 a, NtMatrix2 b) {
        // Matrix multiplication: result = a * b
        double new_v_0_0 = a.v_0_0 * b.v_0_0 + a.v_0_1 * b.v_1_0;
        double new_v_0_1 = a.v_0_0 * b.v_0_1 + a.v_0_1 * b.v_1_1;
        double new_v_1_0 = a.v_1_0 * b.v_0_0 + a.v_1_1 * b.v_1_0;
        double new_v_1_1 = a.v_1_0 * b.v_0_1 + a.v_1_1 * b.v_1_1;
        return new NtMatrix2(new_v_0_0, new_v_0_1, new_v_1_0, new_v_1_1);
    }

    @Skip
    public static NtMatrix3 multiply(NtMatrix3 a, NtMatrix3 b) {
        // Matrix multiplication: result = a * b
        double new_v_0_0 = a.v_0_0 * b.v_0_0 + a.v_0_1 * b.v_1_0 + a.v_0_2 * b.v_2_0;
        double new_v_0_1 = a.v_0_0 * b.v_0_1 + a.v_0_1 * b.v_1_1 + a.v_0_2 * b.v_2_1;
        double new_v_0_2 = a.v_0_0 * b.v_0_2 + a.v_0_1 * b.v_1_2 + a.v_0_2 * b.v_2_2;
        double new_v_1_0 = a.v_1_0 * b.v_0_0 + a.v_1_1 * b.v_1_0 + a.v_1_2 * b.v_2_0;
        double new_v_1_1 = a.v_1_0 * b.v_0_1 + a.v_1_1 * b.v_1_1 + a.v_1_2 * b.v_2_1;
        double new_v_1_2 = a.v_1_0 * b.v_0_2 + a.v_1_1 * b.v_1_2 + a.v_1_2 * b.v_2_2;
        double new_v_2_0 = a.v_2_0 * b.v_0_0 + a.v_2_1 * b.v_1_0 + a.v_2_2 * b.v_2_0;
        double new_v_2_1 = a.v_2_0 * b.v_0_1 + a.v_2_1 * b.v_1_1 + a.v_2_2 * b.v_2_1;
        double new_v_2_2 = a.v_2_0 * b.v_0_2 + a.v_2_1 * b.v_1_2 + a.v_2_2 * b.v_2_2;
        return new NtMatrix3(new_v_0_0, new_v_0_1, new_v_0_2,
                new_v_1_0, new_v_1_1, new_v_1_2,
                new_v_2_0, new_v_2_1, new_v_2_2);
    }

    @Skip
    public static NtMatrix4 multiply(NtMatrix4 a, NtMatrix4 b) {
        // Matrix multiplication: result = a * b
        double new_v_0_0 = a.v_0_0 * b.v_0_0 + a.v_0_1 * b.v_1_0 + a.v_0_2 * b.v_2_0 + a.v_0_3 * b.v_3_0;
        double new_v_0_1 = a.v_0_0 * b.v_0_1 + a.v_0_1 * b.v_1_1 + a.v_0_2 * b.v_2_1 + a.v_0_3 * b.v_3_1;
        double new_v_0_2 = a.v_0_0 * b.v_0_2 + a.v_0_1 * b.v_1_2 + a.v_0_2 * b.v_2_2 + a.v_0_3 * b.v_3_2;
        double new_v_0_3 = a.v_0_0 * b.v_0_3 + a.v_0_1 * b.v_1_3 + a.v_0_2 * b.v_2_3 + a.v_0_3 * b.v_3_3;
        double new_v_1_0 = a.v_1_0 * b.v_0_0 + a.v_1_1 * b.v_1_0 + a.v_1_2 * b.v_2_0 + a.v_1_3 * b.v_3_0;
        double new_v_1_1 = a.v_1_0 * b.v_0_1 + a.v_1_1 * b.v_1_1 + a.v_1_2 * b.v_2_1 + a.v_1_3 * b.v_3_1;
        double new_v_1_2 = a.v_1_0 * b.v_0_2 + a.v_1_1 * b.v_1_2 + a.v_1_2 * b.v_2_2 + a.v_1_3 * b.v_3_2;
        double new_v_1_3 = a.v_1_0 * b.v_0_3 + a.v_1_1 * b.v_1_3 + a.v_1_2 * b.v_2_3 + a.v_1_3 * b.v_3_3;
        double new_v_2_0 = a.v_2_0 * b.v_0_0 + a.v_2_1 * b.v_1_0 + a.v_2_2 * b.v_2_0 + a.v_2_3 * b.v_3_0;
        double new_v_2_1 = a.v_2_0 * b.v_0_1 + a.v_2_1 * b.v_1_1 + a.v_2_2 * b.v_2_1 + a.v_2_3 * b.v_3_1;
        double new_v_2_2 = a.v_2_0 * b.v_0_2 + a.v_2_1 * b.v_1_2 + a.v_2_2 * b.v_2_2 + a.v_2_3 * b.v_3_2;
        double new_v_2_3 = a.v_2_0 * b.v_0_3 + a.v_2_1 * b.v_1_3 + a.v_2_2 * b.v_2_3 + a.v_2_3 * b.v_3_3;
        double new_v_3_0 = a.v_3_0 * b.v_0_0 + a.v_3_1 * b.v_1_0 + a.v_3_2 * b.v_2_0 + a.v_3_3 * b.v_3_0;
        double new_v_3_1 = a.v_3_0 * b.v_0_1 + a.v_3_1 * b.v_1_1 + a.v_3_2 * b.v_2_1 + a.v_3_3 * b.v_3_1;
        double new_v_3_2 = a.v_3_0 * b.v_0_2 + a.v_3_1 * b.v_1_2 + a.v_3_2 * b.v_2_2 + a.v_3_3 * b.v_3_2;
        double new_v_3_3 = a.v_3_0 * b.v_0_3 + a.v_3_1 * b.v_1_3 + a.v_3_2 * b.v_2_3 + a.v_3_3 * b.v_3_3;
        return new NtMatrix4(new_v_0_0, new_v_0_1, new_v_0_2, new_v_0_3,
                new_v_1_0, new_v_1_1, new_v_1_2, new_v_1_3,
                new_v_2_0, new_v_2_1, new_v_2_2, new_v_2_3,
                new_v_3_0, new_v_3_1, new_v_3_2, new_v_3_3);
    }

    @Skip
    public static NtMatrixH4 multiply(NtMatrixH4 a, NtMatrixH4 b) {
        // Matrix multiplication: result = a * b, result is also homogeneous
        double new_v_0_0 = a.v_0_0 * b.v_0_0 + a.v_0_1 * b.v_1_0 + a.v_0_2 * b.v_2_0 + a.v_0_3 * 0;
        double new_v_0_1 = a.v_0_0 * b.v_0_1 + a.v_0_1 * b.v_1_1 + a.v_0_2 * b.v_2_1 + a.v_0_3 * 0;
        double new_v_0_2 = a.v_0_0 * b.v_0_2 + a.v_0_1 * b.v_1_2 + a.v_0_2 * b.v_2_2 + a.v_0_3 * 0;
        double new_v_0_3 = a.v_0_0 * b.v_0_3 + a.v_0_1 * b.v_1_3 + a.v_0_2 * b.v_2_3 + a.v_0_3 * 1;
        double new_v_1_0 = a.v_1_0 * b.v_0_0 + a.v_1_1 * b.v_1_0 + a.v_1_2 * b.v_2_0 + a.v_1_3 * 0;
        double new_v_1_1 = a.v_1_0 * b.v_0_1 + a.v_1_1 * b.v_1_1 + a.v_1_2 * b.v_2_1 + a.v_1_3 * 0;
        double new_v_1_2 = a.v_1_0 * b.v_0_2 + a.v_1_1 * b.v_1_2 + a.v_1_2 * b.v_2_2 + a.v_1_3 * 0;
        double new_v_1_3 = a.v_1_0 * b.v_0_3 + a.v_1_1 * b.v_1_3 + a.v_1_2 * b.v_2_3 + a.v_1_3 * 1;
        double new_v_2_0 = a.v_2_0 * b.v_0_0 + a.v_2_1 * b.v_1_0 + a.v_2_2 * b.v_2_0 + a.v_2_3 * 0;
        double new_v_2_1 = a.v_2_0 * b.v_0_1 + a.v_2_1 * b.v_1_1 + a.v_2_2 * b.v_2_1 + a.v_2_3 * 0;
        double new_v_2_2 = a.v_2_0 * b.v_0_2 + a.v_2_1 * b.v_1_2 + a.v_2_2 * b.v_2_2 + a.v_2_3 * 0;
        double new_v_2_3 = a.v_2_0 * b.v_0_3 + a.v_2_1 * b.v_1_3 + a.v_2_2 * b.v_2_3 + a.v_2_3 * 1;
        return new NtMatrixH4(new_v_0_0, new_v_0_1, new_v_0_2, new_v_0_3,
                new_v_1_0, new_v_1_1, new_v_1_2, new_v_1_3,
                new_v_2_0, new_v_2_1, new_v_2_2, new_v_2_3);
    }

    @Skip
    public static NtMatrix4 multiply(NtMatrixH4 a, NtMatrix4 b) {
        // Matrix multiplication: result = a * b, treating a as full 4x4 with bottom row [0,0,0,1]
        double new_v_0_0 = a.v_0_0 * b.v_0_0 + a.v_0_1 * b.v_1_0 + a.v_0_2 * b.v_2_0 + a.v_0_3 * b.v_3_0;
        double new_v_0_1 = a.v_0_0 * b.v_0_1 + a.v_0_1 * b.v_1_1 + a.v_0_2 * b.v_2_1 + a.v_0_3 * b.v_3_1;
        double new_v_0_2 = a.v_0_0 * b.v_0_2 + a.v_0_1 * b.v_1_2 + a.v_0_2 * b.v_2_2 + a.v_0_3 * b.v_3_2;
        double new_v_0_3 = a.v_0_0 * b.v_0_3 + a.v_0_1 * b.v_1_3 + a.v_0_2 * b.v_2_3 + a.v_0_3 * b.v_3_3;
        double new_v_1_0 = a.v_1_0 * b.v_0_0 + a.v_1_1 * b.v_1_0 + a.v_1_2 * b.v_2_0 + a.v_1_3 * b.v_3_0;
        double new_v_1_1 = a.v_1_0 * b.v_0_1 + a.v_1_1 * b.v_1_1 + a.v_1_2 * b.v_2_1 + a.v_1_3 * b.v_3_1;
        double new_v_1_2 = a.v_1_0 * b.v_0_2 + a.v_1_1 * b.v_1_2 + a.v_1_2 * b.v_2_2 + a.v_1_3 * b.v_3_2;
        double new_v_1_3 = a.v_1_0 * b.v_0_3 + a.v_1_1 * b.v_1_3 + a.v_1_2 * b.v_2_3 + a.v_1_3 * b.v_3_3;
        double new_v_2_0 = a.v_2_0 * b.v_0_0 + a.v_2_1 * b.v_1_0 + a.v_2_2 * b.v_2_0 + a.v_2_3 * b.v_3_0;
        double new_v_2_1 = a.v_2_0 * b.v_0_1 + a.v_2_1 * b.v_1_1 + a.v_2_2 * b.v_2_1 + a.v_2_3 * b.v_3_1;
        double new_v_2_2 = a.v_2_0 * b.v_0_2 + a.v_2_1 * b.v_1_2 + a.v_2_2 * b.v_2_2 + a.v_2_3 * b.v_3_2;
        double new_v_2_3 = a.v_2_0 * b.v_0_3 + a.v_2_1 * b.v_1_3 + a.v_2_2 * b.v_2_3 + a.v_2_3 * b.v_3_3;
        double new_v_3_0 = 0 * b.v_0_0 + 0 * b.v_1_0 + 0 * b.v_2_0 + 1 * b.v_3_0;
        double new_v_3_1 = 0 * b.v_0_1 + 0 * b.v_1_1 + 0 * b.v_2_1 + 1 * b.v_3_1;
        double new_v_3_2 = 0 * b.v_0_2 + 0 * b.v_1_2 + 0 * b.v_2_2 + 1 * b.v_3_2;
        double new_v_3_3 = 0 * b.v_0_3 + 0 * b.v_1_3 + 0 * b.v_2_3 + 1 * b.v_3_3;
        return new NtMatrix4(new_v_0_0, new_v_0_1, new_v_0_2, new_v_0_3,
                new_v_1_0, new_v_1_1, new_v_1_2, new_v_1_3,
                new_v_2_0, new_v_2_1, new_v_2_2, new_v_2_3,
                new_v_3_0, new_v_3_1, new_v_3_2, new_v_3_3);
    }

    @Skip
    public static NtMatrix4 multiply(NtMatrix4 a, NtMatrixH4 b) {
        // Matrix multiplication: result = a * b, treating b as full 4x4 with bottom row [0,0,0,1]
        double new_v_0_0 = a.v_0_0 * b.v_0_0 + a.v_0_1 * b.v_1_0 + a.v_0_2 * b.v_2_0 + a.v_0_3 * 0;
        double new_v_0_1 = a.v_0_0 * b.v_0_1 + a.v_0_1 * b.v_1_1 + a.v_0_2 * b.v_2_1 + a.v_0_3 * 0;
        double new_v_0_2 = a.v_0_0 * b.v_0_2 + a.v_0_1 * b.v_1_2 + a.v_0_2 * b.v_2_2 + a.v_0_3 * 0;
        double new_v_0_3 = a.v_0_0 * b.v_0_3 + a.v_0_1 * b.v_1_3 + a.v_0_2 * b.v_2_3 + a.v_0_3 * 1;
        double new_v_1_0 = a.v_1_0 * b.v_0_0 + a.v_1_1 * b.v_1_0 + a.v_1_2 * b.v_2_0 + a.v_1_3 * 0;
        double new_v_1_1 = a.v_1_0 * b.v_0_1 + a.v_1_1 * b.v_1_1 + a.v_1_2 * b.v_2_1 + a.v_1_3 * 0;
        double new_v_1_2 = a.v_1_0 * b.v_0_2 + a.v_1_1 * b.v_1_2 + a.v_1_2 * b.v_2_2 + a.v_1_3 * 0;
        double new_v_1_3 = a.v_1_0 * b.v_0_3 + a.v_1_1 * b.v_1_3 + a.v_1_2 * b.v_2_3 + a.v_1_3 * 1;
        double new_v_2_0 = a.v_2_0 * b.v_0_0 + a.v_2_1 * b.v_1_0 + a.v_2_2 * b.v_2_0 + a.v_2_3 * 0;
        double new_v_2_1 = a.v_2_0 * b.v_0_1 + a.v_2_1 * b.v_1_1 + a.v_2_2 * b.v_2_1 + a.v_2_3 * 0;
        double new_v_2_2 = a.v_2_0 * b.v_0_2 + a.v_2_1 * b.v_1_2 + a.v_2_2 * b.v_2_2 + a.v_2_3 * 0;
        double new_v_2_3 = a.v_2_0 * b.v_0_3 + a.v_2_1 * b.v_1_3 + a.v_2_2 * b.v_2_3 + a.v_2_3 * 1;
        double new_v_3_0 = a.v_3_0 * b.v_0_0 + a.v_3_1 * b.v_1_0 + a.v_3_2 * b.v_2_0 + a.v_3_3 * 0;
        double new_v_3_1 = a.v_3_0 * b.v_0_1 + a.v_3_1 * b.v_1_1 + a.v_3_2 * b.v_2_1 + a.v_3_3 * 0;
        double new_v_3_2 = a.v_3_0 * b.v_0_2 + a.v_3_1 * b.v_1_2 + a.v_3_2 * b.v_2_2 + a.v_3_3 * 0;
        double new_v_3_3 = a.v_3_0 * b.v_0_3 + a.v_3_1 * b.v_1_3 + a.v_3_2 * b.v_2_3 + a.v_3_3 * 1;
        return new NtMatrix4(new_v_0_0, new_v_0_1, new_v_0_2, new_v_0_3,
                new_v_1_0, new_v_1_1, new_v_1_2, new_v_1_3,
                new_v_2_0, new_v_2_1, new_v_2_2, new_v_2_3,
                new_v_3_0, new_v_3_1, new_v_3_2, new_v_3_3);
    }

    @Extension
    public static NtMatrix3 to_3(NtMatrix2 m) {
        return new NtMatrix3(
                m.v_0_0, m.v_0_1, 0,
                m.v_1_0, m.v_1_1, 0,
                0,       0,       1
        );
    }

    @Extension
    public static NtMatrix4 to_4(NtMatrix3 m) {
        return new NtMatrix4(
                m.v_0_0, m.v_0_1, m.v_0_2, 0,
                m.v_1_0, m.v_1_1, m.v_1_2, 0,
                m.v_2_0, m.v_2_1, m.v_2_2, 0,
                0,       0,       0,       1
        );
    }

    @Extension
    public static NtMatrix4 to_4(NtMatrix2 m) {
        return to_4(to_3(m));
    }

    @Extension
    public static NtMatrix4 to_4(NtMatrixH4 m) {
        return new NtMatrix4(
                m.v_0_0, m.v_0_1, m.v_0_2, m.v_0_3,
                m.v_1_0, m.v_1_1, m.v_1_2, m.v_1_3,
                m.v_2_0, m.v_2_1, m.v_2_2, m.v_2_3,
                0,       0,       0,       1
        );
    }

    @Extension
    public static NtMatrixH4 combine(NtMatrix3 rotation, NtVec3 translation) {
        return new NtMatrixH4(
                rotation.v_0_0, rotation.v_0_1, rotation.v_0_2, translation.x,
                rotation.v_1_0, rotation.v_1_1, rotation.v_1_2, translation.y,
                rotation.v_2_0, rotation.v_2_1, rotation.v_2_2, translation.z
        );
    }

    @Extension
    public static NtMatrix2 to_rotation(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new NtMatrix2(c, -s, s, c);
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix2.class) NtMaybe<NtMatrix2> to_rotation(@HiddenType(clazz = Double.class) NtMaybe<Double> radians) {
        if (radians.has()) {
            return new NtMaybe<>(to_rotation(radians.get()));
        }
        return new NtMaybe<>();
    }

    @Extension
    public static NtMatrix2 to_matrix(NtComplex x) {
        return new NtMatrix2(x.real, -x.imaginary, x.imaginary, x.imaginary);
    }

    @Extension
    public static NtMatrix3 to_rotation_x(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new NtMatrix3(
                1, 0, 0,
                0, c, -s,
                0, s, c
        );
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> to_rotation_x(@HiddenType(clazz = Double.class) NtMaybe<Double> radians) {
        if (radians.has()) {
            return new NtMaybe<>(to_rotation_x(radians.get()));
        }
        return new NtMaybe<>();
    }

    @Extension
    public static NtMatrix3 to_rotation_y(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new NtMatrix3(
                c, 0, s,
                0, 1, 0,
                -s, 0, c
        );
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> to_rotation_y(@HiddenType(clazz = Double.class) NtMaybe<Double> radians) {
        if (radians.has()) {
            return new NtMaybe<>(to_rotation_y(radians.get()));
        }
        return new NtMaybe<>();
    }

    @Extension
    public static NtMatrix3 to_rotation_z(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new NtMatrix3(
                c, -s, 0,
                s, c, 0,
                0, 0, 1
        );
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> to_rotation_z(@HiddenType(clazz = Double.class) NtMaybe<Double> radians) {
        if (radians.has()) {
            return new NtMaybe<>(to_rotation_z(radians.get()));
        }
        return new NtMaybe<>();
    }

    @Extension
    public static NtMatrix3 to_rotation_around(double radians, NtVec3 axis) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        double t = 1 - c;

        // Normalize axis
        double len = Math.sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z);
        double ux = axis.x / len;
        double uy = axis.y / len;
        double uz = axis.z / len;

        double v_0_0 = c + ux * ux * t;
        double v_0_1 = ux * uy * t - uz * s;
        double v_0_2 = ux * uz * t + uy * s;

        double v_1_0 = uy * ux * t + uz * s;
        double v_1_1 = c + uy * uy * t;
        double v_1_2 = uy * uz * t - ux * s;

        double v_2_0 = uz * ux * t - uy * s;
        double v_2_1 = uz * uy * t + ux * s;
        double v_2_2 = c + uz * uz * t;

        return new NtMatrix3(
                v_0_0, v_0_1, v_0_2,
                v_1_0, v_1_1, v_1_2,
                v_2_0, v_2_1, v_2_2
        );
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> to_rotation_around(@HiddenType(clazz = Double.class) NtMaybe<Double> radians, NtVec3 axis) {
        if (radians.has()) {
            return new NtMaybe<>(to_rotation_around(radians.get(), axis));
        }
        return new NtMaybe<>();
    }

    @Extension
    public static NtMatrix3 rotate_around(NtVec3 axis, double radians) {
        return to_rotation_around(radians, axis);
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> rotate_around(NtVec3 axis, @HiddenType(clazz = Double.class) NtMaybe<Double> radians) {
        if (radians.has()) {
            return new NtMaybe<>(to_rotation_around(radians.get(), axis));
        }
        return new NtMaybe<>();
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix2.class) NtMaybe<NtMatrix2> inverse(NtMatrix2 m) {
        // Compute determinant: ad - bc
        double det = m.v_0_0 * m.v_1_1 - m.v_0_1 * m.v_1_0;
        // Check if determinant is zero or non-finite (NaN or infinity)
        if (det == 0.0 || !Double.isFinite(det)) {
            return new NtMaybe<>();
        }
        // Compute inverse: 1/det * [d, -b; -c, a]
        double invDet = 1.0 / det;
        double new_v_0_0 = m.v_1_1 * invDet;
        double new_v_0_1 = -m.v_0_1 * invDet;
        double new_v_1_0 = -m.v_1_0 * invDet;
        double new_v_1_1 = m.v_0_0 * invDet;
        return new NtMaybe<>(new NtMatrix2(new_v_0_0, new_v_0_1, new_v_1_0, new_v_1_1));
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> inverse(NtMatrix3 m) {
        // Compute determinant
        double det = m.v_0_0 * (m.v_1_1 * m.v_2_2 - m.v_1_2 * m.v_2_1)
                - m.v_0_1 * (m.v_1_0 * m.v_2_2 - m.v_1_2 * m.v_2_0)
                + m.v_0_2 * (m.v_1_0 * m.v_2_1 - m.v_1_1 * m.v_2_0);

        // Check if determinant is zero or non-finite
        if (det == 0.0 || !Double.isFinite(det)) {
            return new NtMaybe<>();
        }

        // Compute inverse: adjugate matrix scaled by 1/det
        double invDet = 1.0 / det;
        double new_v_0_0 = (m.v_1_1 * m.v_2_2 - m.v_1_2 * m.v_2_1) * invDet;
        double new_v_0_1 = -(m.v_0_1 * m.v_2_2 - m.v_0_2 * m.v_2_1) * invDet;
        double new_v_0_2 = (m.v_0_1 * m.v_1_2 - m.v_0_2 * m.v_1_1) * invDet;
        double new_v_1_0 = -(m.v_1_0 * m.v_2_2 - m.v_1_2 * m.v_2_0) * invDet;
        double new_v_1_1 = (m.v_0_0 * m.v_2_2 - m.v_0_2 * m.v_2_0) * invDet;
        double new_v_1_2 = -(m.v_0_0 * m.v_1_2 - m.v_0_2 * m.v_1_0) * invDet;
        double new_v_2_0 = (m.v_1_0 * m.v_2_1 - m.v_1_1 * m.v_2_0) * invDet;
        double new_v_2_1 = -(m.v_0_0 * m.v_2_1 - m.v_0_1 * m.v_2_0) * invDet;
        double new_v_2_2 = (m.v_0_0 * m.v_1_1 - m.v_0_1 * m.v_1_0) * invDet;

        return new NtMaybe<>(new NtMatrix3(new_v_0_0, new_v_0_1, new_v_0_2,
                new_v_1_0, new_v_1_1, new_v_1_2,
                new_v_2_0, new_v_2_1, new_v_2_2));
    }

    @Extension
    public static @HiddenType(clazz = NtMatrixH4.class) NtMaybe<NtMatrixH4> inverse(NtMatrixH4 m) {
        // Extract the 3x3 rotation submatrix
        NtMatrix3 rotation = new NtMatrix3(
                m.v_0_0, m.v_0_1, m.v_0_2,
                m.v_1_0, m.v_1_1, m.v_1_2,
                m.v_2_0, m.v_2_1, m.v_2_2
        );

        // Compute the inverse of the rotation matrix
        NtMaybe<NtMatrix3> invRotationMaybe = inverse(rotation);
        if (!invRotationMaybe.has()) {
            return new NtMaybe<>();
        }
        NtMatrix3 invRotation = invRotationMaybe.get();

        // Compute the new translation: -R^-1 * T
        double transX = m.v_0_3;
        double transY = m.v_1_3;
        double transZ = m.v_2_3;
        double newTransX = -(invRotation.v_0_0 * transX + invRotation.v_0_1 * transY + invRotation.v_0_2 * transZ);
        double newTransY = -(invRotation.v_1_0 * transX + invRotation.v_1_1 * transY + invRotation.v_1_2 * transZ);
        double newTransZ = -(invRotation.v_2_0 * transX + invRotation.v_2_1 * transY + invRotation.v_2_2 * transZ);

        // Construct the inverse homogeneous matrix
        return new NtMaybe<>(new NtMatrixH4(
                invRotation.v_0_0, invRotation.v_0_1, invRotation.v_0_2, newTransX,
                invRotation.v_1_0, invRotation.v_1_1, invRotation.v_1_2, newTransY,
                invRotation.v_2_0, invRotation.v_2_1, invRotation.v_2_2, newTransZ
        ));
    }

    @Extension
    public static @HiddenType(clazz = NtMatrix4.class) NtMaybe<NtMatrix4> inverse(NtMatrix4 m) {
        // Compute determinant using minors
        double det =
                m.v_0_0 * (
                        m.v_1_1 * (m.v_2_2 * m.v_3_3 - m.v_2_3 * m.v_3_2) -
                                m.v_1_2 * (m.v_2_1 * m.v_3_3 - m.v_2_3 * m.v_3_1) +
                                m.v_1_3 * (m.v_2_1 * m.v_3_2 - m.v_2_2 * m.v_3_1)
                ) -
                        m.v_0_1 * (
                                m.v_1_0 * (m.v_2_2 * m.v_3_3 - m.v_2_3 * m.v_3_2) -
                                        m.v_1_2 * (m.v_2_0 * m.v_3_3 - m.v_2_3 * m.v_3_0) +
                                        m.v_1_3 * (m.v_2_0 * m.v_3_2 - m.v_2_2 * m.v_3_0)
                        ) +
                        m.v_0_2 * (
                                m.v_1_0 * (m.v_2_1 * m.v_3_3 - m.v_2_3 * m.v_3_1) -
                                        m.v_1_1 * (m.v_2_0 * m.v_3_3 - m.v_2_3 * m.v_3_0) +
                                        m.v_1_3 * (m.v_2_0 * m.v_3_1 - m.v_2_1 * m.v_3_0)
                        ) -
                        m.v_0_3 * (
                                m.v_1_0 * (m.v_2_1 * m.v_3_2 - m.v_2_2 * m.v_3_1) -
                                        m.v_1_1 * (m.v_2_0 * m.v_3_2 - m.v_2_2 * m.v_3_0) +
                                        m.v_1_2 * (m.v_2_0 * m.v_3_1 - m.v_2_1 * m.v_3_0)
                        );

        // Check if determinant is zero or non-finite
        if (det == 0.0 || !Double.isFinite(det)) {
            return new NtMaybe<>();
        }

        // Compute cofactors for the adjugate matrix
        double c_0_0 = (m.v_1_1 * (m.v_2_2 * m.v_3_3 - m.v_2_3 * m.v_3_2) - m.v_1_2 * (m.v_2_1 * m.v_3_3 - m.v_2_3 * m.v_3_1) + m.v_1_3 * (m.v_2_1 * m.v_3_2 - m.v_2_2 * m.v_3_1));
        double c_0_1 = -(m.v_1_0 * (m.v_2_2 * m.v_3_3 - m.v_2_3 * m.v_3_2) - m.v_1_2 * (m.v_2_0 * m.v_3_3 - m.v_2_3 * m.v_3_0) + m.v_1_3 * (m.v_2_0 * m.v_3_2 - m.v_2_2 * m.v_3_0));
        double c_0_2 = (m.v_1_0 * (m.v_2_1 * m.v_3_3 - m.v_2_3 * m.v_3_1) - m.v_1_1 * (m.v_2_0 * m.v_3_3 - m.v_2_3 * m.v_3_0) + m.v_1_3 * (m.v_2_0 * m.v_3_1 - m.v_2_1 * m.v_3_0));
        double c_0_3 = -(m.v_1_0 * (m.v_2_1 * m.v_3_2 - m.v_2_2 * m.v_3_1) - m.v_1_1 * (m.v_2_0 * m.v_3_2 - m.v_2_2 * m.v_3_0) + m.v_1_2 * (m.v_2_0 * m.v_3_1 - m.v_2_1 * m.v_3_0));
        double c_1_0 = -(m.v_0_1 * (m.v_2_2 * m.v_3_3 - m.v_2_3 * m.v_3_2) - m.v_0_2 * (m.v_2_1 * m.v_3_3 - m.v_2_3 * m.v_3_1) + m.v_0_3 * (m.v_2_1 * m.v_3_2 - m.v_2_2 * m.v_3_1));
        double c_1_1 = (m.v_0_0 * (m.v_2_2 * m.v_3_3 - m.v_2_3 * m.v_3_2) - m.v_0_2 * (m.v_2_0 * m.v_3_3 - m.v_2_3 * m.v_3_0) + m.v_0_3 * (m.v_2_0 * m.v_3_2 - m.v_2_2 * m.v_3_0));
        double c_1_2 = -(m.v_0_0 * (m.v_2_1 * m.v_3_3 - m.v_2_3 * m.v_3_1) - m.v_0_1 * (m.v_2_0 * m.v_3_3 - m.v_2_3 * m.v_3_0) + m.v_0_3 * (m.v_2_0 * m.v_3_1 - m.v_2_1 * m.v_3_0));
        double c_1_3 = (m.v_0_0 * (m.v_2_1 * m.v_3_2 - m.v_2_2 * m.v_3_1) - m.v_0_1 * (m.v_2_0 * m.v_3_2 - m.v_2_2 * m.v_3_0) + m.v_0_2 * (m.v_2_0 * m.v_3_1 - m.v_2_1 * m.v_3_0));
        double c_2_0 = (m.v_0_1 * (m.v_1_2 * m.v_3_3 - m.v_1_3 * m.v_3_2) - m.v_0_2 * (m.v_1_1 * m.v_3_3 - m.v_1_3 * m.v_3_1) + m.v_0_3 * (m.v_1_1 * m.v_3_2 - m.v_1_2 * m.v_3_1));
        double c_2_1 = -(m.v_0_0 * (m.v_1_2 * m.v_3_3 - m.v_1_3 * m.v_3_2) - m.v_0_2 * (m.v_1_0 * m.v_3_3 - m.v_1_3 * m.v_3_0) + m.v_0_3 * (m.v_1_0 * m.v_3_2 - m.v_1_2 * m.v_3_0));
        double c_2_2 = (m.v_0_0 * (m.v_1_1 * m.v_3_3 - m.v_1_3 * m.v_3_1) - m.v_0_1 * (m.v_1_0 * m.v_3_3 - m.v_1_3 * m.v_3_0) + m.v_0_3 * (m.v_1_0 * m.v_3_1 - m.v_1_1 * m.v_3_0));
        double c_2_3 = -(m.v_0_0 * (m.v_1_1 * m.v_3_2 - m.v_1_2 * m.v_3_1) - m.v_0_1 * (m.v_1_0 * m.v_3_2 - m.v_1_2 * m.v_3_0) + m.v_0_2 * (m.v_1_0 * m.v_3_1 - m.v_1_1 * m.v_3_0));
        double c_3_0 = -(m.v_0_1 * (m.v_1_2 * m.v_2_3 - m.v_1_3 * m.v_2_2) - m.v_0_2 * (m.v_1_1 * m.v_2_3 - m.v_1_3 * m.v_2_1) + m.v_0_3 * (m.v_1_1 * m.v_2_2 - m.v_1_2 * m.v_2_1));
        double c_3_1 = (m.v_0_0 * (m.v_1_2 * m.v_2_3 - m.v_1_3 * m.v_2_2) - m.v_0_2 * (m.v_1_0 * m.v_2_3 - m.v_1_3 * m.v_2_0) + m.v_0_3 * (m.v_1_0 * m.v_2_2 - m.v_1_2 * m.v_2_0));
        double c_3_2 = -(m.v_0_0 * (m.v_1_1 * m.v_2_3 - m.v_1_3 * m.v_2_1) - m.v_0_1 * (m.v_1_0 * m.v_2_3 - m.v_1_3 * m.v_2_0) + m.v_0_3 * (m.v_1_0 * m.v_2_1 - m.v_1_1 * m.v_2_0));
        double c_3_3 = (m.v_0_0 * (m.v_1_1 * m.v_2_2 - m.v_1_2 * m.v_2_1) - m.v_0_1 * (m.v_1_0 * m.v_2_2 - m.v_1_2 * m.v_2_0) + m.v_0_2 * (m.v_1_0 * m.v_2_1 - m.v_1_1 * m.v_2_0));

        // Adjugate is transpose of cofactor matrix
        double invDet = 1.0 / det;
        double new_v_0_0 = c_0_0 * invDet;
        double new_v_0_1 = c_1_0 * invDet;
        double new_v_0_2 = c_2_0 * invDet;
        double new_v_0_3 = c_3_0 * invDet;
        double new_v_1_0 = c_0_1 * invDet;
        double new_v_1_1 = c_1_1 * invDet;
        double new_v_1_2 = c_2_1 * invDet;
        double new_v_1_3 = c_3_1 * invDet;
        double new_v_2_0 = c_0_2 * invDet;
        double new_v_2_1 = c_1_2 * invDet;
        double new_v_2_2 = c_2_2 * invDet;
        double new_v_2_3 = c_3_2 * invDet;
        double new_v_3_0 = c_0_3 * invDet;
        double new_v_3_1 = c_1_3 * invDet;
        double new_v_3_2 = c_2_3 * invDet;
        double new_v_3_3 = c_3_3 * invDet;

        return new NtMaybe<>(new NtMatrix4(
                new_v_0_0, new_v_0_1, new_v_0_2, new_v_0_3,
                new_v_1_0, new_v_1_1, new_v_1_2, new_v_1_3,
                new_v_2_0, new_v_2_1, new_v_2_2, new_v_2_3,
                new_v_3_0, new_v_3_1, new_v_3_2, new_v_3_3
        ));
    }

    @Extension
    @UseName(name="inverse")
    public static @HiddenType(clazz = NtMatrix2.class) NtMaybe<NtMatrix2> inverse2( @HiddenType(clazz = NtMatrix2.class) NtMaybe<NtMatrix2> mm) {
        if (mm.has()) {
            return inverse(mm.get());
        }
        return mm;
    }
    @Extension
    @UseName(name="inverse")
    public static @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> inverse3( @HiddenType(clazz = NtMatrix3.class) NtMaybe<NtMatrix3> mm) {
        if (mm.has()) {
            return inverse(mm.get());
        }
        return mm;
    }
    @Extension
    @UseName(name="inverse")
    public static @HiddenType(clazz = NtMatrix4.class) NtMaybe<NtMatrix4> inverse4( @HiddenType(clazz = NtMatrix4.class) NtMaybe<NtMatrix4> mm) {
        if (mm.has()) {
            return inverse(mm.get());
        }
        return mm;
    }
    @Extension
    @UseName(name="inverse")
    public static @HiddenType(clazz = NtMatrixH4.class) NtMaybe<NtMatrixH4> inverseH4( @HiddenType(clazz = NtMatrixH4.class) NtMaybe<NtMatrixH4> mm) {
        if (mm.has()) {
            return inverse(mm.get());
        }
        return mm;
    }
}
