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
package ape.support;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.support.testgen.TestClass;
import ape.support.testgen.TestFile;
import ape.support.testgen.TestForge;
import ape.translator.env.CompilerOptions;
import ape.translator.env.EnvironmentState;
import ape.translator.env.GlobalObjectPool;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.tree.Document;
import ape.translator.tree.common.DocumentPosition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GenerateLanguageTests {
  public static boolean generate(String inputRootPath, String outputJavaPath, String outputErrorFile) throws Exception {
    if (isValid(inputRootPath) && isValid(outputJavaPath)) {
      final var root = new File(inputRootPath);
      final var classMap = TestForge.scan(root);
      final var outRoot = new File(outputJavaPath);
      for (final Map.Entry<String, TestClass> entry : classMap.entrySet()) {
        entry.getValue().finish(outRoot);
      }
      writeErrorCSV(inputRootPath, outputErrorFile);
      return true;
    }
    return false;
  }

  private static boolean isValid(String path) {
    File p = new File(path);
    return p.exists() && p.isDirectory();
  }

  public static void writeErrorCSV(String inputRootPath, String outputErrorFile) throws Exception {
    ByteArrayOutputStream memory = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(memory);
    final var options = CompilerOptions.start().make();
    final var globals = GlobalObjectPool.createPoolWithStdLib(RuntimeEnvironment.Tooling);
    final var state = new EnvironmentState(globals, options, RuntimeEnvironment.Tooling);
    final var root = new File(inputRootPath);
    ArrayList<File> files = new ArrayList<>();
    for (File file : root.listFiles()) {
      files.add(file);
    }
    files.sort(Comparator.comparing(File::getName));
    writer.print("file,start_line,start_character,end_line,end_character,message\n");
    for (final File testFile : files) {
      final var test = TestFile.fromFilename(testFile.getName());
      if (!test.success) {
        final var document = new Document();
        document.addSearchPath(root);
        document.processMain(testFile.toString(), DocumentPosition.ZERO);
        document.setClassName("XClass");
        document.check(state);
        final var issues = (ArrayList<HashMap<String, Object>>) new JsonStreamReader(document.errorsJson()).readJavaTree();
        for (int j = 0; j < issues.size(); j++) {
          writer.print(testFile.toString().replaceAll(Pattern.quote("\\"), "/"));
          HashMap<String, Object> node = issues.get(j);
          HashMap<String, HashMap<String, Object>> range = (HashMap<String, HashMap<String, Object>>) node.get("range");
          writer.print(",");
          writer.print(range.get("start").get("line").toString());
          writer.print(",");
          writer.print(range.get("start").get("character").toString());
          writer.print(",");
          writer.print(range.get("end").get("line").toString());
          writer.print(",");
          writer.print(range.get("end").get("character").toString());
          writer.print(",");
          JsonStreamWriter escaped = new JsonStreamWriter();
          escaped.writeString(node.get("message").toString().replaceAll(Pattern.quote("\\"), "/"));
          writer.print(escaped);
          writer.print("\n");
          writer.flush();
        }
      }
    }
    writer.flush();
    Files.writeString(new File(outputErrorFile).toPath(), memory.toString());
  }
}
