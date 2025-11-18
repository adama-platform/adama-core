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
package ape.translator;

import org.junit.Test;

public class GeneratedRegressionTests extends GeneratedBase {
  private String cached_Issue20251114_1 = null;
  private String get_Issue20251114_1() {
    if (cached_Issue20251114_1 != null) {
      return cached_Issue20251114_1;
    }
    cached_Issue20251114_1 = generateTestOutput(false, "Issue20251114_1", "./test_code/Regression_Issue20251114_failure.a");
    return cached_Issue20251114_1;
  }

  @Test
  public void testIssue20251114Failure() {
    assertLiveFail(get_Issue20251114_1());
  }

  @Test
  public void testIssue20251114NotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_Issue20251114_1());
  }

  @Test
  public void testIssue20251114ExceptionFree() {
    assertExceptionFree(get_Issue20251114_1());
  }

  @Test
  public void testIssue20251114TODOFree() {
    assertTODOFree(get_Issue20251114_1());
  }

  @Test
  public void stable_Issue20251114_1() {
    String live = get_Issue20251114_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Regression_Issue20251114_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":8,\"character\":2,\"byte\":134},\"end\":{\"line\":10,\"character\":3,\"byte\":206}},\"severity\":1,\"source\":\"error\",\"message\":\"me has been defined already\",\"file\":\"./test_code/Regression_Issue20251114_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
}
