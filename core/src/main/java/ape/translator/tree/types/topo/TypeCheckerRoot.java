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
package ape.translator.tree.types.topo;

import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

/** This is a hack class to introduce topological ordering as this is going to be a very complex change */
public class TypeCheckerRoot implements TypeChecker {
  private final ArrayList<Check> checks;
  private HashMap<String, ArrayList<String>> aliases;
  private final HashMap<String, Check> byName;

  private class Check {
    private final String name;
    private final Set<String> depends;
    private final Consumer<Environment> checker;

    private boolean handled;

    public Check(String name, Set<String> depends, Consumer<Environment> checker) {
      this.name = name;
      this.depends = depends;
      this.checker = checker;
      this.handled = false;
    }
  }

  public TypeCheckerRoot() {
    this.checks = new ArrayList<>();
    this.aliases = new HashMap<>();
    this.byName = new HashMap<>();
  }

  @Override
  public void define(Token name, Set<String> depends, Consumer<Environment> checker) {
    Check check = new Check(name.text, depends, checker);
    this.checks.add(check);
    byName.put(name.text, check);
  }

  @Override
  public void register(Set<String> depends, Consumer<Environment> checker) {
    this.checks.add(new Check(null, depends, checker));
  }

  public void alias(String from, String to) {
    ArrayList<String> alias = aliases.get(from);
    if (alias == null) {
      alias = new ArrayList<>();
      aliases.put(from, alias);
    }
    alias.add(to);
  }

  @Override
  public void issueError(DocumentPosition dp, String message) {
    this.checks.add(new Check(null, null, env -> env.document.createError(dp, message)));
  }


  private void satisfyOneChild(String name, Environment environment) {
    Check check = byName.get(name);
    if (check == null) {
      return;
    }
    if (!check.handled) {
      satisfyAllChildren(check, environment);
      check.checker.accept(environment);
    }
  }

  private void satisfyAllChildren(Check check, Environment environment) {
    check.handled = true;
    if (check.depends != null) {
      for (String depend : check.depends) {
        ArrayList<String> prior = aliases.get(depend);
        if (prior != null) {
          for (String child : prior) {
            satisfyOneChild(child, environment);
          }
        }
        satisfyOneChild(depend, environment);
      }
    }
  }

  public void check(Environment environment) {
    while (checks.size() > 0) {
      Check check = checks.remove(0);
      if (!check.handled) {
        satisfyAllChildren(check, environment);
        check.checker.accept(environment);
      }
    }
  }
}
