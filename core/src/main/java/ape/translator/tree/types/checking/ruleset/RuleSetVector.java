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
package ape.translator.tree.types.checking.ruleset;

import ape.translator.env.Environment;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.TyNativeVec2;
import ape.translator.tree.types.natives.TyNativeVec3;
import ape.translator.tree.types.natives.TyNativeVec4;
import ape.translator.tree.types.reactive.TyReactiveVec2;
import ape.translator.tree.types.reactive.TyReactiveVec3;
import ape.translator.tree.types.reactive.TyReactiveVec4;

public class RuleSetVector {
    public static boolean IsVec2(final Environment environment, final TyType tyTypeOriginal) {
        var tyType =  RuleSetCommon.Resolve(environment, tyTypeOriginal, true);
        return tyType instanceof TyNativeVec2 || tyType instanceof TyReactiveVec2;
    }
    public static boolean IsVec3(final Environment environment, final TyType tyTypeOriginal) {
        var tyType =  RuleSetCommon.Resolve(environment, tyTypeOriginal, true);
        return tyType instanceof TyNativeVec3 || tyType instanceof TyReactiveVec3;
    }
    public static boolean IsVec4(final Environment environment, final TyType tyTypeOriginal) {
        var tyType =  RuleSetCommon.Resolve(environment, tyTypeOriginal, true);
        return tyType instanceof TyNativeVec4 || tyType instanceof TyReactiveVec4;
    }
    public static boolean IsVector(final Environment environment, final TyType tyTypeOriginal) {
        return RuleSetVector.IsVec2(environment, tyTypeOriginal) || RuleSetVector.IsVec3(environment, tyTypeOriginal) || RuleSetVector.IsVec4(environment, tyTypeOriginal);
    }
}
