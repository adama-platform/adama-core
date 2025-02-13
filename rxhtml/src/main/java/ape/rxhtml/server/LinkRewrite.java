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
package ape.rxhtml.server;

import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

/** a simple link rewriter */
public class LinkRewrite {
  private static final String SLASH = Pattern.quote("/");
  public final Part[] parts;

  public LinkRewrite(String dest) {
    String[] rule = dest.split(SLASH, -1);

    parts = new Part[rule.length];
    for (int i = 0; i < rule.length; i++) {
      if (rule[i].startsWith("$")) {
        parts[i] = new SubstPart(rule[i].substring(1));
      } else {
        parts[i] = new FixedPart(rule[i]);
      }
    }
  }

  public int score(TreeMap<String, List<String>> query) {
    int sum = 0;
    for (int i = 0; i < parts.length; i++) {
      sum += parts[i].score(query);
    }
    return sum;
  }

  public String eval(TreeMap<String, List<String>> query) {
    String[] results = new String[parts.length];
    for (int i = 0; i < parts.length; i++) {
      results[i] = parts[i].eval(query);
    }
    return String.join("/", results);
  }

  private interface Part {
    public String eval(TreeMap<String, List<String>> query);
    public int score(TreeMap<String, List<String>> query);
  }

  public class FixedPart implements Part {
    private final String fixed;

    public FixedPart(String fixed) {
      this.fixed = fixed;
    }

    @Override
    public String eval(TreeMap<String, List<String>> query) {
      return fixed;
    }

    @Override
    public int score(TreeMap<String, List<String>> query) {
      return 1;
    }
  }

  public class SubstPart implements Part {
    private final String var;
    public SubstPart(String var) {
      this.var = var;
    }

    @Override
    public String eval(TreeMap<String, List<String>> query) {
      if (query.containsKey(var)) {
        String result = query.get(var).get(0);
        query.remove(var);
        return result;
      }
      return "";
    }

    @Override
    public int score(TreeMap<String, List<String>> query) {
      if (query.containsKey(var)) {
        return 1;
      } else {
        return -10000;
      }
    }
  }
}
