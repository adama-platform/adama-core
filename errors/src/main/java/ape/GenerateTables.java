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
package ape;

import java.lang.reflect.Field;

public class GenerateTables {
  public static String generate() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("package ape;\n\n");
    sb.append("import java.util.HashMap;\n");
    sb.append("import java.util.HashSet;\n");
    sb.append("\n");
    sb.append("public class ErrorTable {\n");
    sb.append("  public static final ErrorTable INSTANCE = new ErrorTable();\n");
    sb.append("  public final HashMap<Integer, String> names;\n");
    sb.append("  public final HashMap<Integer, String> descriptions;\n");
    sb.append("  private final HashSet<Integer> userspace;\n");
    sb.append("  private final HashSet<Integer> notproblem;\n");
    sb.append("  private final HashSet<Integer> retry;\n");
    sb.append("\n");
    sb.append("  public boolean shouldRetry(int code) {\n");
    sb.append("    return retry.contains(code);\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public boolean isUserProblem(int code) {\n");
    sb.append("    return userspace.contains(code);\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public boolean isNotAProblem(int code) {\n");
    sb.append("    return notproblem.contains(code);\n");
    sb.append("  }\n");
    sb.append("\n");
    sb.append("  public ErrorTable() {\n");
    sb.append("    names = new HashMap<>();\n");
    sb.append("    descriptions = new HashMap<>();\n");
    sb.append("    userspace = new HashSet<>();\n");
    sb.append("    notproblem = new HashSet<>();\n");
    sb.append("    retry = new HashSet<>();\n");
    for (int error : ManualUserTable.ERRORS) {
      sb.append("    userspace.add(").append(error).append(");\n");
    }
    for (Field f : ErrorCodes.class.getFields()) {
      sb.append("    names.put(").append(f.getInt(null)).append(", \"").append(f.getName()).append("\");\n");
      Description description = f.getAnnotation(Description.class);
      if (description != null) {
        sb.append("    descriptions.put(").append(f.getInt(null)).append(", \"").append(description.value()).append("\");\n");
      } else {
        sb.append("    descriptions.put(").append(f.getInt(null)).append(", \"no description of error (yet)\");\n");
      }
      if (f.getAnnotation(User.class) != null) {
        sb.append("    userspace.add(").append(f.getInt(null)).append(");\n");
      }
      if (f.getAnnotation(NotProblem.class) != null) {
        sb.append("    notproblem.add(").append(f.getInt(null)).append(");\n");
      }
      if (f.getAnnotation(RetryInternally.class) != null) {
        sb.append("    retry.add(").append(f.getInt(null)).append(");\n");
      }
    }
    sb.append("  }\n");
    sb.append("}\n");
    return sb.toString();
  }
}
