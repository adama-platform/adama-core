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
package ape.common.template;

import ape.common.template.fragment.Fragment;
import ape.common.template.fragment.FragmentType;
import ape.common.template.tree.*;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * Recursive descent parser for converting template strings into syntax trees.
 * Parses text literals, variable expressions (with optional unescape filter),
 * and conditional blocks (if/ifnot with end markers) into composable T nodes.
 */
public class Parser {
  private final ArrayList<Fragment> fragments;
  private int at;

  private Parser(ArrayList<Fragment> fragments) {
    this.fragments = fragments;
    this.at = 0;
  }

  /** parse the given string and return a template tree node */
  public static T parse(String t) {
    return new Parser(Fragment.parse(t)).pull((x) -> false);
  }

  /** the actual recursive descent method (super generic) */
  private T pull(Function<Fragment, Boolean> stopAt) {
    TConcat root = new TConcat();
    while (at < fragments.size()) {
      Fragment curr = fragments.get(at);
      if (stopAt.apply(curr)) {
        at++;
        return root;
      }
      at++;
      if (curr.type == FragmentType.Text) {
        root.add(new TText(curr.text[0]));
      }
      if (curr.type == FragmentType.Expression) {
        boolean unescape = false;
        if (curr.text.length > 2) {
          unescape = "|".equals(curr.text[1]) && "unescape".equalsIgnoreCase(curr.text[2]);
        }
        root.add(new TVariable(curr.text[0], unescape));
      }
      if (curr.type == FragmentType.If) {
        // TODO: interpret beyond text[0]
        root.add(new TIf(curr.text[0], pull((x) -> x.type == FragmentType.End)));
      }
      if (curr.type == FragmentType.IfNot) {
        // TODO: interpret beyond text[0]
        root.add(new TIfNot(curr.text[0], pull((x) -> x.type == FragmentType.End)));
      }
    }
    return root;
  }
}
