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
package ape.translator.tree.definitions.web;

import ape.common.web.UriMatcher;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.definitions.Definition;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.natives.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class Uri extends Definition {
  private final ArrayList<Consumer<Consumer<Token>>> emission;
  public final TreeMap<String, TyType> variables;
  private final ArrayList<Function<UriTable.UriLevel, UriTable.UriLevel>> next;
  private final StringBuilder str;
  private final StringBuilder rxhtmlPath;
  private final ArrayList<Function<String, Boolean>> matchers;
  private boolean lastHasStar;
  private StringBuilder perfSuffix;

  public Uri() {
    this.emission = new ArrayList<>();
    this.variables = new TreeMap<>();
    this.next = new ArrayList<>();
    this.str = new StringBuilder();
    this.rxhtmlPath = new StringBuilder();
    this.matchers = new ArrayList<>();
    this.lastHasStar = false;
    this.perfSuffix = new StringBuilder();
  }

  public void push(Token slash, Token dollarSign, Token id, Token starToken, Token colon, TyType type) {
    perfSuffix.append("_");
    ingest(slash);
    emission.add((y) -> y.accept(slash));
    str.append("/");
    rxhtmlPath.append("/");
    if (id != null) {
      ingest(id);
      if (dollarSign != null) {
        emission.add((y) -> y.accept(dollarSign));
        str.append("$");
        rxhtmlPath.append("$");
      }
      emission.add((y) -> y.accept(id));
      if (starToken != null) {
        emission.add((y) -> y.accept(starToken));
      }
      String uriFragment = id.stripStringLiteral().text;
      str.append(uriFragment);
      rxhtmlPath.append(uriFragment);
      if (colon != null) {
        ingest(type);
        emission.add((y) -> y.accept(colon));
        emission.add((y) -> type.emit(y));
        variables.put(id.text, type);
        str.append(":");
        str.append(type.getAdamaType());
        rxhtmlPath.append(":");
        switch (type.getAdamaType()) {
          case "int":
          case "double":
          case "long":
            rxhtmlPath.append("number");
            perfSuffix.append("number");
            break;
          default:
            rxhtmlPath.append("text");
            perfSuffix.append("text");
        }
      } else {
        perfSuffix.append(id.stripStringLiteral().text);
      }
      if (starToken == null) {
        if (dollarSign != null) {
            if (type instanceof TyNativeBoolean) {
              matchers.add((str) -> str.equals("true") || str.equals("false"));
              next.add((level) -> level.next(id.text, level.bools));
            } else if (type instanceof TyNativeInteger) {
              matchers.add(Uri::isInteger);
              next.add((level) -> level.next(id.text, level.ints));
            } else if (type instanceof TyNativeLong) {
              matchers.add(Uri::isLong);
              next.add((level) -> level.next(id.text, level.longs));
            } else if (type instanceof TyNativeDouble) {
              matchers.add(Uri::isDouble);
              next.add((level) -> level.next(id.text, level.doubles));
            } else if (type instanceof TyNativeString) {
              matchers.add((str) -> true);
              next.add((level) -> level.next(id.text, level.strings));
          }
        } else {
          String fixed = id.stripStringLiteral().text;
          matchers.add((str) -> fixed.equals(str));
          next.add((level) -> level.next(fixed, level.fixed));
        }
      } else {
        lastHasStar = true;
        matchers.add((str) -> true);
        str.append("*");
        variables.put(id.text, new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("string")));
        next.add((level) -> level.next(id.text, level.strings).tail());
      }
    } else {
      matchers.add((str) -> "".equals(str));
      next.add((level) -> level.next("", level.fixed));
    }
  }

  public UriMatcher matcher() {
    return new UriMatcher(str.toString(), matchers, lastHasStar);
  }

  public void extendInto(Environment environment) {
    for (Map.Entry<String, TyType> var : variables.entrySet()) {
      environment.define(var.getKey(), var.getValue(), true, this);
    }
  }

  public UriTable.UriLevel dive(UriTable.UriLevel root) {
    UriTable.UriLevel level = root;
    for (Function<UriTable.UriLevel, UriTable.UriLevel> fn : next) {
      level = fn.apply(level);
    }
    return level;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    for (Consumer<Consumer<Token>> emit : emission) {
      emit.accept(yielder);
    }
  }

  @Override
  public void format(Formatter formatter) {
  }

  public void typing(Environment environment) {
    for (Map.Entry<String, TyType> var : variables.entrySet()) {
      TyType typeToCheck = var.getValue();
      boolean valid = typeToCheck instanceof TyNativeInteger || typeToCheck instanceof TyNativeDouble || typeToCheck instanceof TyNativeLong || typeToCheck instanceof TyNativeString || typeToCheck instanceof TyNativeBoolean;
      if (!valid) {
        environment.document.createError(this, "The parameter type must be int, long, double, string, or boolean");
      }
    }
  }

  @Override
  public String toString() {
    return str.toString();
  }

  public String rxhtmlPath() {
    return this.rxhtmlPath.toString();
  }

  public static boolean isInteger(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }


  public static boolean isLong(String str) {
    try {
      Long.parseLong(str);
      return true;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }

  public static boolean isDouble(String str) {
    try {
      Double.parseDouble(str);
      return true;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }

  public String toPerfSuffix() {
    return perfSuffix.toString();
  }
}
