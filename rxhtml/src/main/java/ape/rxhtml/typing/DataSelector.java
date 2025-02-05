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

import java.util.function.Consumer;

/** the tuple of a scope and a type */
public class DataSelector {
  public final DataScope scope;
  public final ObjectNode type;

  public DataSelector(DataScope scope, ObjectNode type) {
    this.scope = scope;
    this.type = type;
  }

  public DataScope scopeInto(Consumer<String> errors) {
    try {
      ObjectNode typeToCheck = type;
      String nature = typeToCheck.get("nature").textValue();

      if (nature.equals("native_maybe") || nature.equals("reactive_maybe")) {
        typeToCheck = (ObjectNode) typeToCheck.get("type");
        nature = typeToCheck.get("nature").textValue();
      }

      if (nature.equals("reactive_ref") || nature.equals("native_ref")) {
        String ref = typeToCheck.get("ref").textValue();
        return scope.push(ref);
      }

      errors.accept("failed to scope into: " + typeToCheck);
      return null;
    } catch (Exception reasonsToHateJSON) {
      errors.accept("error parsing reflection tree:" + reasonsToHateJSON.getMessage() + " JSON=" + type.toString());
      return null;
    }
  }

  public DataScope iterateInto(Consumer<String> errors) {
    try {
      ObjectNode typeToCheck = type;
      String nature = typeToCheck.get("nature").textValue();

      if (nature.equals("native_maybe") || nature.equals("reactive_maybe")) {
        typeToCheck = (ObjectNode) typeToCheck.get("type");
        nature = typeToCheck.get("nature").textValue();
      }

      if (nature.equals("native_list") || nature.equals("native_array")) {
        ObjectNode subType = (ObjectNode) typeToCheck.get("type");
        String subNature = subType.get("nature").textValue();
        if (subNature.equals("reactive_ref") || subNature.equals("native_ref")) {
          String ref = subType.get("ref").textValue();
          return scope.push("list:" + ref, ref);
        }
      }
      errors.accept("failed to iterate into: " + type);
      return null;
    } catch (Exception reasonsToHateJSON) {
      errors.accept("error parsing reflection tree:" + reasonsToHateJSON.getMessage() + " JSON=" + type.toString());
      return null;
    }
  }

  public void validateAttribute(Consumer<String> errors) {
    // TODO: validate as finite string
  }

  public void validateIntegral(Consumer<String> errors) {
    // TODO: validate INT, LONG
  }

  public void validateBoolean(Consumer<String> errors) {
    // TODO: validate INT, LONG
  }

  public void validateSwitchable(Consumer<String> errors) {
    // TODO: validate INT, LONG, ENUM, STRING, BOOL
  }
}
