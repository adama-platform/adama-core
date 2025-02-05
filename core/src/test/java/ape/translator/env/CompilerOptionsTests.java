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

import org.junit.Assert;
import org.junit.Test;

public class CompilerOptionsTests {
  @Test
  public void args1() {
    final var options =
        CompilerOptions.start()
            .args(
                0,
                "--billing",
                "no",
                "--code-coverage",
                "no",
                "--remove-tests",
                "no",
                "--silent",
                "no",
                "--goodwill-budget",
                "0",
                "--class",
                "ClassName")
            .make();
    Assert.assertTrue(options.disableBillingCost);
    Assert.assertFalse(options.produceCodeCoverage);
    Assert.assertFalse(options.removeTests);
    Assert.assertTrue(options.stderrLoggingCompiler);
    Assert.assertEquals(0, options.goodwillBudget);
    Assert.assertEquals("ClassName", options.className);
  }

  @Test
  public void args2() {
    final var options =
        CompilerOptions.start()
            .args(
                0,
                "--billing",
                "yes",
                "--code-coverage",
                "yes",
                "--remove-tests",
                "yes",
                "--silent",
                "yes",
                "--goodwill-budget",
                "5000",
                "--class",
                "ClassName2")
            .make();
    Assert.assertFalse(options.disableBillingCost);
    Assert.assertTrue(options.produceCodeCoverage);
    Assert.assertTrue(options.removeTests);
    Assert.assertFalse(options.stderrLoggingCompiler);
    Assert.assertEquals(5000, options.goodwillBudget);
    // Assert.assertEquals("org.foo2", options.packageName);
    Assert.assertEquals("ClassName2", options.className);
  }

  @Test
  public void builder() {
    final var options = CompilerOptions.start().enableCodeCoverage().noCost().make();
    Assert.assertTrue(options.disableBillingCost);
    Assert.assertTrue(options.produceCodeCoverage);
  }

  @Test
  public void core() {
    final var options =
        CompilerOptions.start()
            .args(
                0,
                "--input",
                "a.a",
                "--input",
                "b.a",
                "--add-search-path",
                "foo",
                "--add-search-path",
                "goo",
                "--output",
                "oout")
            .make();
    Assert.assertEquals("a.a", options.inputFiles[0]);
    Assert.assertEquals("b.a", options.inputFiles[1]);
    Assert.assertEquals("foo", options.searchPaths[0]);
    Assert.assertEquals("goo", options.searchPaths[1]);
    Assert.assertEquals("oout", options.outputFile);
  }

  @Test
  public void offset() {
    final var options =
        CompilerOptions.start()
            .args(
                1,
                "compile",
                "--billing",
                "yes",
                "--code-coverage",
                "yes",
                "--remove-tests",
                "yes",
                "--silent",
                "yes",
                "--goodwill-budget",
                "5000",
                "--package",
                "org.foo2",
                "--class",
                "ClassName2")
            .make();
    Assert.assertFalse(options.disableBillingCost);
    Assert.assertTrue(options.produceCodeCoverage);
    Assert.assertTrue(options.removeTests);
    Assert.assertFalse(options.stderrLoggingCompiler);
    Assert.assertEquals(5000, options.goodwillBudget);
    Assert.assertEquals("ClassName2", options.className);
  }
}
