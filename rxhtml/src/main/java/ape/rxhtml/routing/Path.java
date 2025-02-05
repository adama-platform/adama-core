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
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/** a binding site path for routing */
public class Path {
  private final String name;
  private final TreeMap<String, Path> fixed;
  private final ArrayList<Path> numbers;
  private final ArrayList<Path> texts;
  private Path suffix;
  private Pathable target;

  public Path(String name) {
    this.name = name;
    this.fixed = new TreeMap<>();
    this.numbers = new ArrayList<>();
    this.texts = new ArrayList<>();
    this.suffix = null;
    this.target = null;
  }

  public long memory() {
    long result = 40L;
    if (name != null) {
      result += name.length() * 2;
    }
    for (Map.Entry<String, Path> e : fixed.entrySet()) {
      result += 64;
      result += e.getKey().length() * 2;
      result += e.getValue().memory();
    }
    for (Path n : numbers) {
      result += n.memory();
    }
    for (Path t : texts) {
      result += t.memory();
    }
    if (suffix != null) {
      result += suffix.memory();
    }
    if (target != null) {
      result += target.memory();
    }
    return result;
  }

  public Path diveFixed(String path) {
    Path next = fixed.get(path);
    if (next == null) {
      next = new Path(null);
      fixed.put(path, next);
    }
    return next;
  }

  public Path newNumber(String name) {
    Path path = new Path(name);
    numbers.add(path);
    return path;
  }

  public Path newText(String name) {
    Path path = new Path(name);
    texts.add(path);
    return path;
  }

  public Path setSuffix(String name) {
    if (suffix == null) {
      suffix = new Path(name);
    }
    return suffix;
  }

  public boolean set(Pathable target) {
    if (this.target == null) {
      this.target = target;
      return true;
    }
    return false;
  }

  public Pathable route(int at, String[] parts, TreeMap<String, String> capture) {
    if (at == parts.length) {
      return target;
    }
    if (at < parts.length) {
      String part = parts[at];
      // Numbers
      try {
        Double.parseDouble(part);
        for (Path path : numbers) {
          capture.put(path.name, part);
          Pathable found = path.route(at + 1, parts, capture);
          if (found != null) {
            return found;
          }
          capture.remove(path.name);
        }
      } catch (NumberFormatException nfe) {
        // not a number
      }
      // Text
      for (Path path : texts) {
        capture.put(path.name, part);
        Pathable found = path.route(at + 1, parts, capture);
        if (found != null) {
          return found;
        }
        capture.remove(path.name);
      }
      // Fixed Values
      Path fix = fixed.get(part);
      if (fix != null) {
        Pathable found = fix.route(at + 1, parts, capture);
        if (found != null) {
          return found;
        }
      }
      // Suffix
      if (suffix != null) {
        StringBuilder captured = new StringBuilder();
        captured.append(part);
        for (int s = at + 1; s < parts.length; s++) {
          captured.append("/").append(parts[s]);
        }
        capture.put(suffix.name, captured.toString());
        return suffix.target;
      }
    }
    return null;
  }

  public static String[] parsePath(String path) {
    String uri = path;
    // truncate a trailing "/"
    while (uri.length() > 1 && uri.endsWith("/")) {
      uri = uri.substring(0, uri.length() - 1);
    }
    // remove prefix /
    if (uri.startsWith("/")) {
      uri = uri.substring(uri.indexOf('/') + 1);
    }
    return uri.split(Pattern.quote("/"));
  }
}
