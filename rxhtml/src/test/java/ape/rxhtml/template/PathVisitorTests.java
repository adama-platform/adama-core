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
package ape.rxhtml.template;

import ape.rxhtml.template.sp.PathVisitor;
import org.junit.Assert;
import org.junit.Test;

public class PathVisitorTests {
  public class PathVisitorToStr implements PathVisitor {
    StringBuilder sb;
    private PathVisitorToStr() {
      this.sb = new StringBuilder();
    }

    @Override
    public void data() {
      sb.append("[data]");
    }

    @Override
    public void view() {
      sb.append("[view]");
    }

    @Override
    public void root() {
      sb.append("[root]");
    }

    @Override
    public void parent() {
      sb.append("[parent]");
    }

    @Override
    public void dive(String child) {
      sb.append("[dive:" + child + "]");
    }

    @Override
    public void use(String field) {
      sb.append("[use:" + field + "]");
    }

    @Override
    public String toString() {
      return sb.toString();
    }
  }

  @Test
  public void flow1() {
    PathVisitorToStr to_str = new PathVisitorToStr();
    PathVisitor.visit("view:/root/child/thing", to_str);
    Assert.assertEquals("[view][root][dive:root][dive:child][use:thing]", to_str.toString());
  }

  @Test
  public void flow2() {
    PathVisitorToStr to_str = new PathVisitorToStr();
    PathVisitor.visit("data:/thing", to_str);
    Assert.assertEquals("[data][root][use:thing]", to_str.toString());
  }

  @Test
  public void flow3() {
    PathVisitorToStr to_str = new PathVisitorToStr();
    PathVisitor.visit("data:../thing", to_str);
    Assert.assertEquals("[data][parent][use:thing]", to_str.toString());
  }
}
