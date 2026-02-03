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

/**
 * ATL tree node that applies a named transformation function to a value.
 * Transformations include formatting (date, number), encoding (uri, html),
 * and text manipulation. Syntax: {value|transform}
 */
public class Transform implements Tree {

  public final Tree base;
  public final String transform;

  public Transform(Tree base, String transform) {
    this.base = base;
    this.transform = transform;
  }

  @Override
  public Map<String, String> variables() {
    return base.variables();
  }

  @Override
  public String debug() {
    return "TRANSFORM(" + base.debug() + "," + transform + ")";
  }

  @Override
  public String js(Context context, String env) {
    return "($.TR('" + transform + "'))(" + base.js(context, env) + ")";
  }

  @Override
  public boolean hasAuto() {
    return base.hasAuto();
  }

  @Override
  public void writeTypes(ViewScope vs) {
    base.writeTypes(vs);
  }

  @Override
  public Set<String> queries() {
    return base.queries();
  }
}
