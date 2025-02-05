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

public class GeneratedReturnTests extends GeneratedBase {
  private String cached_ShouldReturnButDoesnt_1 = null;
  private String get_ShouldReturnButDoesnt_1() {
    if (cached_ShouldReturnButDoesnt_1 != null) {
      return cached_ShouldReturnButDoesnt_1;
    }
    cached_ShouldReturnButDoesnt_1 = generateTestOutput(false, "ShouldReturnButDoesnt_1", "./test_code/Return_ShouldReturnButDoesnt_failure.a");
    return cached_ShouldReturnButDoesnt_1;
  }

  @Test
  public void testShouldReturnButDoesntFailure() {
    assertLiveFail(get_ShouldReturnButDoesnt_1());
  }

  @Test
  public void testShouldReturnButDoesntNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_ShouldReturnButDoesnt_1());
  }

  @Test
  public void testShouldReturnButDoesntExceptionFree() {
    assertExceptionFree(get_ShouldReturnButDoesnt_1());
  }

  @Test
  public void testShouldReturnButDoesntTODOFree() {
    assertTODOFree(get_ShouldReturnButDoesnt_1());
  }

  @Test
  public void stable_ShouldReturnButDoesnt_1() {
    String live = get_ShouldReturnButDoesnt_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Return_ShouldReturnButDoesnt_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":1,\"character\":2,\"byte\":27},\"end\":{\"line\":1,\"character\":9,\"byte\":34}},\"severity\":1,\"source\":\"error\",\"message\":\"The return statement expected an expression of type `int`\",\"file\":\"./test_code/Return_ShouldReturnButDoesnt_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_ShouldntReturnButDoes_2 = null;
  private String get_ShouldntReturnButDoes_2() {
    if (cached_ShouldntReturnButDoes_2 != null) {
      return cached_ShouldntReturnButDoes_2;
    }
    cached_ShouldntReturnButDoes_2 = generateTestOutput(false, "ShouldntReturnButDoes_2", "./test_code/Return_ShouldntReturnButDoes_failure.a");
    return cached_ShouldntReturnButDoes_2;
  }

  @Test
  public void testShouldntReturnButDoesFailure() {
    assertLiveFail(get_ShouldntReturnButDoes_2());
  }

  @Test
  public void testShouldntReturnButDoesNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_ShouldntReturnButDoes_2());
  }

  @Test
  public void testShouldntReturnButDoesExceptionFree() {
    assertExceptionFree(get_ShouldntReturnButDoes_2());
  }

  @Test
  public void testShouldntReturnButDoesTODOFree() {
    assertTODOFree(get_ShouldntReturnButDoes_2());
  }

  @Test
  public void stable_ShouldntReturnButDoes_2() {
    String live = get_ShouldntReturnButDoes_2();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Return_ShouldntReturnButDoes_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":1,\"character\":2,\"byte\":20},\"end\":{\"line\":1,\"character\":11,\"byte\":29}},\"severity\":1,\"source\":\"error\",\"message\":\"The return statement expects no expression\",\"file\":\"./test_code/Return_ShouldntReturnButDoes_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
}
