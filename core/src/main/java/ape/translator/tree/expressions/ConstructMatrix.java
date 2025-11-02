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
package ape.translator.tree.expressions;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.expressions.constants.DoubleConstant;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.*;

import java.util.ArrayList;
import java.util.function.Consumer;

/** expression to construct a matrix */
public class ConstructMatrix extends Expression {
    public static class Fragment {
        public final Expression expression;
        public final Token token;

        public Fragment(Expression expression, Token token) {
            this.expression = expression;
            this.token = token;
        }
    }

    public final Token token;
    public final Token open;
    public final Fragment[] fragments;

    public ConstructMatrix(Token token, Token open, Fragment... fragments) {
        this.token = token;
        this.open = open;
        this.fragments = fragments;
        ingest(token);
        ingest(open);
        for (Fragment fragment : fragments) {
            ingest(fragment.token);
        }
    }

    @Override
    public void emit(Consumer<Token> yielder) {
        yielder.accept(token);
        yielder.accept(open);
        for (Fragment fragment : fragments) {
            fragment.expression.emit(yielder);
            yielder.accept(fragment.token);
        }
    }

    @Override
    public void format(Formatter formatter) {
        for (Fragment fragment : fragments) {
            fragment.expression.format(formatter);
        }
    }

    @Override
    protected TyType typingInternal(Environment environment, TyType suggestion) {
        TyNativeDouble suggestChild = new TyNativeDouble(TypeBehavior.ReadOnlyGetNativeValue, null, open);
        for (Fragment fragment : fragments) {
            TyType fragmentType = fragment.expression.typingInternal(environment, suggestChild);
            environment.rules.IsNumeric(fragmentType, false);
        }
        switch (fragments.length) {
            case 4:
                return new TyNativeMatrix2(TypeBehavior.ReadOnlyGetNativeValue, null, open);
            case 9:
                return new TyNativeMatrix3(TypeBehavior.ReadOnlyGetNativeValue, null, open);
            case 12:
                return new TyNativeMatrixH4(TypeBehavior.ReadOnlyGetNativeValue, null, open);
            case 16:
                return new TyNativeMatrix4(TypeBehavior.ReadOnlyGetNativeValue, null, open);
        }
        environment.document.createError(this, "a constructed matrix does not have the right number of elements");
        return null;
    }

    @Override
    public void free(FreeEnvironment environment) {
        for (Fragment fragment : fragments) {
            fragment.expression.free(environment);
        }
    }

    @Override
    public void writeJava(StringBuilder sb, Environment environment) {
        sb.append("new ");
        switch (fragments.length) {
            case 4:
                sb.append("NtMatrix2");
                break;
            case 9:
                sb.append("NtMatrix3");
                break;
            case 12:
                sb.append("NtMatrixH4");
                break;
            case 16:
                sb.append("NtMatrix4");
                break;
        }
        sb.append("(");
        boolean notFirst = false;
        for (Fragment fragment : fragments) {
            if (notFirst) {
                sb.append(", ");
            }
            notFirst = true;
            fragment.expression.writeJava(sb, environment);
        }
        sb.append(")");
    }

    public static ConstructMatrix INVENT(Token token, double... values) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        for (double value : values) {
            fragments.add(new Fragment(new DoubleConstant(Token.WRAP("" + value), value), token));
        }
        return new ConstructMatrix(token, token, fragments.toArray(new Fragment[fragments.size()]));
    }
}
