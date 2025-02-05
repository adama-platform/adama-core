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
package ape.translator.env;

import java.util.ArrayList;

/** compiler options to control anything */
public class CompilerOptions {
  public final String className;
  public final boolean disableBillingCost; // G2G
  public final int goodwillBudget; // G2G
  public final String[] inputFiles;
  public final String outputFile;
  public final boolean produceCodeCoverage; // G2G
  public final boolean removeTests;
  public final String[] searchPaths;
  public final boolean stderrLoggingCompiler; // G2G
  public final boolean instrumentPerf;

  private CompilerOptions(final Builder builder) {
    stderrLoggingCompiler = builder.stderrLoggingCompiler;
    produceCodeCoverage = builder.produceCodeCoverage;
    disableBillingCost = builder.disableBillingCost;
    removeTests = builder.removeTests;
    goodwillBudget = builder.goodwillBudget;
    className = builder.className;
    outputFile = builder.outputFile;
    searchPaths = builder.searchPaths.toArray(new String[builder.searchPaths.size()]);
    inputFiles = builder.inputFiles.toArray(new String[builder.inputFiles.size()]);
    instrumentPerf = builder.instrumentPerf;
  }

  public static Builder start() {
    return new Builder();
  }

  public static class Builder {
    public String className;
    public boolean disableBillingCost;
    public int goodwillBudget;
    public ArrayList<String> inputFiles;
    public String outputFile;
    public String packageName;
    public boolean produceCodeCoverage;
    public boolean removeTests;
    public ArrayList<String> searchPaths;
    public boolean stderrLoggingCompiler;
    public boolean instrumentPerf;

    private Builder() {
      stderrLoggingCompiler = true;
      produceCodeCoverage = false;
      disableBillingCost = false;
      removeTests = false;
      goodwillBudget = 100000;
      packageName = null;
      className = "AGame";
      outputFile = null;
      searchPaths = new ArrayList<>();
      inputFiles = new ArrayList<>();
      instrumentPerf = false;
    }

    public Builder args(final int offset, final String... args) {
      for (var k = offset; k + 1 < args.length; k += 2) {
        final var key = args[k];
        final var value = args[k + 1].toLowerCase().trim();
        switch (key) {
          case "--billing":
            disableBillingCost = "no".equals(value) || "false".equals(value);
            break;
          case "--code-coverage":
            produceCodeCoverage = "yes".equals(value) || "true".equals(value);
            break;
          case "--remove-tests":
            removeTests = "yes".equals(value) || "true".equals(value);
            break;
          case "--silent":
            stderrLoggingCompiler = "no".equals(value) || "false".equals(value);
            break;
          case "--goodwill-budget":
            goodwillBudget = Integer.parseInt(value);
            break;
          case "--input":
            inputFiles.add(args[k + 1].trim());
            break;
          case "--add-search-path":
            searchPaths.add(args[k + 1].trim());
            break;
          case "--output":
            outputFile = args[k + 1].trim();
            break;
          case "--package":
            packageName = args[k + 1].trim();
            break;
          case "--class":
            className = args[k + 1].trim();
            break;
        }
      }
      return this;
    }

    public Builder enableCodeCoverage() {
      produceCodeCoverage = true;
      return this;
    }

    public CompilerOptions make() {
      return new CompilerOptions(this);
    }

    public Builder noCost() {
      disableBillingCost = true;
      return this;
    }

    public Builder instrument() {
      instrumentPerf = true;
      return this;
    }
  }
}
