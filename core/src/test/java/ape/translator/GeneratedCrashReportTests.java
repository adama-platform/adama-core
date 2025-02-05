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

public class GeneratedCrashReportTests extends GeneratedBase {
  private String cached_March21_1 = null;
  private String get_March21_1() {
    if (cached_March21_1 != null) {
      return cached_March21_1;
    }
    cached_March21_1 = generateTestOutput(false, "March21_1", "./test_code/CrashReport_March21_failure.a");
    return cached_March21_1;
  }

  @Test
  public void testMarch21Failure() {
    assertLiveFail(get_March21_1());
  }

  @Test
  public void testMarch21NotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_March21_1());
  }

  @Test
  public void testMarch21ExceptionFree() {
    assertExceptionFree(get_March21_1());
  }

  @Test
  public void testMarch21TODOFree() {
    assertTODOFree(get_March21_1());
  }

  @Test
  public void stable_March21_1() {
    String live = get_March21_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:CrashReport_March21_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":3,\"character\":2,\"byte\":74},\"end\":{\"line\":3,\"character\":14,\"byte\":86}},\"severity\":1,\"source\":\"error\",\"message\":\"The type 'maybe<String>' is using a type that was not found.\",\"file\":\"./test_code/CrashReport_March21_failure.a\"},{\"range\":{\"start\":{\"line\":2,\"character\":0,\"byte\":58},\"end\":{\"line\":3,\"character\":27,\"byte\":99}},\"severity\":1,\"source\":\"error\",\"message\":\"The field 'bound_type' has no type\",\"file\":\"./test_code/CrashReport_March21_failure.a\"},{\"range\":{\"start\":{\"line\":2,\"character\":0,\"byte\":58},\"end\":{\"line\":3,\"character\":27,\"byte\":99}},\"severity\":1,\"source\":\"error\",\"message\":\"Variable 'bound_type' has no backing type\",\"file\":\"./test_code/CrashReport_March21_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
}
