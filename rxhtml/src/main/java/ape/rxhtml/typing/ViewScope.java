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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.rxhtml.template.StatePath;
import ape.rxhtml.template.sp.PathInstruction;
import ape.rxhtml.template.sp.SwitchTo;

import java.util.Map;
import java.util.TreeMap;

/** scope for constructing a view object based on inference */
public class ViewScope {
  public final ViewScope parent;
  public final TreeMap<String, String> types;
  public final TreeMap<String, ViewScope> children;

  private ViewScope(ViewScope parent) {
    this.parent = parent;
    this.types = new TreeMap<>();
    this.children = new TreeMap<>();
  }

  public void fill(ObjectNode node) {
    for (Map.Entry<String, String> entry : types.entrySet()) {
      node.put(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, ViewScope> entry : children.entrySet()) {
      entry.getValue().fill(node.putObject(entry.getKey()));
    }
  }

  public ViewScope eval(String pathing) {
    StatePath sp = StatePath.resolve(pathing, "$");
    ViewScope current = this;
    for (PathInstruction instruction : sp.instructions) {
      if (current != null) {
        current = instruction.next(current);
      }
    }
    return current.child(sp.name);
  }

  public void write(String pathing, String type, boolean checkForViewSwitch) {
    StatePath sp = StatePath.resolve(pathing, "$");
    ViewScope current = this;
    boolean foundView = false;
    for (PathInstruction instruction : sp.instructions) {
      if (instruction instanceof SwitchTo) {
        if ("view".equals(((SwitchTo) instruction).dest)) {
          foundView = true;
        }
      }
      if (current != null) {
        current = instruction.next(current);
      }
    }
    if (!foundView && checkForViewSwitch) {
      return;
    }
    if (current != null) {
      String prior = current.types.get(sp.name);
      if (prior == null || "lookup".equals(prior)) {
        current.types.put(sp.name, type);
        // TODO: convert the map to a set such that we can see all the type interactions? consider it
      }
    }
  }

  public ViewScope child(String name) {
    ViewScope result = children.get(name);
    if (result == null) {
      result = new ViewScope(this);
      children.put(name, result);
    }
    return result;
  }

  public static ViewScope makeRoot() {
    return new ViewScope(null);
  }
}
