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
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.TyType;

import java.util.ArrayList;
import java.util.function.Consumer;

public class AnonymousTuple extends Expression {
  private final AnonymousObject anonymousObject;

  public static class PrefixedExpression {
    public final Token token;
    public final Expression expression;

    public PrefixedExpression(Token token, Expression expression) {
      this.token = token;
      this.expression = expression;
    }
  }

  private final ArrayList<PrefixedExpression> expressions;
  private Token suffix;

  public AnonymousTuple() {
    this.anonymousObject = new AnonymousObject(Token.WRAP("{"));
    this.expressions = new ArrayList<>();
    this.suffix = null;
  }

  public static String nameOf(int priorSize) {
    switch (priorSize) {
      case 0:
        return "first";
      case 1:
        return "second";
      case 2:
        return "third";
      case 3:
        return "fourth";
      case 4:
        return "fifth";
      case 5:
        return "sixth";
      case 6:
        return "seventh";
      case 7:
        return "eighth";
      case 8:
        return "ninth";
      case 9:
        return "tenth";
      default:
        return "pos_" + (priorSize + 1);
    }
  }

  public void add(Token token, Expression expr) {
    Token name = expr.asIdentiferToken(token.sourceName, nameOf(expressions.size()));
    expressions.add(new PrefixedExpression(token, expr));
    anonymousObject.add(token, name, token, expr);
  }

  public void finish(Token token) {
    this.suffix = token;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    for (PrefixedExpression pe : expressions) {
      yielder.accept(pe.token);
      pe.expression.emit(yielder);
    }
    yielder.accept(suffix);
  }

  @Override
  public void format(Formatter formatter) {
    for (PrefixedExpression pe : expressions) {
      pe.expression.format(formatter);
    }
  }

  @Override
  protected TyType typingInternal(Environment environment, TyType suggestion) {
    return anonymousObject.typingInternal(environment, suggestion);
  }

  @Override
  public void writeJava(StringBuilder sb, Environment environment) {
    anonymousObject.writeJava(sb, environment);
  }

  @Override
  public void free(FreeEnvironment environment) {
    for(var expr : expressions) {
      expr.expression.free(environment);
    }
  }
}
