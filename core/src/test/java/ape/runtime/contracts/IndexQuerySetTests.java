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
package ape.runtime.contracts;

import ape.runtime.natives.NtMaybe;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class IndexQuerySetTests {
  @Test
  public void coverage() {
    ArrayList<String> log = new ArrayList<>();
    IndexQuerySet iqs = new IndexQuerySet() {
      @Override
      public void intersect(int column, int value, LookupMode mode) {
        log.add("c:" + column + "==" + value + ";" + mode);
      }

      @Override
      public void primary(int value) {
        log.add("p:" + value);
      }

      @Override
      public void push() {
        log.add("PUSH");
      }

      @Override
      public void finish() {
        log.add("FINISH");
      }
    };
    iqs.intersect(5, new NtMaybe<>(5), IndexQuerySet.LookupMode.Equals);
    iqs.intersect(5, new NtMaybe<>(), IndexQuerySet.LookupMode.Equals);
    iqs.intersect(5, new NtMaybe<>(7), IndexQuerySet.LookupMode.Equals);
    iqs.primary(100);
    iqs.push();
    iqs.finish();
    Assert.assertEquals("c:5==5;Equals", log.get(0));
    Assert.assertEquals("c:5==0;Equals", log.get(1));
    Assert.assertEquals("c:5==7;Equals", log.get(2));
    Assert.assertEquals("p:100", log.get(3));
    Assert.assertEquals("PUSH", log.get(4));
    Assert.assertEquals("FINISH", log.get(5));
  }
}
