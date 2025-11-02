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
package ape.translator.tree.types.natives;

import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.expressions.ConstructVector;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;

public class TyNativeVec3 extends TyNativeProxyString {

    public TyNativeVec3(TypeBehavior behavior, Token readonlyToken, Token token) {
        super(behavior, readonlyToken, token, "NtVec3", "vec3");
    }

    @Override
    public TyType makeCopyWithNewPositionInternal(DocumentPosition position, TypeBehavior newBehavior) {
        return new TyNativeVec3(newBehavior, readonlyToken, token).withPosition(position);
    }

    @Override
    public Expression inventDefaultValueExpression(DocumentPosition forWhatExpression) {
        return ConstructVector.INVENT(token, 0.0, 0.0, 0.0, null);
    }

    @Override
    public TyNativeFunctional lookupMethod(String name, Environment environment) {
        TyNativeFunctional fieldLookup;
        fieldLookup = TyNativeVec4.commonVectorMethod(token, this, name, "x");
        if (fieldLookup != null) {
            return fieldLookup;
        }
        fieldLookup = TyNativeVec4.commonVectorMethod(token, this, name, "y");
        if (fieldLookup != null) {
            return fieldLookup;
        }
        fieldLookup = TyNativeVec4.commonVectorMethod(token, this, name, "z");
        if (fieldLookup != null) {
            return fieldLookup;
        }
        return environment.state.globals.findExtension(this, name);
    }
}
