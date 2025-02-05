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

import ape.translator.env2.Scope;
import ape.translator.parser.*;
import ape.translator.parser.*;
import ape.translator.parser.token.TokenEngine;
import ape.translator.tree.SymbolIndex;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PhaseEmission {
  public static void go(final String filename, final Path path, final StringBuilder outputFile) throws Exception {
    outputFile.append("--EMISSION-----------------------------------------").append("\n");
    final var esb = new StringBuilderDocumentHandler();
    final var readIn = Files.readString(path);
    final var tokenEngine = new TokenEngine(filename, readIn.codePoints().iterator());
    final var parser = new Parser(tokenEngine, new SymbolIndex(), Scope.makeRootDocument());
    Consumer<TopLevelDocumentHandler> play = parser.document();
    play.accept(esb);
    report(readIn, esb.builder.toString(), outputFile);
    Formatter formatter = new Formatter();
    try {
      play.accept(new WhiteSpaceNormalizeTokenDocumentHandler());
      play.accept(new FormatDocumentHandler(formatter));
      final var esb2 = new StringBuilderDocumentHandler();
      play.accept(esb2);
      outputFile.append("=FORMAT===================================================\n");
      outputFile.append(esb2.builder.toString()).append("\n");
      outputFile.append("==========================================================\n");
    } catch (Exception ex) {
      outputFile.append("-------------------------------------");
      outputFile.append("!! FORMAT-EXCEPTION !!");
      final var memory = new ByteArrayOutputStream();
      final var writer = new PrintWriter(memory);
      ex.printStackTrace(writer);
      writer.flush();
      outputFile.append(memory.toString()).append("\n");
      outputFile.append("-------------------------------------");
    }
  }

  public static void report(final String readIn, final String result, final StringBuilder outputFile) {
    if (!result.equals(readIn)) {
      outputFile.append("!!!Emission Failure!!!\n");
      outputFile.append("==========================================================\n");
      outputFile.append(result).append("\n");
      outputFile.append("=VERSUS===================================================\n");
      outputFile.append(readIn).append("\n");
      outputFile.append("==========================================================\n");
    } else {
      outputFile.append("Emission Success, Yay\n");
    }
  }
}
