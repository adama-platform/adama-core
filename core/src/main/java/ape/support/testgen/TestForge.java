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

import ape.runtime.ops.SilentDocumentMonitor;
import ape.translator.jvm.LivingDocumentFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class TestForge {
  public static String forge(final boolean emission, final String className, final Path path, final Path inputRoot) {
    final var outputFile = new StringBuilder();
    final var passedTests = new AtomicBoolean(true);
    try {
      final var results = PhaseValidate.go(inputRoot, path, className, emission, outputFile);
      final var passedValidation = results.passedValidation;
      final var java = results.java;
      LivingDocumentFactory factory = null;
      if (passedValidation) {
        factory = PhaseCompile.go(className, java, outputFile);
      }
      if (factory != null) {
        final SilentDocumentMonitor monitor = new SilentDocumentMonitor() {
          @Override
          public void assertFailureAt(final int startLine, final int startPosition, final int endLine, final int endLinePosition, final int total, final int failures) {
            outputFile.append("ASSERT FAILURE:" + startLine + "," + startPosition + " --> " + endLine + "," + endLinePosition + " (" + failures + "/" + total + ")\n");
            passedTests.set(false);
          }
        };
        PhaseReflect.go(results, outputFile);
        PhaseRun.go(factory, monitor, passedTests, outputFile);
        PhaseTest.go(factory, monitor, passedTests, outputFile);
      }
      if (passedValidation) {
        if (passedTests.get()) {
          outputFile.append("Success").append("\n");
        } else {
          outputFile.append("AlmostTestsNotPassing").append("\n");
        }
      } else {
        outputFile.append("FailedValidation").append("\n");
      }
    } catch (final Exception ioe) {
      outputFile.append("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!").append("\n");
      outputFile.append("!!EXCEPTION!!!!!!!!!!!!!!!!!!").append("\n");
      outputFile.append("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!").append("\n");
      outputFile.append(String.format("path: %s failed due to to exception", className)).append("\n");
      final var memory = new ByteArrayOutputStream();
      final var writer = new PrintWriter(memory);
      ioe.printStackTrace(writer);
      writer.flush();
      outputFile.append(memory.toString()).append("\n");
    }
    return outputFile.toString() //
                     .replaceAll(Pattern.quote("\\\\test_code\\\\"), "/test_code/") //
                     .replaceAll(Pattern.quote("\\test_code\\"), "/test_code/") //
                     .replaceAll(Pattern.quote("\\\\\\\\test_code"), "/test_code") //
                     .replaceAll(Pattern.quote("\\\\test_code"), "/test_code") //
                     .replaceAll(Pattern.quote("\\test_code"), "/test_code"); //
  }

  public static TreeMap<String, TestClass> scan(final File root) throws IOException {
    final var classMap = new TreeMap<String, TestClass>();
    final var files = new ArrayList<File>();
    for (final File testFile : root.listFiles()) {
      files.add(testFile);
    }
    files.sort(Comparator.comparing(File::getName));
    String lastClazz = "";
    for (final File testFile : files) {
      final var test = TestFile.fromFilename(testFile.getName());
      if (!lastClazz.equals(test.clazz)) {
        System.out.print("\u001b[34mSuite: " + test.clazz + "\u001b[0m\n");
        lastClazz = test.clazz;
      }
      System.out.print("  " + test.name);
      for (int pad = test.name.length(); pad < 45; pad++) {
        System.out.print(" ");
      }
      System.out.print("... ");
      var testClass = classMap.get(test.clazz);
      if (testClass == null) {
        testClass = new TestClass(test.clazz);
        classMap.put(test.clazz, testClass);
      }
      boolean result = testClass.addTest(test);
      System.out.print((test.success == result) ? "\u001b[32mGOOD\u001b[0m\n" : "\u001b[31mBAD\u001b[0m\n");
    }
    return classMap;
  }
}
