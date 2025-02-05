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

import ape.common.Once;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.structures.StorageSpecialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TypeCheckerStructure implements TypeChecker {
  private class Check {
    public final String name;
    public final Set<String> depends;
    public final Consumer<Environment> checker;

    public Check(String name, Set<String> depends, Consumer<Environment> checker) {
      this.name = name;
      this.depends = depends;
      this.checker = checker;
    }

    public void transferInto(String name, Once<Environment> onceEnv, StorageSpecialization specialization, TypeCheckerRoot rootChecker) {
      Consumer<Environment> modern = (stale) -> {
        Environment envToUse = onceEnv.access(() -> specialization == StorageSpecialization.Message ? stale.scope().scopeMessage() : stale.scope());
        checker.accept(envToUse);
      };

      if (name == null && depends == null) {
        rootChecker.register(Collections.emptySet(), modern);
      }

      HashSet<String> translated = new HashSet<>();
      if (depends != null) {
        for (String depend : depends) {
          if (defined.contains(depend)) {
            translated.add(name + "::" + depend);
          } else {
            translated.add(depend);
          }
        }
      }

      if (this.name != null) {
        rootChecker.define(Token.WRAP(name + "::" + this.name), translated, modern);
        rootChecker.alias("::" + this.name, name + "::" + this.name);
      } else {
        rootChecker.register(translated, modern);
      }
    }
  }

  private final ArrayList<Check> checks;
  private final HashSet<String> defined;

  public TypeCheckerStructure() {
    this.checks = new ArrayList<>();
    this.defined = new HashSet<>();
  }

  @Override
  public void define(Token name, Set<String> depends, Consumer<Environment> checker) {
    this.checks.add(new Check(name.text, depends, checker));
    this.defined.add(name.text);
  }

  @Override
  public void register(Set<String> depends, Consumer<Environment> checker) {
    this.checks.add(new Check(null, depends, checker));
  }

  @Override
  public void issueError(DocumentPosition dp, String message) {
    this.checks.add(new Check(null, null, env -> {
      env.document.createError(dp, message);
    }));
  }

  public void transferInto(String name, StorageSpecialization specialization, TypeCheckerRoot rootChecker) {
    Once<Environment> onceEnv = new Once<>();
    for (final Check check : checks) {
      check.transferInto(name, onceEnv, specialization, rootChecker);
    }
  }
}
