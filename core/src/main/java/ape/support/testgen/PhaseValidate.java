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
package ape.support.testgen;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.CompilerOptions;
import ape.translator.env.EnvironmentState;
import ape.translator.env.GlobalObjectPool;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.tree.Document;
import ape.translator.tree.common.DocumentPosition;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Pattern;

public class PhaseValidate {
  public static ValidationResults go(final Path inputRoot, final Path path, final String className, final boolean emission, final StringBuilder outputFile) throws Exception {
    CompilerOptions.Builder optionsBuilder = CompilerOptions.start().enableCodeCoverage();
    if (path.getFileName().toString().contains("Instrumented")) {
      optionsBuilder = optionsBuilder.instrument();
    }
    final var options = optionsBuilder.make();
    final var globals = GlobalObjectPool.createPoolWithStdLib(RuntimeEnvironment.Tooling);
    final var state = new EnvironmentState(globals, options, RuntimeEnvironment.Tooling);
    final var document = new Document();
    document.addSearchPath(inputRoot.toFile());
    HashMap<String, String> inc = new HashMap<>();
    inc.put("std", "public int std_here = 123;");
    inc.put("std/foo", "public int foo_here = 123;");
    inc.put("bad", "public int;");
    document.setIncludes(inc);
    document.processMain(path.toString(), DocumentPosition.ZERO);
    document.setClassName(className);
    document.check(state.scope());
    JsonStreamWriter writer = new JsonStreamWriter();
    document.writeTypeReflectionJson(writer);
    String reflection = writer.toString();
    outputFile.append("Path:").append(path.getFileName().toString().replaceAll(Pattern.quote("\\"), "/")).append("\n");
    if (emission) {
      PhaseEmission.go(path.toString(), path, outputFile);
    }
    outputFile.append("--ISSUES-------------------------------------------").append("\n");
    final var java = document.hasErrors() ? "" : document.compileJava(state);
    String issues = document.errorsJson();
    outputFile.append(issues).append("\"");
    outputFile.append("--JAVA---------------------------------------------").append("\n");
    outputFile.append(java).append("\n");
    return new ValidationResults(issues.equals("[]") && !document.hasErrors(), java, reflection);
  }

  public static class ValidationResults {
    public final String java;
    public final boolean passedValidation;
    public final String reflection;

    public ValidationResults(final boolean passedValidation, final String java, String reflection) {
      this.passedValidation = passedValidation;
      this.java = java;
      this.reflection = reflection;
    }
  }
}
