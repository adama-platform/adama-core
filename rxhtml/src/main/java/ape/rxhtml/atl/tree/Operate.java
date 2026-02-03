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
package ape.rxhtml.atl.tree;

import ape.rxhtml.atl.Context;
import ape.rxhtml.atl.ParseException;
import ape.rxhtml.atl.Parser;
import ape.rxhtml.typing.ViewScope;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * ATL tree node for comparison operations between two expressions.
 * Supports equality (=), inequality (!=), and ordering (<, >, <=, >=).
 * Used in conditionals: [count>0]has items[/count]
 */
public class Operate implements Tree {
  public static final String[] OPERATORS = new String[] { "<=", ">=", "!=", "<", ">", "=" };

  public static String convertOp(String op) {
    if ("=".equals(op)) {
      return "==";
    }
    return op;
  }

  public final Tree tree;
  public final Tree value;
  public final String operator;

  public Operate(Tree tree, String value, String operator) throws ParseException  {
    this.tree = tree;
    this.value = Parser.parse(value);
    this.operator = operator;
  }

  @Override
  public Map<String, String> variables() {
    TreeMap<String, String> union = new TreeMap<>();
    union.putAll(tree.variables());
    union.putAll(value.variables());
    return union;
  }

  @Override
  public String debug() {
    return "OP(" + operator + ")[" + tree.debug() + ",'" + value.debug() + "']";
  }

  @Override
  public String js(Context context, String env) {
    return "(" + tree.js(Context.DEFAULT, env) + operator + value.js(Context.DEFAULT, env) + ")";
  }

  @Override
  public boolean hasAuto() {
    return tree.hasAuto() || value.hasAuto();
  }

  @Override
  public void writeTypes(ViewScope vs) {
    tree.writeTypes(vs);
    value.writeTypes(vs);
  }

  @Override
  public Set<String> queries() {
    TreeSet<String> all = new TreeSet<>();
    all.addAll(tree.queries());
    all.addAll(value.queries());
    return all;
  }
}
