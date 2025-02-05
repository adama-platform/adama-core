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
import ape.translator.env.GlobalObjectPool;
import ape.translator.tree.types.TyType;

import java.util.TreeMap;

/** common: watch an environment for types that flow */
public class LambdaWatcher implements Watcher {
  private final Environment environment;
  private final TreeMap<String, TyType> closureTyTypes;
  private final TreeMap<String, String> closureTypes;

  public LambdaWatcher(Environment environment, TreeMap<String, TyType> closureTyTypes, TreeMap<String, String> closureTypes) {
    this.environment = environment;
    this.closureTyTypes = closureTyTypes;
    this.closureTypes = closureTypes;
  }

  @Override
  public void observe(String name, TyType type) {
    TyType ty = environment.rules.Resolve(type, false);
    if (GlobalObjectPool.ignoreCapture(name, ty)) {
      return;
    }
    if (!closureTypes.containsKey(name) && ty != null) {
      closureTyTypes.put(name, ty);
      closureTypes.put(name, ty.getJavaConcreteType(environment));
    }
  }

  @Override
  public void assoc(String name) {

  }
}
