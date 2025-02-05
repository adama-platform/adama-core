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

import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.privacy.Guard;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.Collections;
import java.util.function.Consumer;

public class WebGuard extends DocumentPosition {
  public final Token requires;
  public final Guard guard;

  public WebGuard(Token requires, Guard guard) {
    this.requires = requires;
    this.guard = guard;
    ingest(requires);
    ingest(guard);
  }

  public void emit(final Consumer<Token> yielder) {
    yielder.accept(requires);
    guard.emit(yielder);
  }

  public void format(Formatter formatter) {
    guard.format(formatter);
  }

  public void typing(TypeCheckerRoot checker) {
    checker.register(Collections.emptySet(), (env) -> {
      for (TokenizedItem<String> policy : guard.policies) {
        var dcp = env.document.root.storage.policies.get(policy.item);
        if (dcp == null) {
          env.document.createError(this, String.format("Policy '%s' was not found for web operation", policy.item));
        }
      }
    });
  }
}
