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
package ape.translator.tree.watcher;

import ape.translator.env.Environment;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.natives.*;

import java.util.LinkedHashSet;

public class SimpleWatcherForVariablesAndServices implements Watcher {
  private final Environment env;
  private final LinkedHashSet<String> variables;
  private final LinkedHashSet<String> services;

  public SimpleWatcherForVariablesAndServices(Environment env, LinkedHashSet<String> variables, LinkedHashSet<String> services) {
    this.env = env;
    this.variables = variables;
    this.services = services;
  }

  @Override
  public void observe(String name, TyType type) {
    TyType resolved = env.rules.Resolve(type, true);
    if (resolved instanceof TyNativeGlobalObject) return;
    if (resolved instanceof TyNativeTemplate) return;
    if (resolved instanceof TyNativeFunctional) {
      variables.addAll(((TyNativeFunctional) resolved).gatherDependencies());
      return;
    }
    if (resolved instanceof TyNativeService) {
      services.add(((TyNativeService) resolved).service.name.text);
      return;
    }
    if (resolved instanceof TyNativeClientService) {
      services.add(((TyNativeClientService) resolved).service.name.text);
      return;
    }
    if (!env.document.functionTypes.containsKey(name)) {
      variables.add(name);
    }
  }

  @Override
  public void assoc(String name) {

  }
}
