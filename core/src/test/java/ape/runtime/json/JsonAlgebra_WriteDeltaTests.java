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
package ape.runtime.json;

import org.junit.Assert;
import org.junit.Test;

public class JsonAlgebra_WriteDeltaTests {

  private Object of(String x) {
    return new JsonStreamReader(x).readJavaTree();
  }

  private String delta(Object from, Object to) {
    JsonStreamWriter writer = new JsonStreamWriter();
    JsonAlgebra.writeObjectFieldDelta(from, to, writer);
    return writer.toString();
  }

  @Test
  public void change1() {
    Object from = of("null");
    Object to = of("{}");
    Assert.assertEquals("{}", delta(from, to));
  }

  @Test
  public void change2() {
    Object from = of("{\"x\":true}");
    Object to = of("{}");
    Assert.assertEquals("{\"x\":null}", delta(from, to));
  }

  @Test
  public void change3() {
    Object from = of("{}");
    Object to = of("{\"x\":true}");
    Assert.assertEquals("{\"x\":true}", delta(from, to));
  }

  @Test
  public void change4() {
    Object from = of("{\"x\":false}");
    Object to = of("{\"x\":{\"x\":true}}");
    Assert.assertEquals("{\"x\":{\"x\":true}}", delta(from, to));
  }

  @Test
  public void change5() {
    Object from = of("{\"x\":{\"x\":false}}");
    Object to = of("{\"x\":{\"x\":true}}");
    Assert.assertEquals("{\"x\":{\"x\":true}}", delta(from, to));
  }

}
