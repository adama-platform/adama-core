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
import org.jsoup.nodes.Entities;

/** pull the variable into the document */
public class TVariable implements T {
  public final String variable;
  public final boolean unescape;

  public TVariable(String variable, boolean unescape) {
    this.variable = variable;
    this.unescape = unescape;
  }

  @Override
  public void render(Settings settings, JsonNode node, StringBuilder output) {
    JsonNode fetched = node.get(variable);
    if (fetched != null && fetched.isTextual()) {
      if (settings.html && !unescape) {
        output.append(Entities.escape(fetched.textValue()));
      } else {
        output.append(fetched.textValue());
      }
    }
  }

  @Override
  public long memory() {
    return 64 + variable.length();
  }
}
