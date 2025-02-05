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
package ape.common;

import org.junit.Assert;
import org.junit.Test;

public class ErrorCodeExceptionTests {
  @Test
  public void coverage() {
    ExceptionLogger logger = (t, errorCode) -> {
    };
    ErrorCodeException ex1 = new ErrorCodeException(42);
    ErrorCodeException ex2 = new ErrorCodeException(500, new NullPointerException());
    ErrorCodeException ex3 = new ErrorCodeException(4242, "nope");
    Assert.assertEquals(42, ErrorCodeException.detectOrWrap(100, ex1, logger).code);
    Assert.assertEquals(500, ErrorCodeException.detectOrWrap(100, new RuntimeException(ex2), logger).code);
    Assert.assertEquals(100, ErrorCodeException.detectOrWrap(100, new NullPointerException(), logger).code);
    Assert.assertEquals(42, ex1.code);
    Assert.assertEquals(500, ex2.code);
    Assert.assertEquals(4242, ex3.code);
  }
}
