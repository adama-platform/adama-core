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
package ape.runtime.stdlib;

import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtMaybe;
import org.junit.Assert;
import org.junit.Test;

public class LibHTMLTests {
  @Test
  public void parse1_direct() {
    NtMaybe<NtDynamic> result = LibHTML.convertHTMLtoJSON("<food x=\"abc\">Hello World<br /></food>", "food");
    Assert.assertTrue(result.has());
    Assert.assertEquals("{\"t\":\"food\",\"a\":{\"x\":\"abc\"},\"c\":[\"Hello World\",{\"t\":\"br\"}]}", result.get().json);
    Assert.assertEquals("<food x=\"abc\">Hello World<br /></food>", LibHTML.convertJSONtoHTML(result.get()));
  }

  @Test
  public void parse1_body() { // verify JSoup injection
    NtMaybe<NtDynamic> result = LibHTML.convertHTMLtoJSON("<food x=\"abc\">Hello World<br /></food>", "body");
    Assert.assertTrue(result.has());
    Assert.assertEquals("{\"t\":\"body\",\"c\":[{\"t\":\"food\",\"a\":{\"x\":\"abc\"},\"c\":[\"Hello World\",{\"t\":\"br\"}]}]}", result.get().json);
    Assert.assertEquals("<body><food x=\"abc\">Hello World<br /></food></body>", LibHTML.convertJSONtoHTML(result.get()));
  }

  @Test
  public void parse1_html() { // verify JSoup injection
    NtMaybe<NtDynamic> result = LibHTML.convertHTMLtoJSON("<food x=\"abc\">Hello World<br /></food>", "html");
    Assert.assertTrue(result.has());
    Assert.assertEquals("{\"t\":\"html\",\"c\":[{\"t\":\"head\",\"c\":[]},{\"t\":\"body\",\"c\":[{\"t\":\"food\",\"a\":{\"x\":\"abc\"},\"c\":[\"Hello World\",{\"t\":\"br\"}]}]}]}", result.get().json);
    Assert.assertEquals("<html><head></head><body><food x=\"abc\">Hello World<br /></food></body></html>", LibHTML.convertJSONtoHTML(result.get()));
  }

  @Test
  public void parse2_escape() {
    NtMaybe<NtDynamic> result = LibHTML.convertHTMLtoJSON("<food x=\"&quot;hi&quot; &apos;Mr. Bond&apos; (&lt; &gt;) &amp; Mr. Cat\"></food>", "html");
    Assert.assertTrue(result.has());
    Assert.assertEquals("{\"t\":\"html\",\"c\":[{\"t\":\"head\",\"c\":[]},{\"t\":\"body\",\"c\":[{\"t\":\"food\",\"a\":{\"x\":\"\\\"hi\\\" 'Mr. Bond' (< >) & Mr. Cat\"},\"c\":[]}]}]}", result.get().json);
    Assert.assertEquals("<html><head></head><body><food x=\"&quot;hi&quot; &apos;Mr. Bond&apos; (&lt; &gt;) &amp; Mr. Cat\"></food></body></html>", LibHTML.convertJSONtoHTML(result.get()));
  }

  @Test
  public void parseNope() {
    NtMaybe<NtDynamic> result = LibHTML.convertHTMLtoJSON("<food x=\"abc\">Hello World<br /></food>", "bar");
    Assert.assertFalse(result.has());
  }
}
