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
package ape.translator.env;

import java.util.HashMap;
import java.util.HashSet;

/** an environment for computing free variables */
public class FreeEnvironment {
  public final HashSet<String> free;
  private final FreeEnvironment parent;
  private final HashMap<String, String> translation;
  private final HashSet<String> defined;

  private FreeEnvironment(final FreeEnvironment parent, HashMap<String, String> translation, HashSet<String> free) {
    this.parent = parent;
    this.translation = translation;
    this.defined = new HashSet<>();
    this.free = free;
  }

  /** require a variable */
  public void require(String variable) {
    if (defined.contains(variable)) {
      return;
    }
    if (parent != null) {
      parent.require(variable);
      return;
    }
    String translated = translation.get(variable);
    if (translated != null) {
      free.add(translated);
    } else {
      free.add(variable);
    }
  }

  /** define a variable */
  public void define(String variable) {
    defined.add(variable);
  }

  /** push a new scope boundary for logic */
  public FreeEnvironment push() {
    return new FreeEnvironment(this, translation, free);
  }

  /** create a new environment */
  public static FreeEnvironment root() {
    return new FreeEnvironment(null, new HashMap<>(), new HashSet<>());
  }

  /** create a new environment */
  public static FreeEnvironment record(HashMap<String, String> recordFieldTranslation) {
    return new FreeEnvironment(null, recordFieldTranslation, new HashSet<>());
  }

  @Override
  public String toString() {
    return "free:" + String.join(", ", free);
  }
}
