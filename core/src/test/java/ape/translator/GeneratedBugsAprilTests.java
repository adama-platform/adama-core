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

public class GeneratedBugsAprilTests extends GeneratedBase {
  private String cached_Issues_1 = null;
  private String get_Issues_1() {
    if (cached_Issues_1 != null) {
      return cached_Issues_1;
    }
    cached_Issues_1 = generateTestOutput(false, "Issues_1", "./test_code/BugsApril_Issues_failure.a");
    return cached_Issues_1;
  }

  @Test
  public void testIssuesFailure() {
    assertLiveFail(get_Issues_1());
  }

  @Test
  public void testIssuesNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_Issues_1());
  }

  @Test
  public void testIssuesExceptionFree() {
    assertExceptionFree(get_Issues_1());
  }

  @Test
  public void testIssuesTODOFree() {
    assertTODOFree(get_Issues_1());
  }

  @Test
  public void stable_Issues_1() {
    String live = get_Issues_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:BugsApril_Issues_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":3,\"character\":6,\"byte\":21},\"end\":{\"line\":3,\"character\":14,\"byte\":29}},\"severity\":1,\"source\":\"error\",\"message\":\"Expression expected to be computed, rather than assigned to\",\"file\":\"./test_code/BugsApril_Issues_failure.a\"},{\"range\":{\"start\":{\"line\":3,\"character\":6,\"byte\":21},\"end\":{\"line\":3,\"character\":14,\"byte\":29}},\"severity\":1,\"source\":\"error\",\"message\":\"Could not find a meaning for 'bool' + 'int'\",\"file\":\"./test_code/BugsApril_Issues_failure.a\"},{\"range\":{\"start\":{\"line\":4,\"character\":3,\"byte\":35},\"end\":{\"line\":4,\"character\":11,\"byte\":43}},\"severity\":1,\"source\":\"error\",\"message\":\"Expression expected to be computed, rather than assigned to\",\"file\":\"./test_code/BugsApril_Issues_failure.a\"},{\"range\":{\"start\":{\"line\":4,\"character\":3,\"byte\":35},\"end\":{\"line\":4,\"character\":11,\"byte\":43}},\"severity\":1,\"source\":\"error\",\"message\":\"Could not find a meaning for 'bool' + 'int'\",\"file\":\"./test_code/BugsApril_Issues_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
}
