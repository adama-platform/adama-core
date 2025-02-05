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
import ape.rxhtml.typing.ViewScope;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** an if/then/else node */
public class Condition implements Tree {
  public final Tree guard;
  public Tree branchTrue;
  public Tree branchFalse;

  public Condition(Tree guard, Tree branchTrue, Tree branchFalse) {
    this.guard = guard;
    this.branchTrue = branchTrue;
    this.branchFalse = branchFalse;
  }

  @Override
  public Map<String, String> variables() {
    TreeMap<String, String> union = new TreeMap<>();
    union.putAll(guard.variables());
    union.putAll(branchTrue.variables());
    union.putAll(branchFalse.variables());
    return union;
  }

  @Override
  public String debug() {
    return "(" + guard.debug() + ") ? (" + branchTrue.debug() + ") : (" + branchFalse.debug() + ")";
  }

  @Override
  public String js(Context context, String env) {
    return "((" + guard.js(context, env) + ") ? (" + branchTrue.js(context, env) + ") : (" + branchFalse.js(context, env) + "))";
  }

  @Override
  public boolean hasAuto() {
    return guard.hasAuto() || branchTrue.hasAuto() || branchFalse.hasAuto();
  }

  @Override
  public void writeTypes(ViewScope vs) {
    guard.writeTypes(vs);
    branchTrue.writeTypes(vs);
    branchFalse.writeTypes(vs);
  }

  @Override
  public Set<String> queries() {
    TreeSet<String> all = new TreeSet<>();
    all.addAll(guard.queries());
    all.addAll(branchTrue.queries());
    all.addAll(branchFalse.queries());
    return all;
  }
}
