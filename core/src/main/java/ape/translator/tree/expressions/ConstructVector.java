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
import ape.translator.tree.types.natives.TyNativeDouble;
import ape.translator.tree.types.natives.TyNativeVec2;
import ape.translator.tree.types.natives.TyNativeVec3;
import ape.translator.tree.types.natives.TyNativeVec4;

import java.util.function.Consumer;

/** expression to construct a vector */
public class ConstructVector extends Expression {
    private final Token intro;
    private final Token open;
    private final Expression x;
    private final Token pipe1;
    private final Expression y;
    private final Token pipe2;
    private final Expression z;
    private final Token pipe3;
    private final Expression w;
    private final Token close;

    public ConstructVector(Token intro, Token open, Expression x, Token pipe1, Expression y, Token pipe2, Expression z, Token pipe3, Expression w, Token close) {
        this.intro = intro;
        this.open = open;
        this.x = x;
        this.pipe1 = pipe1;
        this.y = y;
        this.pipe2 = pipe2;
        this.z = z;
        this.pipe3 = pipe3;
        this.w = w;
        this.close = close;
        ingest(open);
        ingest(close);
    }

    @Override
    public void emit(Consumer<Token> yielder) {
        yielder.accept(intro);
        yielder.accept(open);
        x.emit(yielder);
        yielder.accept(pipe1);
        y.emit(yielder);
        if (pipe2 != null) {
            yielder.accept(pipe2);
            z.emit(yielder);
            if (pipe3 != null) {
                yielder.accept(pipe3);
                w.emit(yielder);
            }
        }
        yielder.accept(close);
    }

    @Override
    public void format(Formatter formatter) {
        x.format(formatter);
        y.format(formatter);
        if (z != null) {
            z.format(formatter);
        }
        if (w != null) {
            w.format(formatter);
        }
    }

    @Override
    protected TyType typingInternal(Environment environment, TyType suggestion) {
        TyNativeDouble suggestChild = new TyNativeDouble(TypeBehavior.ReadOnlyGetNativeValue, null, open);
        environment.rules.IsNumeric(x.typingInternal(environment, suggestChild), false);
        environment.rules.IsNumeric(y.typingInternal(environment, suggestChild), false);
        if (pipe2 != null) {
            environment.rules.IsNumeric(z.typingInternal(environment, suggestChild), false);
            if (pipe3 != null) {
                environment.rules.IsNumeric(w.typingInternal(environment, suggestChild), false);
                return new TyNativeVec4(TypeBehavior.ReadOnlyGetNativeValue, null, open);
            } else {
                return new TyNativeVec3(TypeBehavior.ReadOnlyGetNativeValue, null, open);
            }
        } else {
            return new TyNativeVec2(TypeBehavior.ReadOnlyGetNativeValue, null, open);
        }
    }

    @Override
    public void free(FreeEnvironment environment) {
        x.free(environment);
        y.free(environment);
        if (z != null) {
            z.free(environment);
        }
        if (w != null) {
            w.free(environment);
        }
    }

    @Override
    public void writeJava(StringBuilder sb, Environment environment) {
        if (pipe2 != null) {
            if (pipe3 != null) {
                sb.append("new NtVec4(");
                x.writeJava(sb, environment);
                sb.append(", ");
                y.writeJava(sb, environment);
                sb.append(", ");
                z.writeJava(sb, environment);
                sb.append(", ");
                w.writeJava(sb, environment);
                sb.append(")");
            } else {
                sb.append("new NtVec3(");
                x.writeJava(sb, environment);
                sb.append(", ");
                y.writeJava(sb, environment);
                sb.append(", ");
                z.writeJava(sb, environment);
                sb.append(")");
            }
        } else {
            sb.append("new NtVec2(");
            x.writeJava(sb, environment);
            sb.append(", ");
            y.writeJava(sb, environment);
            sb.append(")");
        }
    }

    public static ConstructVector INVENT(Token token, double x, double y, Double z, Double w) {
        if (z == null) {
            return new ConstructVector(token, token,
                    new DoubleConstant(Token.WRAP("" + x), x), token,
                    new DoubleConstant(Token.WRAP("" + y), y), null,
                    null, null,
                    null,
                    token);
        } else {
            if (w == null) {
                return new ConstructVector(token, token,
                        new DoubleConstant(Token.WRAP("" + x), x), token,
                        new DoubleConstant(Token.WRAP("" + y), y), token,
                        new DoubleConstant(Token.WRAP("" + z), z), null,
                        null,
                        token);
            } else {
                return new ConstructVector(token, token,
                        new DoubleConstant(Token.WRAP("" + x), x), token,
                        new DoubleConstant(Token.WRAP("" + y), y), token,
                        new DoubleConstant(Token.WRAP("" + z), z), token,
                        new DoubleConstant(Token.WRAP("" + w), w), token);
            }
        }
    }
}
