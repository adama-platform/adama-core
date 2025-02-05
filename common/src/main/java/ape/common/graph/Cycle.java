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
package ape.common.graph;

import java.util.*;

/* a cycle detector in a graph */
public class Cycle {
  private final Map<String, Set<String>> graph;
  private final TreeSet<String> visited;
  private final Stack<String> stack;
  private final ArrayDeque<String> remain;

  private Cycle(Map<String, Set<String>> graph) {
    this.graph = graph;
    this.visited = new TreeSet<>();
    this.stack = new Stack<>();
    this.remain = new ArrayDeque<>(graph.keySet());
  }

  public static String detect(Map<String, Set<String>> graph) {
    return new Cycle(graph).detect();
  }

  private String walk(String at) {
    if (stack.contains(at)) {
      stack.push(at);
      return String.join(", ", stack.toArray(new String[stack.size()]));
    }
    if (visited.contains(at)) {
      return null;
    }
    visited.add(at);
    Set<String> depends = graph.get(at);
    if (depends != null) {
      stack.push(at);
      try {
        for (String depend : depends) {
          String result = walk(depend);
          if (result != null) {
            return result;
          }
        }
      } finally {
        stack.pop();
      }
    }
    return null;
  }

  private String detect() {
    if (!remain.isEmpty()) {
      String result = walk(remain.poll());
      return result;
    }
    return null;
  }
}
