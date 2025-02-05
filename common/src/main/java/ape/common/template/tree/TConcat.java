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
package ape.common.template.tree;

import com.fasterxml.jackson.databind.JsonNode;
import ape.common.template.Settings;

import java.util.ArrayList;

/** concat multiple T together */
public class TConcat implements T {
  private final ArrayList<T> children;
  private long memory;

  public TConcat() {
    this.children = new ArrayList<>();
    this.memory = 64;
  }

  public void add(T child) {
    this.children.add(child);
    this.memory += 40 + child.memory();
  }

  @Override
  public void render(Settings settings, JsonNode node, StringBuilder output) {
    for (T child : children) {
      child.render(settings, node, output);
    }
  }

  @Override
  public long memory() {
    return memory;
  }
}
