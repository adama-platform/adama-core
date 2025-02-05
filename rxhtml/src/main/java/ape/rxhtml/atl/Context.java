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

import java.util.HashMap;

public class Context {
  public static final Context DEFAULT = new Context(false, false);
  public final boolean is_class;
  public final HashMap<String, Integer> freq;
  public final boolean allow_auto;

  private Context(boolean is_class, boolean allow_auto) {
    this.is_class = is_class;
    if (is_class) {
      freq = new HashMap<>();
    } else {
      freq = null;
    }
    this.allow_auto = allow_auto;
  }

  public static final Context makeClassContext() {
    return new Context(true, false);
  }

  public static final Context makeAutoVariable() {
    return new Context(false, true);
  }

  public void cssTrack(String fragment) {
    Integer prior = freq.get(fragment);
    if (prior == null) {
      freq.put(fragment, 1);
    } else {
      freq.put(fragment, prior + 1);
    }
  }
}
