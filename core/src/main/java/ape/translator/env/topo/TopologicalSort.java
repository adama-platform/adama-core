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
package ape.translator.env.topo;

import java.util.*;

/** a simple class for building a topological order */
public class TopologicalSort<T> {
  public class TopoValue {
    public final T value;
    public final Set<String> dependencies;
    private boolean handled;

    public TopoValue(T value, Set<String> dependencies) {
      this.value = value;
      this.dependencies = dependencies;
      this.handled = dependencies == null;
    }
  }
  private final HashMap<String, TopoValue> values;
  private final ArrayList<T> result;
  private final ArrayDeque<String> remain;
  private final TreeSet<String> cycles;

  public TopologicalSort() {
    this.values = new HashMap<>();
    this.result = new ArrayList<>();
    this.remain = new ArrayDeque<>();
    this.cycles = new TreeSet<>();
  }

  /** add a single item */
  public void add(String key, T value, Set<String> rawDependencies) {
    Set<String> dependencies = rawDependencies != null ? (rawDependencies.isEmpty() ? null : rawDependencies) : null;
    TopoValue val = new TopoValue(value, dependencies);
    values.put(key, val);

    if (dependencies != null) {
      if (allDependenciesHandled(dependencies)) {
        dependencies = null;
      }
    }

    if (dependencies == null) {
      val.handled = true;
      result.add(value);
    } else {
      remain.add(key);
    }
  }

  private boolean allDependenciesHandled(Set<String> dependencies) {
    for (String depend : dependencies) {
      TopoValue exists = values.get(depend);
      if (exists == null) {
        return false;
      }
      if (!exists.handled) {
        return false;
      }
    }
    return true;
  }

  /** core algorithm to insert items requiring dependencies first */
  private void insert(String key, String butNot) {
    if (key.equals(butNot)) {
      cycles.add(key);
      return;
    }

    TopoValue val = values.get(key);
    if (val == null) {
      return;
    }
    if (val.handled) {
      return;
    }
    val.handled = true;

    // make sure all the children are present
    for (String depend : val.dependencies) {
      insert(depend, butNot == null ? key : butNot);
    }

    result.add(val.value);
    remain.remove(key);
  }

  /** drain remaining items */
  private void drainRemain() {
    while (!remain.isEmpty()) {
      insert(remain.removeFirst(), null);
    }
  }

  /** get the results sorted */
  public ArrayList<T> sort() {
    drainRemain();
    return result;
  }

  /** get any elements part of a cycle */
  public Collection<String> cycles() {
    return cycles;
  }
}
