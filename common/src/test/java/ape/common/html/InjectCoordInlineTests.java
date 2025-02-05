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
package ape.common.html;

import org.junit.Assert;
import org.junit.Test;

public class InjectCoordInlineTests {
  @Test
  public void foo() {
    Assert.assertEquals("<element ln:ch=\"0;0;0;9;name\">", InjectCoordInline.execute("<element>", "name"));
    Assert.assertEquals("<element ln:ch=\"0;0;0;10;name\" />", InjectCoordInline.execute("<element/>", "name"));
    Assert.assertEquals("<element ln:ch=\"0;0;0;18;name\"         >", InjectCoordInline.execute("<element         >", "name"));

    Assert.assertEquals("<!--comment-->HI", InjectCoordInline.execute("<!--comment-->HI", "name"));
  }

  @Test
  public void scriptEmbed() {
    Assert.assertEquals("<hi ln:ch=\"0;0;0;4;name\">there<script ln:ch=\"0;9;0;17;name\">foo()</script></hi><x ln:ch=\"0;36;0;39;name\"></x>", InjectCoordInline.execute("<hi>there<script>foo()</script></hi><x></x>", "name"));
    Assert.assertEquals("<hi ln:ch=\"0;0;0;4;name\">there<script ln:ch=\"0;9;0;17;name\">if(1<2) {}</script></hi><x ln:ch=\"0;41;0;44;name\"></x>", InjectCoordInline.execute("<hi>there<script>if(1<2) {}</script></hi><x></x>", "name"));
  }
}
