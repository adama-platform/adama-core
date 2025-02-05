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

import java.util.ArrayList;
import java.util.Stack;

/** visitor for working with paths within a DataScope; this is how to navigate and select data from within a scope */
public class DataScopeVisitor implements PathVisitor {
  private final String path;
  private final PrivacyFilter privacy;
  private final ObjectNode forest;
  private final Stack<String> current;
  private ObjectNode useType;
  private boolean switchToView;
  private ArrayList<String> errors;

  public DataScopeVisitor(String path, PrivacyFilter privacy, ObjectNode forest, Stack<String> current) {
    this.path = path;
    this.privacy = privacy;
    this.forest = forest;
    this.current = current;
    this.switchToView = false;
    this.errors = new ArrayList<>();
  }

  public String[] destroyAndConvertIntoPath() {
    String[] path = new String[current.size()];
    int k = path.length - 1;
    while (!current.empty()) {
      path[k] = current.pop();
      k--;
    }
    return path;
  }

  @Override
  public void data() {

  }

  @Override
  public void view() {
    this.switchToView = true;
  }

  @Override
  public void root() {
    while (current.size() > 1) {
      current.pop();
    }
  }

  @Override
  public void parent() {
    if (current.size() > 1) {
      current.pop();
    }
  }

  private ObjectNode getCurrentChildType(String child) {
    String at = current.peek();
    boolean isList = false; // spec work
    if (at.startsWith("list:")) {
      isList = true;
      at = at.substring(5);
    }
    ObjectNode holder = (ObjectNode) forest.get("types").get(at);
    if (holder == null) {
      errors.add("Failed to find type '" + at + "'");
      return null;
    }

    if (isList) {
      try {
        Integer.parseInt(child);
        // it's all good, extract the embedded list type
        errors.add("Parsed, but not yet implemented to dive into an index within an list");
        return null;
      } catch (NumberFormatException nfe) {
        errors.add("Failed to parse '" + child + "' as int within '" + at + "' (which is a list/array)");
        return null;
      }
    } else {
      ObjectNode childType = (ObjectNode) holder.get("fields").get(child);
      if (childType == null) {
        errors.add("Failed to type '" + child + "' within '" + at + "'");
        return null;
      }
      JsonNode privacyLevel = childType.get("privacy");
      if (!privacy.visible(privacyLevel)) {
        errors.add("Privacy violation; field '" + child + "' was referenced, but is not visible within the privacy filter:" + privacyLevel.toString() + " allowed:" + privacy.toString());
        // TODO: make above error more meaningful
      }
      childType.put("used", true);
      return (ObjectNode) childType.get("type");
    }
  }

  @Override
  public void dive(String child) {
    ObjectNode nextType = getCurrentChildType(child);
    if (nextType == null) {
      return;
    }

    String nature = nextType.get("nature").textValue();

    if (nature.equals("native_maybe") || nature.equals("reactive_maybe")) {
      nextType = (ObjectNode) nextType.get("type");
      nature = nextType.get("nature").textValue();
    }

    if (nature.equals("native_list") || nature.equals("native_array")) {
      ObjectNode subType = (ObjectNode) nextType.get("type");
      String subNature = subType.get("nature").textValue();
      if (subNature.equals("reactive_ref") || subNature.equals("native_ref")) {
        String ref = subType.get("ref").textValue();
        current.push("list:" + ref);
      } else  {
        errors.add("failed to understand list sub-type:" + subType);
      }
    } else if (nature.equals("native_ref") || nature.equals("reactive_ref")) {
      String ref = nextType.get("ref").textValue();
      current.push(ref);
    } else {
      errors.add("failed to understand type:" + nextType);
    }
  }

  @Override
  public void use(String field) {
    this.useType = getCurrentChildType(field);
  }

  public ObjectNode getUseType() {
    return this.useType;
  }

  public boolean hasErrors() {
    return errors.size() > 0;
  }

  public ArrayList<String> getErrors() {
    return errors;
  }

  public boolean didSwitchToView() {
    return switchToView;
  }
}
