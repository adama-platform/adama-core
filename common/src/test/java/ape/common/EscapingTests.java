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

public class EscapingTests {
  @Test
  public void noop1() {
    Assert.assertEquals("Hi", new Escaping("Hi").go());
  }

  @Test
  public void noop2() {
    Assert.assertEquals("猿も木から落ちる", new Escaping("猿も木から落ちる").go());
  }

  @Test
  public void newline() {
    Assert.assertEquals("hi\\nthere", new Escaping("hi\nthere").toString());
  }

  @Test
  public void return_go() {
    Assert.assertEquals("hithere", new Escaping("hi\rthere").go());
  }

  @Test
  public void return_stay() {
    Assert.assertEquals("hi\\rthere", new Escaping("hi\rthere").keepReturns().toString());
  }

  @Test
  public void return_preserve() {
    Assert.assertEquals("hi\rthere", new Escaping("hi\rthere").keepReturns().dontEscapeReturns().toString());
  }

  @Test
  public void quoting1() {
    Assert.assertEquals("Hi 'there' \\\"yo\\\"", new Escaping("Hi 'there' \"yo\"").go());
  }

  @Test
  public void quoting2() {
    Assert.assertEquals("Hi \\'there\\' \"yo\"", new Escaping("Hi 'there' \"yo\"").switchQuotes().toString());
  }

  @Test
  public void slash() {
    Assert.assertEquals("a \\\\ b", new Escaping("a \\ b").switchQuotes().go());
  }

  @Test
  public void keeplash() {
    Assert.assertEquals("a \\ b", new Escaping("a \\ b").keepSlashes().go());
  }

  @Test
  public void killnewlines() {
    Assert.assertEquals("a  b", new Escaping("a \n b").removeNewLines().go());
  }
}
