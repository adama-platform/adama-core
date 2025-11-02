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

import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtVec2;
import ape.runtime.natives.NtVec3;
import ape.runtime.natives.NtVec4;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;
import ape.translator.reflect.Skip;

/** library of vector math */
public class LibVector {
    @Skip
    public static double dot(NtVec2 a, NtVec2 b) {
        return a.x * b.x + a.y * b.y;
    }
    @Skip
    public static double dot(NtVec3 a, NtVec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
    @Skip
    public static double dot(NtVec4 a, NtVec4 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    @Skip
    public static NtVec2 add(NtVec2 a, NtVec2 b) {
        return new NtVec2(a.x + b.x, a.y + b.y);
    }
    @Skip
    public static NtVec3 add(NtVec3 a, NtVec3 b) {
        return new NtVec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }
    @Skip
    public static NtVec4 add(NtVec4 a, NtVec4 b) {
        return new NtVec4(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w);
    }

    @Skip
    public static NtVec2 sub(NtVec2 a, NtVec2 b) {
        return new NtVec2(a.x - b.x, a.y - b.y);
    }
    @Skip
    public static NtVec3 sub(NtVec3 a, NtVec3 b) {
        return new NtVec3(a.x - b.x, a.y - b.y, a.z - b.z);
    }
    @Skip
    public static NtVec4 sub(NtVec4 a, NtVec4 b) {
        return new NtVec4(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w);
    }

    @Skip
    public static NtVec2 scale(double s, NtVec2 v) {
        return new NtVec2(v.x * s, v.y * s);
    }
    @Skip
    public static NtVec3 scale(double s, NtVec3 v) {
        return new NtVec3(v.x * s, v.y * s, v.z * s);
    }
    @Skip
    public static NtVec4 scale(double s, NtVec4 v) {
        return new NtVec4(v.x * s, v.y * s, v.z * s, v.w * s);
    }

    @Skip
    public static NtVec3 cross(NtVec3 a, NtVec3 b) {
        double x = a.y * b.z - a.z * b.y;
        double y = a.z * b.x - a.x * b.z;
        double z = a.x * b.y - a.y * b.x;
        return new NtVec3(x, y, z);
    }

    @Skip
    public static double cross(NtVec2 a, NtVec2 b) {
        return a.x * b.y - a.y * b.x;
    }

    @Extension
    public static double length(NtVec2 x) {
        return Math.sqrt(dot(x, x));
    }

    @Extension
    public static double length(NtVec3 x) {
        return Math.sqrt(dot(x, x));
    }

    @Extension
    public static double length(NtVec4 x) {
        return Math.sqrt(dot(x, x));
    }

    @Extension
    public static @HiddenType(clazz = NtVec2.class) NtMaybe<NtVec2> normalize(NtVec2 v) {
        double len = length(v);
        if (len > 1e-10) {
            return new NtMaybe<>(new NtVec2(v.x / len, v.y / len));
        }
        return new NtMaybe<>(); // zero vector returned
    }

    @Extension
    public static @HiddenType(clazz = NtVec3.class) NtMaybe<NtVec3> normalize(NtVec3 v) {
        double len = length(v);
        if (len > 1e-10) {
            return new NtMaybe<>(new NtVec3(v.x / len, v.y / len, v.z / len));
        }
        return new NtMaybe<>(v);
    }

    @Extension
    public static NtVec3 flipToUp(NtVec3 v) {
        if (v.y < 0) {
            return new NtVec3(v.x, -v.y, v.z);
        }
        return v;
    }

    @Extension
    public static NtVec2 cmult(NtVec2 a, NtVec2 b) {
        return new NtVec2(a.x * b.x, a.y * b.y);
    }

    @Extension
    public static NtVec3 cmult(NtVec3 a, NtVec3 b) {
        return new NtVec3(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    @Extension
    public static NtVec4 cmult(NtVec4 a, NtVec4 b) {
        return new NtVec4(a.x * b.x, a.y * b.y, a.z * b.z, a.w * b.w);
    }
}
