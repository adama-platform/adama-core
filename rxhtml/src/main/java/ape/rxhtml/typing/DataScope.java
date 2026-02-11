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
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.rxhtml.template.sp.PathVisitor;

import java.util.Stack;
import java.util.function.Consumer;

/** a path/scope into a forest */
public class DataScope {
  private final ObjectNode forest;
  private final String[] path;

  private DataScope(ObjectNode forest, String[] path) {
    this.forest = forest;
    this.path = path;
  }

  public Stack<String> toStackPath() {
    Stack<String> stack = new Stack<>();
    for (String p : path) {
      stack.push(p);
    }
    return stack;
  }

  public boolean hasChannel(String channel) {
    JsonNode channels = forest.get("channels");
    if (channels == null) {
      return false;
    }
    return channels.has(channel);
  }

  public static DataScope root(ObjectNode forest) {
    return new DataScope(forest, new String[] { "__Root" });
  }

  public DataScope push(String... append) {
    String[] next = new String[path.length + append.length];
    for (int k = 0; k < path.length; k++) {
      next[k] = path[k];
    }
    for (int k = 0; k < append.length; k++) {
      next[path.length + k] = append[k];
    }
    return new DataScope(forest, next);
  }

  public DataSelector select(PrivacyFilter privacy, String path, Consumer<String> reportError) {
    DataScopeVisitor dsv = new DataScopeVisitor(path, privacy, forest, toStackPath());
    PathVisitor.visit(path, dsv);
    if (dsv.didSwitchToView()) {
      return null;
    }
    if (dsv.hasErrors()) {
      for (String error : dsv.getErrors()) {
        reportError.accept(error);
      }
      return null;
    }
    String[] newPath = dsv.destroyAndConvertIntoPath();
    DataScope next = new DataScope(forest, newPath);
    return new DataSelector(next, dsv.getUseType());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String p : path) {
      sb.append("[").append(p).append("]");
    }
    return sb.toString();
  }
}
