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
package ape.translator.tree.definitions;

import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.client.Group;
import ape.translator.tree.definitions.client.Method;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.Collections;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DefineClientService extends Definition {
  public final Token client;
  public final Token name;
  public final Group root;
  public final TreeMap<String, Method> methods;

  public DefineClientService(Token client, Token name, Group root) {
    this.client = client;
    this.name = name;
    this.root = root;
    this.methods = new TreeMap<>();
    ingest(client);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(client);
    yielder.accept(name);
    root.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    root.format(formatter);
  }

  public void typing(TypeCheckerRoot checker) {
    root.typingForRootGroup(checker);
    root.index(methods);
    checker.register(Collections.emptySet(), root::typecheck);
  }

  public void writeJavaScriptDefn(StringBuilderWithTabs sb, AtomicInteger uniqueIdGenerator, String version) {
    root.writeJavaScriptDefn(name.text, sb, uniqueIdGenerator, version, null, null);
  }
}
