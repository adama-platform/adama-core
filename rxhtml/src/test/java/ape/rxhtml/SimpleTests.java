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
package ape.rxhtml;

import ape.rxhtml.template.config.ShellConfig;
import org.junit.Test;

public class SimpleTests {

  @Test
  public void emptyvalue() {
    drive("<template name=\"foo\">how<input checked /></template>");
  }

  private static void drive(String rxhtml) {
    System.err.println(RxHtmlTool.convertStringToTemplateForest(rxhtml, null, ShellConfig.start().withFeedback((e, x) -> System.err.println(x)).end()));
  }

  @Test
  public void basic() {
    drive("<template name=\"foo\">how<b class=\"foo bar\">d</b>y<img src=\"imgurl\"/></template>");
  }

  @Test
  public void single_var() {
    drive("<template name=\"foo\"><lookup name=\"x\"/></template>");
  }

  @Test
  public void repeat_var() {
    drive("<template name=\"foo\"><lookup name=\"x\"/><lookup name=\"x\"/><lookup name=\"x\"/></template>");
  }

  @Test
  public void sanityStyle() {
    drive("<forest><style>XYZ</style></forest>");
  }

  @Test
  public void preWS() {
    drive("<forest><page uri=\"/\"><pre>X\nY\nZ\n</page></forest>");
  }

  @Test
  public void adamaWS() {
    drive("<forest><page uri=\"/\"><pre adama>X\nY\nZ\n</pre></page></forest>");
  }

  @Test
  public void highlightWS() {
    drive("<forest><page uri=\"/\"><pre highlight=\"css\">X\nY\nZ\n</pre></forest>");
  }
}
