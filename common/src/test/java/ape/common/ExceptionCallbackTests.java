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

import java.util.ArrayList;

public class ExceptionCallbackTests {
  @Test
  public void flow() {
    ArrayList<String> v = new ArrayList<>();
    ExceptionCallback<String> cb = new ExceptionCallback<String>() {
      @Override
      public void invoke(String value) throws ErrorCodeException {
        if ("x".equals(value)) {
          throw new ErrorCodeException(1);
        }
        v.add(value);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        v.add("ex:" + ex.code);
      }
    };
    cb.success("yay");
    cb.failure(new ErrorCodeException(123));
    cb.success("x");
    Assert.assertEquals(3, v.size());
    Assert.assertEquals("yay", v.get(0));
    Assert.assertEquals("ex:123", v.get(1));
    Assert.assertEquals("ex:1", v.get(2));
  }
}
