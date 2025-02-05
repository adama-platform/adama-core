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
package ape.rxhtml.routing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.function.Function;

/** instructions represent a parsed URI */
public class Instructions {
  public final String javascript;
  public final HashSet<String> depends;
  public final String formula;
  public final String normalized;
  public final TreeMap<String, String> types;
  public final ArrayList<Function<Path, Path>> progress;

  public Instructions(final String javascript, HashSet<String> depends, String formula, String normalized, TreeMap<String, String> types, ArrayList<Function<Path, Path>> progress) {
    this.javascript = javascript;
    this.depends = depends;
    this.formula = formula;
    this.normalized = normalized;
    this.types = types;
    this.progress = progress;
  }

  /** convert a raw uri to an instruction set */
  public static Instructions parse(String uriRaw) {
    HashSet<String> depends = new HashSet<>();
    String uri = (uriRaw.startsWith("/") ? uriRaw.substring(1) : uriRaw).trim();
    StringBuilder formula = new StringBuilder();
    formula.append("/");
    TreeMap<String, String> types = new TreeMap<>();
    ArrayList<Function<Path, Path>> progress = new ArrayList<>();
    StringBuilder normalized = new StringBuilder();
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    do {
      int kSlash = uri.indexOf('/');
      String fragment = kSlash >= 0 ? uri.substring(0, kSlash).trim() : uri;
      uri = kSlash >= 0 ? uri.substring(kSlash + 1).trim() : "";
      if (!first) {
        sb.append(",");
      }
      first = false;
      if (fragment.startsWith("$")) {
        boolean suffix = false;
        if (fragment.endsWith("*")) {
          if (uri.equals("")) {
            suffix = true;
          } else {
            fragment = fragment.substring(0, fragment.length() - 1);
          }
        }
        if (suffix) {
          final String name = fragment.substring(1, fragment.length() - 1);
          depends.add(name);
          sb.append("'suffix','").append(name).append("'");
          types.put(name, "text");
          formula.append("{").append(name).append("}");
          normalized.append("/$").append("text");
          progress.add((p) -> p.setSuffix(name));
        } else {
          int colon = fragment.indexOf(':');
          String type = (colon > 0 ? fragment.substring(colon + 1) : "text").trim().toLowerCase();
          final String name = (colon > 0 ? fragment.substring(1, colon) : fragment.substring(1)).trim();
          switch (type) {
            case "number":
            case "int":
            case "double":
              type = "number";
              break;
            default:
              type = "text";
          }
          depends.add(name);
          sb.append("'").append(type).append("','").append(name).append("'");
          types.put(name, type);
          formula.append("{").append(name).append("}");
          normalized.append("/$").append(type);
          if ("number".equals(type)) {
            progress.add((p) -> p.newNumber(name));
          } else {
            progress.add((p) -> p.newText(name));
          }
        }
      } else {
        normalized.append("/").append(fragment);
        sb.append("'fixed','").append(fragment).append("'");
        formula.append(fragment);
        final String fixedFragment = fragment;
        progress.add((p) -> p.diveFixed(fixedFragment));
      }
      if (kSlash >= 0) {
        formula.append("/");
      }
    } while (uri.length() > 0);
    sb.append("]");
    return new Instructions(sb.toString(), depends, formula.toString(), normalized.toString(), types, progress);
  }
}
