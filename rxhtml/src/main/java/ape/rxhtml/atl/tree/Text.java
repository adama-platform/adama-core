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
package ape.rxhtml.atl.tree;

import ape.common.Escaping;
import ape.rxhtml.atl.Context;
import ape.rxhtml.typing.ViewScope;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * ATL tree node representing static text content.
 * Outputs a JavaScript string literal. For class attributes, tracks CSS
 * class usage and normalizes whitespace for optimization.
 */
public class Text implements Tree {
  public final String text;

  public Text(String text) {
    this.text = text;
  }

  @Override
  public Map<String, String> variables() {
    return Collections.emptyMap();
  }

  @Override
  public String debug() {
    return "TEXT(" + text + ")";
  }

  @Override
  public String js(Context context, String env) {
    String textToUse = text;
    if (context.is_class) {
      String trimmed = textToUse.trim();
      context.cssTrack(trimmed);
      // The reason we do this is to ensure developers don't try to make a new class via concatenation as that will break future optimizers.
      textToUse = " " + trimmed + " ";
    }
    return "\"" + new Escaping(textToUse).removeNewLines().keepSlashes().go() + "\"";
  }

  /** silly optimization for empty strings and classes */
  public boolean skip(Context context) {
    if (context.is_class) {
      return text.trim().length() == 0;
    }
    return false;
  }

  @Override
  public boolean hasAuto() {
    return false;
  }

  @Override
  public void writeTypes(ViewScope vs) {
  }

  @Override
  public Set<String> queries() {
    return Collections.emptySet();
  }
}
