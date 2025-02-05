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
package ape.rxhtml.typing;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.TreeSet;

/** scope of allowed privacy policies */
public class PrivacyFilter {
  public final TreeSet<String> allowed;

  public PrivacyFilter(String... allow) {
    this.allowed = new TreeSet<>();
    for (String x : allow) {
      if (x.length() > 0) {
        allowed.add(x);
      }
    }
  }

  @Override
  public String toString() {
    ArrayList<String> parts = new ArrayList<>();
    parts.addAll(allowed);
    return String.join(", ", parts);
  }

  public boolean visible(JsonNode privacy) {
    if (privacy == null) {
      return false;
    }
    if (privacy.isTextual()) {
      String fieldPolicy = privacy.textValue();
      if ("private".equals(fieldPolicy)) {
        return false;
      }
      if ("public".equals(fieldPolicy) || "bubble".equals(fieldPolicy)) {
        return true;
      }
      return false;
    } else if (privacy.isArray()) {
      for (int k = 0; k < privacy.size(); k++) {
        if (!allowed.contains(privacy.get(k).textValue())) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
