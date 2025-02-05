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
package ape.translator.tree.common;

import java.util.ArrayList;

/**
 * this is the janky way we do build code, and it brings great shame. Fixing this will require a
 * massive overhaul, and the problem is just how hard it is to make a reasonable formatted code.
 */
public class StringBuilderWithTabs {
  private final StringBuilder builder;
  private int tabs;

  public StringBuilderWithTabs() {
    builder = new StringBuilder();
    tabs = 0;
  }

  /** append the given string */
  public StringBuilderWithTabs append(final String x) {
    builder.append(x);
    return this;
  }

  /** insert a tab UNCLEAN */
  public StringBuilderWithTabs tab() {
    builder.append("  ");
    return this;
  }

  /** decrease the tab */
  public StringBuilderWithTabs tabDown() {
    tabs--;
    if (tabs < 0) {
      tabs = 0;
    }
    return this;
  }

  /** increase the tabs */
  public StringBuilderWithTabs tabUp() {
    tabs++;
    return this;
  }

  /** split the builder into lines */
  public ArrayList<String> toLines() {
    final var lines = new ArrayList<String>();
    for (final String line : toString().split("\n")) {
      lines.add(line);
    }
    return lines;
  }

  @Override
  public String toString() {
    return builder.toString();
  }

  /** append a new line */
  public StringBuilderWithTabs writeNewline() {
    builder.append("\n");
    for (var k = 0; k < tabs; k++) {
      builder.append("  ");
    }
    return this;
  }
}
