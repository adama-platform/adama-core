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
package ape.rxhtml.atl;

import ape.rxhtml.atl.tree.*;
import ape.rxhtml.atl.tree.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Parses ATL (Attribute Template Language) expressions into an abstract syntax tree.
 * Handles variable lookups, conditional blocks, text content, transformations,
 * and comparison operations. The resulting Tree can generate JavaScript code.
 */
public class Parser {

  // handle transforms
  private static Tree wrapTransforms(Tree root, TokenStream.Token token) {
    Tree n = root;
    for (int k = 0; k < token.transforms.length; k++) {
      n = new Transform(n, token.transforms[k]);
    }
    return n;
  }

  private static Tree of(ArrayList<Tree> children) {
    if (children.size() > 1) {
      return new Concat(children.toArray(new Tree[children.size()]));
    } else if (children.size() == 1) {
      return children.get(0);
    } else {
      return new Empty();
    }
  }

  private static boolean isAutoVar(String name) {
    return ("%".equals(name) || "$".equals(name) || "#".equals(name));
  }

  private static Tree getBaseOf(TokenStream.Token conditionStart) throws ParseException {
    // TODO: && and ||
    for (String operator : Operate.OPERATORS) {
      if (conditionStart.base.contains(operator)) {
        String[] parts = conditionStart.base.split(Pattern.quote(operator));
        return new Operate(new Lookup(parts[0].trim()), parts.length > 1 ? parts[1].trim() : "", Operate.convertOp(operator));
      }
    }
    return new Lookup(conditionStart.base);
  }

  private static Tree condition(Iterator<TokenStream.Token> it, TokenStream.Token conditionStart) throws ParseException {
    ArrayList<Tree> childrenTrue = new ArrayList<>();
    ArrayList<Tree> childrenFalse = new ArrayList<>();
    ArrayList<Tree> active = childrenTrue;

    while (true) {
      if (!it.hasNext()) {
        throw new ParseException("unclosed condition block");
      }
      TokenStream.Token token = it.next();
      if (token.type == TokenStream.Type.Condition) {
        if (token.mod == TokenStream.Modifier.None) {
          route(active, it, token);
        } else if (token.mod == TokenStream.Modifier.Else) {
          active = childrenFalse;
        } else if (token.mod == TokenStream.Modifier.End) {
          break;
        }
      } else {
        route(active, it, token);
      }
    }

    Tree lookup = getBaseOf(conditionStart);
    Tree guard = wrapTransforms(lookup, conditionStart);
    if (conditionStart.mod == TokenStream.Modifier.Not) {
      guard = new Negate(guard);
    }
    return new Condition(guard, of(childrenTrue), of(childrenFalse));
  }

  private static void route(ArrayList<Tree> children, Iterator<TokenStream.Token> it, TokenStream.Token token) throws ParseException {
    switch (token.type) {
      case Text:
        children.add(new Text(token.base));
        return;
      case Variable:
        if (isAutoVar(token.base)) {
          children.add(new AutoVar());
        } else {
          children.add(wrapTransforms(new Lookup(token.base), token));
        }
        return;
      case Condition:
        children.add(condition(it, token));
    }
  }

  private static Tree parse(Iterator<TokenStream.Token> it) throws ParseException {
    ArrayList<Tree> children = new ArrayList<>();
    while (it.hasNext()) {
      route(children, it, it.next());
    }
    return of(children);
  }

  public static Tree parse(String text) throws ParseException {
    return parse(TokenStream.tokenize(text).iterator());
  }
}
