/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
