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

import javax.annotation.processing.Generated;
import org.junit.Test;

@Generated("ape.support.testgen.TestClass")
public class GeneratedBlockTests extends GeneratedBase {
  private String cached_DeadCode_1 = null;
  private String get_DeadCode_1() {
    if (cached_DeadCode_1 != null) {
      return cached_DeadCode_1;
    }
    cached_DeadCode_1 = generateTestOutput(false, "DeadCode_1", "./test_code/Block_DeadCode_failure.a");
    return cached_DeadCode_1;
  }

  @Test
  public void testDeadCodeFailure() {
    assertLiveFail(get_DeadCode_1());
  }

  @Test
  public void testDeadCodeNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_DeadCode_1());
  }

  @Test
  public void testDeadCodeExceptionFree() {
    assertExceptionFree(get_DeadCode_1());
  }

  @Test
  public void testDeadCodeTODOFree() {
    assertTODOFree(get_DeadCode_1());
  }

  @Test
  public void stable_DeadCode_1() {
    String live = get_DeadCode_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Block_DeadCode_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":2,\"character\":2,\"byte\":25},\"end\":{\"line\":2,\"character\":14,\"byte\":37}},\"severity\":1,\"source\":\"error\",\"message\":\"This code is unreachable.\",\"file\":\"./test_code/Block_DeadCode_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_DuplicateVariable_2 = null;
  private String get_DuplicateVariable_2() {
    if (cached_DuplicateVariable_2 != null) {
      return cached_DuplicateVariable_2;
    }
    cached_DuplicateVariable_2 = generateTestOutput(false, "DuplicateVariable_2", "./test_code/Block_DuplicateVariable_failure.a");
    return cached_DuplicateVariable_2;
  }

  @Test
  public void testDuplicateVariableFailure() {
    assertLiveFail(get_DuplicateVariable_2());
  }

  @Test
  public void testDuplicateVariableNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_DuplicateVariable_2());
  }

  @Test
  public void testDuplicateVariableExceptionFree() {
    assertExceptionFree(get_DuplicateVariable_2());
  }

  @Test
  public void testDuplicateVariableTODOFree() {
    assertTODOFree(get_DuplicateVariable_2());
  }

  @Test
  public void stable_DuplicateVariable_2() {
    String live = get_DuplicateVariable_2();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Block_DuplicateVariable_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":2,\"character\":2,\"byte\":24},\"end\":{\"line\":2,\"character\":8,\"byte\":30}},\"severity\":1,\"source\":\"error\",\"message\":\"Variable 'x' was already defined\",\"file\":\"./test_code/Block_DuplicateVariable_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
}
