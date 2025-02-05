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
package ape.common.web;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Function;

public class UriMatcherTests {
  @Test
  public void flow_simple() {
    ArrayList<Function<String, Boolean>> matchers = new ArrayList<>();
    matchers.add((s) -> true);
    matchers.add((s) -> "yes".equals(s));
    UriMatcher matcher = new UriMatcher("name", matchers, false);
    Assert.assertFalse(matcher.matches("/yo"));
    Assert.assertTrue(matcher.matches("/yo/yes"));
    Assert.assertTrue(matcher.matches("/asfasfgsagasg124sxz/yes"));
    Assert.assertFalse(matcher.matches("/asfasfgsagasg124sxz/no"));
  }

  @Test
  public void flow_star() {
    ArrayList<Function<String, Boolean>> matchers = new ArrayList<>();
    matchers.add((s) -> true);
    matchers.add((s) -> "yes".equals(s));
    UriMatcher matcher = new UriMatcher("name", matchers, true);
    Assert.assertFalse(matcher.matches("/yo"));
    Assert.assertTrue(matcher.matches("/yo/yes"));
    Assert.assertTrue(matcher.matches("/yo/yes/yes-123541254asdg"));
    Assert.assertTrue(matcher.matches("/asfasfgsagasg124sxz/yes/asfggfs"));
    Assert.assertFalse(matcher.matches("/asfasfgsagasg124sxz/no/xgxgssdf"));
  }
}
