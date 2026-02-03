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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * ATL tree node representing the auto-variable (% or $).
 * References the current iteration item in loops and repeat contexts.
 * Only valid in contexts where allow_auto is true.
 */
public class AutoVar implements Tree {
  @Override
  public Map<String, String> variables() {
    return Collections.singletonMap("%", "this.x");
  }

  @Override
  public String debug() {
    return "<AUTO>";
  }

  @Override
  public String js(Context context, String env) {
    if (context.allow_auto) {
      return env + ".__x";
    } else {
      return "'__auto_id_not_allowed__'";
    }
  }

  @Override
  public boolean hasAuto() {
    return true;
  }

  @Override
  public void writeTypes(ViewScope vs) {
  }

  @Override
  public Set<String> queries() {
    return Collections.emptySet();
  }
}
